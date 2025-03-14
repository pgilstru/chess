package dataaccess.sql;

import chess.piece.*;
import com.google.gson.Gson;
import chess.ChessGame;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.UserData;
import chess.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final Gson serializer;
    // constructor to initialize dao and configure DB
    public SQLGameDAO() throws DataAccessException {
        this.serializer = createSerializer();
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        // clear all userData in the userData table, but not the table itself
        var statement = "DELETE FROM games";
        executeUpdate(statement);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        // retrieve gameData given a gameID from the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID=?";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind the username parameter
                prepStatement.setInt(1, gameID);

                try (var rs = prepStatement.executeQuery()) {
                    if (rs.next()) {
                        // return userData object from the result set
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to get gameData " + ex.getMessage());
        }
        // no matching auth token found, return null
        return null;
    }

    @Override
    public GameData createGame(GameData gameData) throws DataAccessException {
        // Create a new user in the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind gameID
                prepStatement.setInt(1, gameData.gameID());

                // bind whiteUsername
                prepStatement.setString(2, gameData.whiteUsername());

                // bind blackUsername
                prepStatement.setString(3, gameData.blackUsername());

                // bind gameName
                prepStatement.setString(4, gameData.gameName());

                // bind game (first store it as json)
                prepStatement.setString(5, serializer.toJson(gameData.game()));

                prepStatement.executeUpdate();

                try (var rs = prepStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        int gameID = rs.getInt(1);
                        return new GameData(gameID, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create user " + ex.getMessage());
        }
        throw new DataAccessException("error: game creation failed");
    }

    @Override
    public List<GameData> listGames() throws DataAccessException{
        // Retrieve all games.
        List<GameData> list = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
            try (var prepStatement = conn.prepareStatement(statement)) {
                try (var rs = prepStatement.executeQuery()) {
                    while (rs.next()) {
                        list.add(readGame(rs));
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException("Error: unable to list games " + ex.getMessage());
        }
        return list;
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException{
        // update an existing game in the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
            try (var prepStatement = conn.prepareStatement(statement)) {
                prepStatement.setString(1, gameData.whiteUsername());
                prepStatement.setString(2, gameData.blackUsername());
                prepStatement.setString(3, gameData.gameName());
                prepStatement.setString(4, serializer.toJson(gameData.game()));
                prepStatement.setInt(5, gameData.gameID());

                int rowsUpdated = prepStatement.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Error: game doesn't exist");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to update game " + ex.getMessage());
        }
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        // connects given result set to a GameData object
        int gameID = rs.getInt("gameID");
        String whiteUsername = rs.getString("whiteUsername");
        String blackUsername = rs.getString("blackUsername");
        String gameName = rs.getString("gameName");

        // deserialize the chess game
        ChessGame game = serializer.fromJson((rs.getString("game")), ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, game);
    }

    private void executeUpdate(String statement) throws DataAccessException {
        // helps with executing sql update statements
        try (var conn = DatabaseManager.getConnection()) {
            try (var prepStatement = conn.prepareStatement(statement)) {
                prepStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: updating database " + ex.getMessage());
        }
    }

    // array of sql statements responsible for setting up the DB and its tables
    private final String[] createStatements = {
            // handle creating gameData table if it doesn't already exist
            """
            CREATE TABLE IF NOT EXISTS games (
            `gameID` int NOT NULL,
            `whiteUsername` varchar(255) NOT NULL,
            `blackUsername` varchar(255) NOT NULL,
            `gameName` varchar(255) NOT NULL,
            `game` TEXT NOT NULL,
            PRIMARY KEY (gameID),
            INDEX(whiteUsername),
            INDEX(blackUsername),
            INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException {
        // ensure DB exists by attempting to create it
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            // execute createStatements CREATE TABLE statement
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: creating tables " + ex.getMessage());
        }
    }

    private static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ChessPiece.class,
                (JsonDeserializer<ChessPiece>) (el, type, ctx) -> {
                    ChessPiece chessPiece = null;
                    if (el.isJsonObject()) {
                        String pieceType = el.getAsJsonObject().get("type").getAsString();
                        switch (ChessPiece.PieceType.valueOf(pieceType)) {
                            case PAWN -> chessPiece = ctx.deserialize(el, PawnMovesCalculator.class);
                            case ROOK -> chessPiece = ctx.deserialize(el, RookMovesCalculator.class);
                            case KNIGHT -> chessPiece = ctx.deserialize(el, KnightMovesCalculator.class);
                            case BISHOP -> chessPiece = ctx.deserialize(el, BishopMovesCalculator.class);
                            case QUEEN -> chessPiece = ctx.deserialize(el, QueenMovesCalculator.class);
                            case KING -> chessPiece = ctx.deserialize(el, KingMovesCalculator.class);
                        }
                    }
                    return chessPiece;
                });

        return gsonBuilder.create();
    }
}
