package ui.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import model.GameData;
import model.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.GameSerializer;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@ClientEndpoint
public class WebSocketFacade extends Endpoint {
    private final String url;
    private Session session;
    private final NotificationHandler notificationHandler;
    private final Gson gson;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        this.url = url;
        this.notificationHandler = notificationHandler;

        this.gson = new GsonBuilder().registerTypeAdapter(UserGameCommand.class, new GameSerializer())
                .registerTypeAdapter(ServerMessage.class, new ServerDeserializer()).create();
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.session = session;
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            ServerMessage notification = gson.fromJson(message, ServerMessage.class);
            notificationHandler.notify(notification);
        } catch (Exception e) {
            System.out.println("Error processing the ws message: " + e.getMessage());
        }
    }

    public void connectToGame(String authToken, Integer gameID) throws ResponseException {
        // user makes a ws connection as a player or observer
        try {
            // change the url to use websocket 'ws'
            String wsUrl = url.replace("http", "ws");

            // use the 'ws' endpoint as well
            URI uri = new URI(wsUrl + "/ws");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            container.connectToServer(this, uri);

            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            sendMessage(command);
        } catch (IOException | URISyntaxException | DeploymentException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(String authToken, Integer gameID) throws ResponseException {
        // tells server you are leaving the game so it won't send you notifications
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
        // used to request to make a move in game
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);

            // add the move to the command
            command.setMove(move);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resignGame(String authToken, Integer gameID) throws ResponseException {
        // forfeits the match and ends the game so no more moves can be made
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void sendMessage(UserGameCommand command) throws IOException {
        // verify session exists and is open
        if (session != null && session.isOpen()) {
            // if it is, send the message
            String message = gson.toJson(command);
            session.getBasicRemote().sendText(message);
        } else {
            throw new IOException("Websocket session isn't open");
        }
    }

    private static class ServerDeserializer implements JsonDeserializer<ServerMessage> {
        // needed this for some reason, only thing that fixed some things when debugging
        @Override
        public ServerMessage deserialize(JsonElement json, java.lang.reflect.Type typeOf, JsonDeserializationContext context) {
            JsonObject jsonObject = json.getAsJsonObject();

            // get the type of server message
            String type = jsonObject.get("serverMessageType").getAsString();
            ServerMessage.ServerMessageType messageType = ServerMessage.ServerMessageType.valueOf(type);

            String message = null; // sets default to null

            if (jsonObject.has("message")) {
                // if there is a message, extract it
                message = jsonObject.get("message").getAsString();
            }

            String errorMessage = null;
            if (jsonObject.has("errorMessage")) {
                // if there is an error message, extract it
                errorMessage = jsonObject.get("errorMessage").getAsString();
            }

            // convert nested json into GameData
            GameData game = null;
            if (jsonObject.has("game")) {
                game = context.deserialize(jsonObject.get("game"), GameData.class);
            }

            return new ServerMessage(messageType, message, errorMessage, game);
        }
    }
}