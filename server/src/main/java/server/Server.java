package server;

import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.sql.*;
import model.*;
import service.*;
import spark.*;

import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Server {
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;
    private final GameDAO gameDAO;

    private final int unAuth;
    private final int internalErr;

    private static final Gson serializer = new Gson();

    public Server() {
        try {
            AuthDAO authDAO = new SQLAuthDAO();
            UserDAO userDAO = new SQLUserDAO();
            GameDAO gameDAO = new SQLGameDAO();

            // initialize services
            this.clearService = new ClearService(userDAO, authDAO, gameDAO);
            this.userService = new UserService(userDAO, authDAO);
            this.gameService = new GameService(gameDAO, authDAO);
            this.authDAO = authDAO;
            this.userDAO = userDAO;
            this.gameDAO = gameDAO;

            this.unAuth = HttpURLConnection.HTTP_UNAUTHORIZED;
            this.internalErr = HttpURLConnection.HTTP_INTERNAL_ERROR;
        } catch (DataAccessException ex) {
            throw new RuntimeException("Error initializing daos: " + ex.getMessage());
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clearHandler);
        Spark.post("/user", this::registerHandler);
        Spark.post("/session", this::loginHandler);
        Spark.delete("/session", this::logoutHandler);
        Spark.get("/game", this::listGamesHandler);
        Spark.post("/game", this::createGameHandler);
        Spark.put("/game", this::joinGameHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    // clear all data from db (or ram memory)
    private Object clearHandler(Request req, Response res) throws ResponseException {
        try {
            clearService.clear();
            res.status(HttpURLConnection.HTTP_OK);

            // return empty JSON obj to show success
            return "{}";
        } catch (ResponseException e) {
            // 500 error code
            return errorHandler(res, e, HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private Object registerHandler(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData newUser = serializer.fromJson(req.body(), UserData.class);

        // verify input is valid
        if (newUser == null || newUser.username() == null || newUser.username().isBlank() || newUser.password() == null || newUser.email() == null) {
            res.status(HttpURLConnection.HTTP_BAD_REQUEST); // 400 error code
            return serializer.toJson(Map.of("message", "Error: invalid input"));
        }

        try {
            // call the appropriate service
            AuthData authData = userService.register(newUser);

            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return serializer.toJson(authData);
        } catch (IllegalArgumentException e) {
            res.status(HttpURLConnection.HTTP_FORBIDDEN); // 403 error code
            return serializer.toJson(Map.of("message", "Error: username is already taken"));
        } catch (Exception e) {
            return errorHandler(res, e, HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private Object loginHandler(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData user = serializer.fromJson(req.body(), UserData.class);

        try {
            // call the appropriate service
            AuthData authData = userService.login(user);

            // verify user is authenticated
            if (authData == null) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
                return serializer.toJson(Map.of("message", "Error: Username or password is incorrect"));
            }
            res.status(HttpURLConnection.HTTP_OK); // 200 error code

            // when the service responds convert the response object back to JSON and send it
            return serializer.toJson(authData);
        } catch (ResponseException e) {
            return errorHandler(res, e, e.statusCode() == 401 ? unAuth : internalErr);
        }
    }

    private Object logoutHandler(Request req, Response res) throws DataAccessException {
        // get auth header (token)
        String authToken = req.headers("authorization");
        if (authToken == null || authDAO.getAuth(authToken) == null) {
            res.status(HttpURLConnection.HTTP_UNAUTHORIZED); // 401 error code
            return serializer.toJson(Map.of("message", "Error: invalid authToken"));
        }

        try {
            // call the appropriate service
            userService.logout(authToken);

            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return "{}";
        } catch (ResponseException e) {
            return errorHandler(res, e, e.statusCode() == 401 ? unAuth : internalErr);
        }
    }

    private Object listGamesHandler(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        try {
            // get list of games
            var list = gameService.list(authToken);

            if (list == null || list.isEmpty()) {
                list = new ArrayList<>();
            }

            System.out.println("list of games: " + list.size());

            // summarize list (so response is shorter and easier to read)
            var listSum = list.stream().map(game ->
                new GameDetails(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName())).toList();

            // call the appropriate service
            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return serializer.toJson(Map.of("games", listSum));
        } catch (ResponseException e) {
            // e.g. token is invalid
            return errorHandler(res, e, e.statusCode() == 401 ? unAuth : internalErr);
        }
    }

    private Object createGameHandler(Request req, Response res) throws DataAccessException {
        // get auth header (token)
        String authToken = checkAuth(req, res);
        if (authDAO.getAuth(authToken) == null) {
            res.status(HttpURLConnection.HTTP_UNAUTHORIZED); // 401 error code
            return serializer.toJson(Map.of("message", "Error: invalid authToken"));
        }

        if (req.body() == null || req.body().isEmpty()) {
            res.status(HttpURLConnection.HTTP_BAD_REQUEST); // 400 error code
            return serializer.toJson(Map.of("message", "Error: bad request"));
        }

        // convert HTTP request into Java usable objects and data
        GameData gameData = serializer.fromJson(req.body(), GameData.class);

        try {
            // call the appropriate service
            GameData newGame = gameService.create(gameData, authToken);

            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return serializer.toJson(Map.of("gameID", newGame.gameID()));

        } catch (ResponseException e) {
            // e.g. token is invalid
            return errorHandler(res, e, e.statusCode() == 401 ? unAuth : internalErr);
        }
    }

    private Object joinGameHandler(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        // convert HTTP request into Java usable objects and data
        JoinRequest joinRequest = serializer.fromJson(req.body(), JoinRequest.class);

        // verify valid gameid and usercolor
        if (joinRequest.gameID() <= 0 || joinRequest.playerColor() == null) {
            res.status(HttpURLConnection.HTTP_BAD_REQUEST); // 400 error code
            String message = null;
            if (joinRequest.gameID() <= 0) {
                message = "gameID is invalid";
            }
            if (joinRequest.playerColor() == null) {
                message = "must enter a player color";
            }
            return serializer.toJson(Map.of("message", "Error: bad request" + message));
        }

        try {
            // call the appropriate service
            gameService.join(joinRequest, authToken);
            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return "{}";
        } catch (ResponseException e) {
            // e.g. token is invalid
            int forbidden = HttpURLConnection.HTTP_FORBIDDEN;
            return errorHandler(res, e, e.statusCode() == 401 ? unAuth : e.statusCode() == 403 ? forbidden : internalErr);
        }
    }

    private String checkAuth(Request req, Response res) throws ResponseException{
        String authToken = req.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            res.status(HttpURLConnection.HTTP_UNAUTHORIZED); // 401 error
            throw new ResponseException(HttpURLConnection.HTTP_UNAUTHORIZED, "Error: unauthorized");
        }

        return authToken;
    }

    // added for code quality
    private Object errorHandler(Response res, Exception exception, int statusCode) {
        // set statusCode
        res.status(statusCode);

        // returnn error message
        return serializer.toJson(Map.of("message", "Error: " + exception.getMessage()));
    }
}
