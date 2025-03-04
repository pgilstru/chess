package server;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import service.ClearService;
import service.GameService;
import service.ResponseException;
import service.UserService;
import spark.*;

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
//        Spark.get("/game", this::listGames);
//        Spark.post("/game", this::createGame);
//        Spark.put("/game", this::joinGame);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clear(Request req, Response res) throws ResponseException {
        clearService.clear();
        res.status(200);

        // return empty JSON obj to show success
        return new Gson().toJson(new Object());
    }

    private Object register(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);

        // call the appropriate service
        AuthData authData = userService.register(newUser);

        res.status(200);

        // when the service responds convert the response object back to JSON and send it
        return new Gson().toJson(authData);
    }

    private Object login(Request req, Response res) throws ResponseException {
        // convert HTTP request into Java usable objects and data
        UserData user = new Gson().fromJson(req.body(), UserData.class);

        // call the appropriate service
        AuthData authData = userService.login(user);

        res.status(200);

        // when the service responds convert the response object back to JSON and send it
        return new Gson().toJson(authData);
    }

    private Object logout(Request req, Response res) throws ResponseException {
        // get auth header (token)
        String authToken = req.headers("authorization");

        try {
            // call the appropriate service
            userService.logout(authToken);
            res.status(200);

            // when the service responds convert the response object back to JSON and send it
            return new Gson().toJson(new Object());
        } catch (IllegalArgumentException e) {
            // e.g. token is invalid
            res.status(400);
            return new Gson().toJson(e.getMessage());
        }
    }
}
