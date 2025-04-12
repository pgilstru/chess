package websocket.commands;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.util.Objects;

/**
 * Represents a command a user can send the server over a websocket
 *
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class UserGameCommand {

    private final CommandType commandType;

    private final String authToken;

    private final Integer gameID;

    private ChessMove move;
//    public static class Move{
//        private final ChessPosition start;
//        private final ChessPosition end;
//
//        public Move(ChessMove move) {
//            this.start = move.getStartPosition();
//            this.end = move.getEndPosition();
//        }
//    }
//    private Move move;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.commandType = commandType;
        this.authToken = authToken;
        this.gameID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return gameID;
    }

    public ChessMove getMove() {
        return move;
//        if (move != null) {
//            return new ChessMove(move.start, move.end, null);
//        }
//        return null;
    }

    public void setMove(ChessMove move) {
        this.move = move;
//        this.move = new Move(move);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGameCommand)) {
            return false;
        }
        UserGameCommand that = (UserGameCommand) o;
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }

    public static class Move{
        private final ChessPosition start;
        private final ChessPosition end;

        public Move(ChessPosition start, ChessPosition end) {
            this.start = start;
            this.end = end;
        }

        public ChessPosition getStart() {
            return start;
        }

        public ChessPosition getEnd() {
            return end;
        }
    }

    public static class GameSerializer implements JsonSerializer<UserGameCommand> {
        @Override
        public JsonElement serialize(UserGameCommand src, java.lang.reflect.Type type, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("commandType", src.getCommandType().name());
            jsonObject.addProperty("authToken", src.getAuthToken());
            jsonObject.addProperty("gameID", src.getGameID());

            if (src.getMove() != null) {
                JsonObject moveObj = new JsonObject();
                JsonObject start = new JsonObject();

                start.addProperty("row", src.getMove().getStartPosition().getRow());
                start.addProperty("col", src.getMove().getStartPosition().getColumn());

                JsonObject end = new JsonObject();
                end.addProperty("row", src.getMove().getEndPosition().getRow());
                end.addProperty("col", src.getMove().getEndPosition().getColumn());

                moveObj.add("start", start);
                moveObj.add("end", end);

                jsonObject.add("move", moveObj);
            }

            return jsonObject;
        }
    }
}
