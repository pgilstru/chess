package server.websocket;

import com.google.gson.Gson;
import model.GameData;
import model.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    private final GameService gameService;
//    private final Gson serializer = new Gson();

    // constructor for WebSocketHandler
    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket is connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, String message) {
        System.out.println("WebSocket connection closed: " + session.getRemoteAddress());

    }


    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), session);
            case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
            case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
        }
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException {
        // connect to a specific game
        connections.add(authToken, gameID, session);

        // load the game
        GameData game = GameService.load(gameID);

        var message = String.format("User joined the game: " + gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(gameID, authToken, notification);
    }
// update make move!
    private void makeMove(String authToken, Integer gameID, Session session) throws IOException {
        // make a specific move in a specific game
        try {
            var message = String.format("User joined the game: " + gameID);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            connections.broadcast(gameID, authToken, notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException {
        // leave a specific game
        connections.remove(authToken, gameID);
        var message = String.format("User joined the game: " + gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(gameID, authToken, notification);
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        // resign from a specific game
        connections.remove(authToken, gameID);
        var message = String.format("User joined the game: " + gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        connections.broadcast(gameID, authToken, notification);
    }
}
