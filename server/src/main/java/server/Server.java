package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import model.UserData;
import service.ClearService;
import service.GameService;
import service.ResponseException;
import service.UserService;
import spark.*;

import java.util.Map;

public class Server {
    private final ClearService clearService;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        // initialize daos
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // initialize services
        this.clearService = new ClearService(userDAO, authDAO, gameDAO);
        this.userService = new UserService(userDAO, authDAO);
        this.gameService = new GameService(gameDAO, authDAO);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

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
    private Object clear(Request req, Response res) throws ResponseException {
        clearService.clear();
        res.status(200);

        // return empty JSON obj to show success
        return new Gson().toJson(new Object());
    }

    private Object register(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);

        try {
            // call the appropriate service
            AuthData authData = userService.register(newUser);

            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(authData);
        } catch (ResponseException e) {
            res.status(e.StatusCode());
            return new Gson().toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object login(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData user = new Gson().fromJson(req.body(), UserData.class);

        try {
            // call the appropriate service
            AuthData authData = userService.login(user);

            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(authData);
        } catch (ResponseException e) {
            res.status(e.StatusCode());
            return new Gson().toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object logout(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        try {
            // call the appropriate service
            userService.logout(authToken);
            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(new Object());
        } catch (IllegalArgumentException e) {
            res.status(400);
            return new Gson().toJson(Map.of("message", e.getMessage()));
        }
    }

    private Object listGames(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        try {
            // call the appropriate service
            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(Map.of("games", gameService.list(authToken)));
        } catch (IllegalArgumentException e) {
            // e.g. token is invalid
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object createGame(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        // convert HTTP request into Java usable objects and data
        GameData gameData = new Gson().fromJson(req.body(), GameData.class);

        if (gameData.gameName() == null || gameData.gameName().isEmpty()) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        }
        try {
            // call the appropriate service
            GameData newGame = gameService.create(gameData, authToken);
            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(newGame);
        } catch (IllegalArgumentException e) {
            // e.g. token is invalid
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    private Object joinGame(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = checkAuth(req, res);

        // convert HTTP request into Java usable objects and data
        JoinRequest joinRequest = new Gson().fromJson(req.body(), JoinRequest.class);

        // verify valid gameid and usercolor
        if (joinRequest.gameID() == 0 || joinRequest.userColor() == null) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        }

        try {
            // call the appropriate service
            gameService.join(joinRequest, authToken);
            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(new Object());
        } catch (ResponseException e) {
            // e.g. token is invalid
            res.status(e.StatusCode());
            return new Gson().toJson(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            // e.g. token is invalid
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        }
    }

    private String checkAuth(Request req, Response res) {
        String authToken = req.headers("authorization");
        if (authToken == null) {
            res.status(401);
            return new Gson().toJson(Map.of("message", "Error: unauthorized"));
        }

        return authToken;
    }
}
