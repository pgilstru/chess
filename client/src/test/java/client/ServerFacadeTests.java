package client;

import chess.ChessGame;
import dataaccess.DataAccessException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() {
        // clear before each test
        facade.clearDB();
        facade.setAuthToken(null);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    void registerSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(userData);
        Assertions.assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerFailUsernameTaken() throws Exception {
        // register og user
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(userData);
        facade.logout(authData.authToken());

        UserData userData2 = new UserData("player1", "otherpassword", "p2@email.com");
        // attempt to register another user with same username (should fail)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(userData2);
        });
    }

    @Test
    void loginSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(userData);

        facade.logout(authData.authToken());

        AuthData authDataNew = facade.login(userData);
        Assertions.assertNotNull(authDataNew);
        Assertions.assertTrue(authDataNew.authToken().length() > 10);
    }

    @Test
    void loginFailInvalidPw() throws Exception {
        // register og user
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(userData);
        facade.logout(authData.authToken());

        UserData userData2 = new UserData("player1", "wrongpassword", "p1@email.com");

        // attempt to log in with wrong password (should fail)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(userData2);
        });
    }

    @Test
    void createGameSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        facade.register(userData);
        AuthData authData = facade.login(userData);
        facade.setAuthToken(authData.authToken());

        GameData gameData = new GameData(0, null, null, "game", null);
//
//        Assertions.assertThrows(ResponseException.class, () -> {
        GameData newGame = facade.createGame(gameData);
//        });
        Assertions.assertNotNull(gameData);
        Assertions.assertEquals("game", gameData.gameName());
    }

    @Test
    void createGameFailInvalidGameData() throws Exception {
        // attempt to create a game without logging in (should fail)
        //GameData gameData = new GameData(0, null, null, "game", null);
//        String authToken = "nonexistentAuthToken";
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame(null);
        });
    }

    @Test
    void listGamesSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        facade.register(userData);
        AuthData authData = facade.login(userData);
        String authToken = authData.authToken();

        GameData gameData = new GameData(0, null, null, "game", null);
        GameData gameData2 = new GameData(1, null, null, "game", null);

        facade.createGame(gameData);
        facade.createGame(gameData2);

        List<GameData> games = facade.listGames();
        Assertions.assertNotNull(games);
        Assertions.assertEquals(2, games.size());
    }

    @Test
    void listGamesFailNotAuthorized() throws Exception {
        // attempt to list games without logging in (should fail)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames();
        });
    }

    @Test
    void joinGameSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.login(userData);
        facade.setAuthToken(authData.authToken());

        GameData game = facade.createGame(new GameData(0, null, null, "game", null));

        JoinRequest joinRequest = new JoinRequest(ChessGame.TeamColor.WHITE, game.gameID());
        facade.joinGame(joinRequest);

        List<GameData> games = facade.listGames();
        Assertions.assertEquals("player1", games.getFirst().whiteUsername());
    }

    @Test
    void joinGameFailNotAuthorized() throws Exception {
        // attempt to join a game without logging in (should fail)
//        JoinRequest joinRequest = new JoinRequest(ChessGame.TeamColor.WHITE, 1);
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.joinGame(new JoinRequest(ChessGame.TeamColor.WHITE, 1));
        });
    }

    @Test
    void logoutSuccess() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = facade.register(userData);

        facade.logout(authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames();
        });
    }

    @Test
    void logoutFailNotAuthorized() throws Exception {
        // attempt to log out without logging in (should fail)
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.logout(null);
        });
    }
}
