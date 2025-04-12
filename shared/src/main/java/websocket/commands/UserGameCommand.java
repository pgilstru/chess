package websocket.commands;

import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
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
        this.move = null;
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
//                Objects.equals(getGameID(), that.getGameID());
                Objects.equals(getGameID(), that.getGameID()) &&
                Objects.equals(getMove(), that.getMove());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getMove());
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

    public static class GameSerializer implements JsonSerializer<UserGameCommand>, JsonDeserializer<UserGameCommand> {
        @Override
        public JsonElement serialize(UserGameCommand src, Type type, JsonSerializationContext context) {
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

                moveObj.add("startPosition", start);
                moveObj.add("endPosition", end);

                jsonObject.add("move", moveObj);
            }

            return jsonObject;
        }
//
        @Override
        public UserGameCommand deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            CommandType commandType = CommandType.valueOf(jsonObject.get("commandType").getAsString());
            String authToken = jsonObject.get("authToken").getAsString();
            Integer gameID = jsonObject.get("gameID").getAsInt();

            UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);

            if (jsonObject.has("move")) {
                JsonObject moveObj = jsonObject.getAsJsonObject("move");
                JsonObject startObj = moveObj.getAsJsonObject("startPosition");
                JsonObject endObj = moveObj.getAsJsonObject("endPosition");

//                ChessPosition start = new ChessPosition(
//                        startObj.get("row").getAsInt(),
//                        startObj.get("col").getAsInt()
//                );
//                ChessPosition end = new ChessPosition(
//                        endObj.get("row").getAsInt(),
//                        endObj.get("col").getAsInt()
//                );

                int startRow = startObj.get("row").getAsInt();
                int startCol = startObj.get("col").getAsInt();
                int endRow = endObj.get("row").getAsInt();
                int endCol = endObj.get("col").getAsInt();

                System.out.println("Deserializing move - start: (" + startRow + "," + startCol + "), end: (" + endRow + "," + endCol + ")");

                ChessPosition start = new ChessPosition(startRow, startCol);
                ChessPosition end = new ChessPosition(endRow, endCol);

                command.setMove(new ChessMove(start, end, null));
            }

            return command;
        }
    }
}
