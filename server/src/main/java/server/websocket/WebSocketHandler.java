package server.websocket;

import chess.ChessPosition;
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
import chess.ChessMove;

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
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove(), session);
            case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
            case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
        }
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException {
        // connect to a specific game
        connections.add(authToken, gameID, session);

        // load the game
        GameData game = gameService.load(gameID);

        var message = String.format("User joined the game: " + gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, authToken, notification);
    }

    private void makeMove(String authToken, Integer gameID, ChessMove chessMove, Session session) throws IOException {
        // make a specific move in a specific game
        try {
            // find the current game
            GameData game = gameService.load(gameID);

            // update the game with the move
            gameService.makeMove(game, chessMove, authToken);

            // notify players about the move made
            ChessPosition startPos = chessMove.getStartPosition();
            ChessPosition endPos = chessMove.getEndPosition();
            var message = String.format("Move made: " + startPos + " to " + endPos);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, authToken, notification);

            // update game state for all players
            var load = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
            connections.broadcast(gameID, null, load);
        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
            // send error message to player who made invalid move instead of throwing error?
            var errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.sendMessage(authToken, errorMessage);
        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException {
        // leave a specific game
        connections.remove(authToken, gameID);
        var message = String.format("User left the game: " + gameID);
        var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(gameID, authToken, notification);
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        // resign from a specific game
        try {
            // update game to be over
            gameService.resignGame(gameID, authToken);

            // notify players about resignation
            connections.remove(authToken, gameID);
            var message = String.format("User resigned from the game: " + gameID);
            var notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, authToken, notification);
        } catch (Exception ex) {
//            throw new RuntimeException(e);
            var errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage());
            connections.sendMessage(authToken, errorMessage);
        }
    }
}
