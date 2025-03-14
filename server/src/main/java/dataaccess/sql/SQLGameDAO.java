package dataaccess.sql;

import chess.piece.*;
import com.google.gson.Gson;
import chess.ChessGame;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import chess.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLGameDAO implements GameDAO {
    private final Gson serializer;
    // constructor to initialize dao and configure DB
    public SQLGameDAO() throws DataAccessException {
        this.serializer = createSerializer();
        // array of sql statements responsible for setting up the DB and its tables
        String[] createStatements = {
            // handle creating gameData table if it doesn't already exist
            """
            CREATE TABLE IF NOT EXISTS games (
            `gameID` int NOT NULL AUTO_INCREMENT,
            `whiteUsername` varchar(255),
            `blackUsername` varchar(255),
            `gameName` varchar(255) NOT NULL,
            `game` TEXT NOT NULL,
            PRIMARY KEY (gameID)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        DatabaseManager.configureDatabase(createStatements);
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
            var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
            try (var prepStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
                String whiteUser = gameData.whiteUsername();
                String blackUser = gameData.blackUsername() != null ? gameData.blackUsername() : null;
                System.out.println(blackUser);

                // bind whiteUsername
                prepStatement.setString(1, whiteUser);
                System.out.println(gameData.whiteUsername());
                System.out.println(whiteUser);

                // bind blackUsername
                prepStatement.setString(2, blackUser);

                // bind gameName
                prepStatement.setString(3, gameData.gameName());

                // bind game (first store it as json)
                prepStatement.setString(4, serializer.toJson(gameData.game()));

                int affectedRows = prepStatement.executeUpdate();
                if (affectedRows > 0) {
                    var rs = prepStatement.getGeneratedKeys();
                    if (rs.next()) {
                        int gameID = rs.getInt(1);
                        return new GameData(gameID, whiteUser, blackUser, gameData.gameName(), gameData.game());
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create game " + ex.getMessage());
        }
        throw new DataAccessException("error: game creation failed");
    }

    @Override
    public List<GameData> listGames() throws DataAccessException{
        // Retrieve all games.
        List<GameData> list = new ArrayList<>();
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

    private static Gson createSerializer() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(ChessPiece.class,
                (JsonDeserializer<ChessPiece>) (el, type, ctx) -> {
                    ChessPiece chessPiece = null;
                    if (el.isJsonObject()) {
                        String pieceType = el.getAsJsonObject().get("type").getAsString();
                        ChessGame.TeamColor pieceColor = ChessGame.TeamColor.valueOf(el.getAsJsonObject().get("pieceColor").getAsString());

                        switch (pieceType) {
                            case "PAWN":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.PAWN);
                                break;
                            case "ROOK":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.ROOK);
                                break;
                            case "KNIGHT":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.KNIGHT);
                                break;
                            case "BISHOP":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.BISHOP);
                                break;
                            case "KING":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.KING);
                                break;
                            case "QUEEN":
                                chessPiece = new ChessPiece(pieceColor, ChessPiece.PieceType.QUEEN);
                                break;
                            default:
                        }
                    }
                    return chessPiece;
                });

        gsonBuilder.registerTypeAdapter(PieceMovesCalculator.class,
                (JsonDeserializer<PieceMovesCalculator>) (el, type, ctx) -> {
                    if (el.isJsonObject()) {
                        String pieceType = el.getAsJsonObject().get("type").getAsString();

                        switch (pieceType) {
                            case "PAWN":
                                return ctx.deserialize(el, PawnMovesCalculator.class);
                            case "ROOK":
                                return ctx.deserialize(el, RookMovesCalculator.class);
                            case "KNIGHT":
                                return ctx.deserialize(el, KnightMovesCalculator.class);
                            case "BISHOP":
                                return ctx.deserialize(el, BishopMovesCalculator.class);
                            case "KING":
                                return ctx.deserialize(el, KingMovesCalculator.class);
                            case "QUEEN":
                                return ctx.deserialize(el, QueenMovesCalculator.class);
                            default:
                        }
                    }
                    return null;
                });
        return gsonBuilder.create();
    }
}
