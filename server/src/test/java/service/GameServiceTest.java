package service;

import chess.ChessGame;
import dataaccess.memory.*;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class GameServiceTest {
    private static MemoryUserDAO userDAO;
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static GameService gameService;

    @BeforeAll
    public static void beforeAll() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @BeforeEach
    public void clear() throws ResponseException, DatabaseUnavailableException {
        // clear everything before running each test
        new ClearService(userDAO, authDAO, gameDAO).clear();
    }

    @Test
    public void successfulCreate() throws ResponseException, DataAccessException {
        // tests what should be a successful creation of a game
        // authenticate the user
        AuthData authData = new AuthData("some-auth-token", "testUser");
        authDAO.createAuth(authData);

        // create a game
        GameData req = new GameData(123,null, null, "test game", new ChessGame());
        GameData res = gameService.create(req, authData.authToken());

        // verify game's successfully created
        List<GameData> gameDataList = gameService.list(authData.authToken());
        Assertions.assertEquals(1, gameDataList.size());

        // verify it's stored (in RAM or database) by checking if it's in the list by comparing things
        GameData gameData = gameDataList.getFirst(); // grab from DAO
        Assertions.assertEquals(res.gameID(), gameData.gameID());
        Assertions.assertEquals(req.gameName(), gameData.gameName()); // verify the name is the same
        Assertions.assertNull(gameData.whiteUsername()); // verify nobody was auto added as players without joining
        Assertions.assertNull(gameData.blackUsername());
        Assertions.assertNotNull(gameData.game()); // verify it populated the actual game
    }

    @Test
    public void failedCreateUnAuth() throws ResponseException {
        // test trying to create a game without being authorized... should fail
        // create a game
        GameData req = new GameData(123,null, null, "test game", new ChessGame());

        // see if trying to create a game unauthorized throws an exception
        try {
            gameService.create(req, null);
            Assertions.fail("Fail: successfully created a game unauthorized. Expected an IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assertions.assertEquals("Must be authenticated to create a game", e.getMessage());
        }
    }

    @Test
    public void successfulListGames() throws DataAccessException {
        // test what should be successfully listing games
        // authenticate the user
        AuthData authData = new AuthData("some-auth-token", "testUser");
        authDAO.createAuth(authData);

        // make an empty list to store expected games
        List<GameData> expected = new ArrayList<>();
        expected.add(gameService.create(new GameData(0, "brenner", null, "brens cool game", new ChessGame()), authData.authToken()));
        expected.add(gameService.create(new GameData(1, "cami", null, "cams cool game", new ChessGame()), authData.authToken()));
        expected.add(gameService.create(new GameData(2, "luke", null, "lukes cool game", new ChessGame()), authData.authToken()));

        // try to list the games
        var actual = gameService.list(authData.authToken());

        // verify it worked
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    public void failedListGamesUnAuth() {
        // try to list games without being authenticated (should fail)

        // see if trying to list a game unauthorized throws an exception
        ResponseException thrown = Assertions.assertThrows(ResponseException.class,
                () -> gameService.list(null));
        Assertions.assertEquals("Must be authenticated to list games", thrown.getMessage());
    }

    @Test
    public void successfulJoinGame() throws DataAccessException {
        // tests what should be a successful joining of a game
        // authenticate the user
        AuthData authData = new AuthData("some-auth-token", "testUser");
        authDAO.createAuth(authData);

        // create a game
        GameData gameData = gameService.create(new GameData(123,null, null, "test game", new ChessGame()), authData.authToken());

        // attempt joining the game as white
        JoinRequest req = new JoinRequest(ChessGame.TeamColor.WHITE, gameData.gameID());
        Assertions.assertDoesNotThrow(() -> gameService.join(req, authData.authToken()));

        // verify the user was added
        GameData newGame = gameDAO.getGame(gameData.gameID());
        Assertions.assertEquals("testUser", newGame.whiteUsername());
        Assertions.assertNull(newGame.blackUsername());
    }

    @Test
    public void failedJoinGameGameNotFound() throws DataAccessException {
        // tests joining a game that doesn't exist (should fail)
        // authenticate the user (so test doesn't give us a not authenticated error instead)
        AuthData authData = new AuthData("some-auth-token", "testUser");
        authDAO.createAuth(authData);

        JoinRequest req = new JoinRequest(ChessGame.TeamColor.BLACK, 222);

        // see if trying to join a non-existent game throws an exception
        Assertions.assertThrows(ResponseException.class, () -> gameService.join(req, "Game not found"));
    }
}
