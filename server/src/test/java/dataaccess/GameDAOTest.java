package dataaccess;

import chess.ChessGame;
import dataaccess.sql.SQLGameDAO;
import model.GameData;
import org.junit.jupiter.api.*;

public class GameDAOTest {

    private static SQLGameDAO gameDAO;

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        gameDAO = new SQLGameDAO();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    @DisplayName("Should successfully create a new game")
    void successfulCreateGame() throws DataAccessException {
        ChessGame game = new ChessGame();

        // create gamedata for testing
        GameData gameData = new GameData(0, "whiteUser", "blackUser", "testGame", game);

        GameData createdGame = gameDAO.createGame(gameData);

        // verify exception isn't thrown (game is successfully created)
//        Assertions.assertDoesNotThrow(() -> gameDAO.createGame(gameData));

        // verify created game matches input data
        GameData retrievedGame = gameDAO.getGame(createdGame.gameID());

        Assertions.assertAll(
                () -> Assertions.assertEquals(gameData.gameName(), retrievedGame.gameName()),
                () -> Assertions.assertEquals(gameData.whiteUsername(), retrievedGame.whiteUsername()),
                () -> Assertions.assertEquals(gameData.blackUsername(), retrievedGame.blackUsername())
        );
        Assertions.assertEquals(gameData.gameName(), retrievedGame.gameName());
        Assertions.assertEquals(gameData.whiteUsername(), retrievedGame.whiteUsername());
        Assertions.assertEquals(gameData.blackUsername(), retrievedGame.blackUsername());
    }

    @Test
    @DisplayName("Should fail to retrieve a game that doesn't exist")
    void failedGetGameInvalidID() throws DataAccessException {
        // try to get a game that doesn't exist
        GameData retrievedGame = gameDAO.getGame(781);
        Assertions.assertNull(retrievedGame, "Expected null for game that doesn't exist");
    }
}
