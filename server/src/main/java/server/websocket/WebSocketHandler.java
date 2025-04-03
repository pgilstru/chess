package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import websocket.commands.UserGameCommand;

import java.io.IOException;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), command.getGameID());
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID());
            case LEAVE -> leave(command.getAuthToken(), command.getGameID());
            case RESIGN -> resign(command.getAuthToken(), command.getGameID());
        }
    }

    private void connect(String authToken, Integer gameID) {
        // connect to a specific game
        connections.add(authToken, gameID);
        var message = String.format()
    }
}
