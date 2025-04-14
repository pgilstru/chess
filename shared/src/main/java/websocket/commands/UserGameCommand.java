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
    }

    public void setMove(ChessMove move) {
        this.move = move;
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
        return Objects.hash(getCommandType(), getAuthToken(), getGameID(), getMove());
    }

    public static class GameSerializer implements JsonSerializer<UserGameCommand>, JsonDeserializer<UserGameCommand> {
        @Override
        public JsonElement serialize(UserGameCommand src, Type type, JsonSerializationContext context) {
            // serializes a userGameCommand to json
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("commandType", src.getCommandType().name());
            jsonObject.addProperty("authToken", src.getAuthToken());
            jsonObject.addProperty("gameID", src.getGameID());

            // for make move commands
            if (src.getMove() != null) {
                JsonObject moveObj = new JsonObject();
                JsonObject start = new JsonObject();

                // serialize start
                start.addProperty("row", src.getMove().getStartPosition().getRow());
                start.addProperty("col", src.getMove().getStartPosition().getColumn());

                JsonObject end = new JsonObject();
                // serialize end
                end.addProperty("row", src.getMove().getEndPosition().getRow());
                end.addProperty("col", src.getMove().getEndPosition().getColumn());

                moveObj.add("startPosition", start);
                moveObj.add("endPosition", end);

                // adds move to the command json
                jsonObject.add("move", moveObj);
            }

            return jsonObject;
        }

        @Override
        public UserGameCommand deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            // deserializes json into a userGameCommand
            JsonObject jsonObject = json.getAsJsonObject();
            CommandType commandType = CommandType.valueOf(jsonObject.get("commandType").getAsString());
            String authToken = jsonObject.get("authToken").getAsString();
            Integer gameID = jsonObject.get("gameID").getAsInt();

            UserGameCommand command = new UserGameCommand(commandType, authToken, gameID);

            // check if the command is for making a move
            if (jsonObject.has("move")) {
                JsonObject moveObj = jsonObject.getAsJsonObject("move");
                JsonObject startObj = moveObj.getAsJsonObject("startPosition");
                JsonObject endObj = moveObj.getAsJsonObject("endPosition");

                int startRow = startObj.get("row").getAsInt();
                int startCol = startObj.get("col").getAsInt();
                int endRow = endObj.get("row").getAsInt();
                int endCol = endObj.get("col").getAsInt();

                // debug stuff
                System.out.println("Deserializing move - start: (" + startRow + "," + startCol + "), end: (" + endRow + "," + endCol + ")");

                // creates chessMove and adds it to the command
                ChessPosition start = new ChessPosition(startRow, startCol);
                ChessPosition end = new ChessPosition(endRow, endCol);

                command.setMove(new ChessMove(start, end, null));
            }

            return command;
        }
    }
}
