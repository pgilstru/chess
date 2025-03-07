package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import model.UserData;
import service.*;
import spark.*;

import java.net.HttpURLConnection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Server {
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public Server() {
        // initialize daos
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // initialize services
        this.clearService = new ClearService(userDAO, authDAO, gameDAO);
        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
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
        var serializer = new Gson();

        try {
            clearService.clear();
            res.status(HttpURLConnection.HTTP_OK);

            // return empty JSON obj to show success
//            return serializer.toJson(new Object());
            return "{}";
        } catch (ResponseException e) {
            // 500 error code
            res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object registerHandler(Request req, Response res) throws ResponseException {
        var serializer = new Gson();

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
            res.status(HttpURLConnection.HTTP_INTERNAL_ERROR); // 500 error code
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
//        } catch (ResponseException e) {
//            if (e.StatusCode() == 400) {
//                res.status(HttpURLConnection.HTTP_BAD_REQUEST);
//            } else if (e.StatusCode() == 403) {
//                res.status(HttpURLConnection.HTTP_FORBIDDEN);
//            } else {
//                // 500 error code
//                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
//            }
//            return serializer.toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object loginHandler(Request req, Response res) throws ResponseException {
        var serializer = new Gson();

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
            if (e.StatusCode() == 401) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
            } else {
                // 500 error code
                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            return serializer.toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object logoutHandler(Request req, Response res) throws DataAccessException {
        var serializer = new Gson();

        // get auth header (token)
//        String authToken = checkAuth(req, res);
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
            if (e.StatusCode() == 401) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
            } else {
                // 500 error code
                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object listGamesHandler(Request req, Response res) throws ResponseException {
        var serializer = new Gson();

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
            var listSum = list.stream().map(game -> {
                var gameData = new LinkedHashMap<String, Object>();
                gameData.put("gameID", game.gameID());
                gameData.put("whiteUsername", game.whiteUsername() != null ? game.whiteUsername() : null);
                gameData.put("blackUsername", game.blackUsername() != null ? game.blackUsername() : null);
                gameData.put("gameName", game.gameName());
                return gameData;
            }).toList();

            // call the appropriate service
            res.status(HttpURLConnection.HTTP_OK); // 200 code

            // when the service responds convert the response object back to JSON and send it
            return serializer.toJson(Map.of("games", listSum));
        } catch (ResponseException e) {
            // e.g. token is invalid
            if (e.StatusCode() == 401) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
            } else {
                // 500 error code
                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object createGameHandler(Request req, Response res) throws DataAccessException {
        // get auth header (token)
//        String authToken = checkAuth(req, res);

        var serializer = new Gson();

        String authToken = req.headers("authorization");
        if (authToken == null || authDAO.getAuth(authToken) == null) {
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
//            return serializer.toJson(Map.of("gameName", newGame.gameName()));
            return serializer.toJson(Map.of("gameID", newGame.gameID()));

        } catch (ResponseException e) {
            // e.g. token is invalid
            if (e.StatusCode() == 401) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
            } else {
                // 500 error code
                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object joinGameHandler(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);
        var serializer = new Gson();

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
            if (e.StatusCode() == 401) {
                res.status(HttpURLConnection.HTTP_UNAUTHORIZED);
            } else if (e.StatusCode() == 403) {
                res.status(HttpURLConnection.HTTP_FORBIDDEN);
            } else {
                // 500 error code
                res.status(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
            return serializer.toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private String checkAuth(Request req, Response res) throws ResponseException{
        String authToken = req.headers("authorization");

        if (authToken == null || authToken.isEmpty()) {
            res.status(HttpURLConnection.HTTP_UNAUTHORIZED); // 401 error
            throw new ResponseException(401, "Error: unauthorized");
        }

        return authToken;
    }
}
