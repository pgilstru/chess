package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.GameData;
import model.ResponseException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {

    private static MemoryUserDAO userDAO;
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static ClearService clearService;

    @BeforeAll
    public static void beforeAll() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @BeforeEach
    public void clear() throws ResponseException, DatabaseUnavailableException {
        // clear everything before running each clear test (ironic)
        new ClearService(userDAO, authDAO, gameDAO).clear();
    }

    @Test
    public void successfulClear() throws DataAccessException {
        // tests that clearing data works
        // add data to clear first
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");
        userDAO.createUser(userData);
        AuthData authData = new AuthData("some-auth-token", "testUser");
        authDAO.createAuth(authData);
        GameData gameData = new GameData(123, "testUser", null, "test game", new ChessGame());
        gameDAO.createGame(gameData);

        // attempt to clear all the data
        clearService.clear();

        // verify everything is clear
        Assertions.assertNull(userDAO.getUser("testUser"));
        Assertions.assertNull(authDAO.getAuth("some-auth-token"));

        // note: bc list is an array, you can't say null, or you will get false bc technically it is []
        // so you must check for is empty
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    public void failedClearDbUnavail() {
        // tests for the database being unavailable
        // "simulate" an error with the database by sending null data
        clearService = new ClearService(null, authDAO, gameDAO);

        // verify a DatabaseUnavailableException is thrown
        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
                () -> clearService.clear());
        Assertions.assertEquals("Database error", thrown.getMessage());
    }
}
