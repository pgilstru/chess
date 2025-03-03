package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.JoinRequest;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // public CreateResult create(CreateRequest createRequest) {}
    public GameData create(GameData gameData, String authToken) {
        try {
            // verify user is authenticated first
            if (authDAO.getAuth(authToken) == null) {
                throw new IllegalArgumentException("Must be authenticated to logout");
            }

            // verify input (gameName) isn't empty
            if (gameData.gameName() == null) {
                throw new IllegalArgumentException("Must provide a game name");
            }

            // create a new game
            GameData newGame = new GameData(0, null, null, gameData.gameName(), new ChessGame());
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
                throw new IllegalArgumentException("Must be authenticated to logout");
            }

            // get the game and verify the game exists
            GameData game = gameDAO.getGame(joinRequest.gameID());
            if (game == null) {
                throw new IllegalArgumentException("Game not found");
            }

            // verify a color was given
            if (joinRequest.userColor() == null) {
                throw new IllegalArgumentException("Must provide a user color");
            }

            ChessGame.TeamColor reqColor = joinRequest.userColor();
            // add them to the game
            String username = authDAO.getAuth(authToken).username();
            if (reqColor == ChessGame.TeamColor.BLACK) {
                // add them as the black piece player
                game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            } else if (reqColor == ChessGame.TeamColor.WHITE) {
                // add them as the white piece player
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
}
