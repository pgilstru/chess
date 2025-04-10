package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
//    ServerMessageType serverMessageType;
    private final ServerMessageType serverMessageType;
    private final String message;
    private final String errorMessage;
    private final GameData game;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
        this.message = null;
        this.errorMessage = null;
        this.game = null;
    }

    public ServerMessage(ServerMessageType type, String message) {
        this.serverMessageType = type;
        this.message = message;
        this.errorMessage = null;
        this.game = null;
    }

    public ServerMessage(ServerMessageType type, GameData gameData) {
        this.serverMessageType = type;
        this.message = null;
        this.errorMessage = null;
        this.game = gameData;
    }

    public ServerMessage(ServerMessageType type, String message, String errorMessage, GameData gameData) {
        this.serverMessageType = type;
        this.message = message;
        this.errorMessage = errorMessage;
        this.game = gameData;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public GameData getGame() {
        return game;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }

    @Override
    public String toString() {
//        return super.toString();
        return "ServerMessage{" + "serverMessageType=" + serverMessageType +
                ", message='" + message + '\'' + ", errorMessage='" + errorMessage + '\'' +
                ", game=" + (game != null ? "GameData" : "null") + '}';
    }
}
