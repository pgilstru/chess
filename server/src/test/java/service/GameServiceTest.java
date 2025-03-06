package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.memory.*;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class GameServiceTest {
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static GameService gameService;

    @BeforeAll
    public static void beforeAll() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        new ClearService(authDAO, gameDAO).clear();
    }
}
