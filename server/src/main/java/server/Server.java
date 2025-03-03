package server;

import com.google.gson.Gson;
import handler.ClearHandler;
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

    public Server(ClearService clearService, UserService userService, GameService gameService) {
        this.clearService = clearService;
        this.userService = userService;
        this.gameService = gameService;
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clear);
        Spark.post("/user", this::register);
        Spark.post("/session", new LoginHandler(userService));
        Spark.delete("/session", new LogoutHandler(userService));
        Spark.get("/game", new ListHandler(gameService));
        Spark.post("/game", new CreateHandler(gameService));
        Spark.put("/game", new JoinHandler(gameService));

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
        res.status(204);
        return "";
    }

    private Object register(Request req, Response res) throws ResponseException {
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);
        newUser = userService.register(newUser);
        // register returns authData, need to figure out what to do here instead of the userData?
        return new Gson().toJson(newUser);
    }

    private Object login(Request req, Response res) throws ResponseException {
        UserData newUser = new Gson().fromJson(req.body(), UserData.class);
        newUser = userService.login(newUser);
        return new Gson().toJson(newUser);
    }
}
