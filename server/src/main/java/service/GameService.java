package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinRequest;
import model.ResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private int gameIDCounter = 0;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // public CreateResult create(CreateRequest createRequest) {}
    public GameData create(GameData gameData, String authToken) {
        System.out.println("Inserting game with whiteUsername: " + gameData.whiteUsername());

        try {
            // verify user is authenticated first
            if (authDAO.getAuth(authToken) == null) {
                throw new IllegalArgumentException("Must be authenticated to create a game");
            }

            // verify input (gameName) isn't empty
            if (gameData.gameName() == null) {
                throw new IllegalArgumentException("Must provide a game name");
            }

            // create a new game
            String whiteUser = gameData.whiteUsername();
            String blackUser = gameData.blackUsername();
            String gameName = gameData.gameName();
            ChessGame chessGame = new ChessGame();

            GameData newGame = new GameData(0, whiteUser, blackUser, gameName, chessGame);
            return gameDAO.createGame(newGame);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // public JoinResult join(JoinRequest joinRequest) {}
    public void join(JoinRequest joinRequest, String authToken) {
        try {
            // verify user is authenticated first
            if (authDAO.getAuth(authToken) == null) {
                throw new ResponseException(401, "Must be authenticated to join a game");
            }

            // get the game and verify the game exists
            GameData game = gameDAO.getGame(joinRequest.gameID());
            if (game == null) {
                throw new IllegalArgumentException("Game not found");
            }

            String whiteUser = game.whiteUsername();
            String blackUser = game.blackUsername();

            if (Objects.equals(whiteUser, "")) {
                // change to null for error handling purposes below
                whiteUser = null;
            }

            if (Objects.equals(blackUser, "")) {
                // change to null for error handling purposes below
                blackUser = null;
            }

            // check if game is already full (has 2 players)
            if (whiteUser != null && blackUser != null) {
                throw new IllegalArgumentException("Game already has two players");
            }

            // verify a color was given
            if (joinRequest.playerColor() == null) {
                throw new IllegalArgumentException("Must provide a user color");
            }

            ChessGame.TeamColor reqColor = joinRequest.playerColor();

            // verify requested color is available
            if (reqColor == ChessGame.TeamColor.BLACK && blackUser != null) {
                throw new ResponseException(403, "Player for black team is already taken");
            }
            if (reqColor == ChessGame.TeamColor.WHITE && whiteUser != null) {
                throw new ResponseException(403, "Player for white team is already taken");
            }

            // add them to the game
            String username = authDAO.getAuth(authToken).username();

            if (reqColor == ChessGame.TeamColor.BLACK) {
                System.out.println("Assigning user " + username + " as Black in game " + game.gameID());
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            } else if (reqColor == ChessGame.TeamColor.WHITE) {
                // assign other user to black player
                System.out.println("Assigning user " + username + " as White in game " + game.gameID());
                game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            } else {
                // throw an error because color doesn't exist
                throw new IllegalArgumentException("not an accepted color");
            }

            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // public ListResult list(ListRequest listRequest) {}
    public List<GameData> list(String authToken) {
        try {
            // verify user is authenticated first
            if (authDAO.getAuth(authToken) == null) {
                throw new ResponseException(401, "Must be authenticated to list games");
            }

            // list all the games
            return new ArrayList<>(gameDAO.listGames());

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public GameData load(int gameID) {
        // load the specified game
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException ex) {
            throw new RuntimeException("couldn't load the game: " + ex.getMessage());
        }
    }
}
