package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
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

    public boolean isGoodAuthToken(String authToken) {
        try {
            return authDAO.getAuth(authToken) != null;
        } catch (DataAccessException e) {
            return false;
        }
    }

    public String getUsername(String authToken) throws ResponseException {
        try {
            var authData = authDAO.getAuth(authToken);
            if (authData == null) {
                throw new ResponseException(401, "Invalid auth token");
            }

            return authData.username();
        } catch (DataAccessException e) {
            throw new ResponseException(500, "Error when accessing authData");
        }
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

            if (Objects.equals(whiteUser, "") || Objects.equals(whiteUser, "No white user yet") ||
                    Objects.equals(whiteUser, "white")) {
                // change to null for error handling purposes below
                whiteUser = null;
            }

            if (Objects.equals(blackUser, "") || Objects.equals(blackUser, "No black user yet") ||
                    Objects.equals(blackUser, "black")) {
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

    public void makeMove(int gameID, ChessMove chessMove, String authToken) {
        try {
            // verify user is authorized
            if (authDAO.getAuth(authToken) == null) {
                throw new ResponseException(401, "Must be authenticated");
            }

            // get current game's state and make sure it exists
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                throw new IllegalArgumentException("Game not found");
            }

            // get the chess game
            ChessGame chessGame = gameData.game();

            // verify game is not over
            if (chessGame.isGameOver()) {
                throw new ResponseException(400, "Game is over");
            }

            // verify the player can make a move (it's their turn)
            String username = authDAO.getAuth(authToken).username();
            ChessGame.TeamColor turn = chessGame.getTeamTurn();

            if ((turn == ChessGame.TeamColor.WHITE && !username.equals(gameData.whiteUsername())) ||
                    (turn == ChessGame.TeamColor.BLACK && !username.equals(gameData.blackUsername()))) {
                throw new ResponseException(403, "It isn't your turn yet, move not made");
            }

            // verify move is actually valid
            if (!chessGame.validMoves(chessMove.getStartPosition()).contains(chessMove)) {
                throw new ResponseException(400, "Invalid move");
            }

            // make the move
            chessGame.makeMove(chessMove);

            // check if the game should be over
            if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                chessGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                chessGame.setGameOver(true);
            }

            // update game in DB
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error making move: " + e.getMessage());
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }

    public void resignGame(int gameID, String authToken) {
        try {
            // verify user is authenticated
            if (authDAO.getAuth(authToken) == null) {
                throw new ResponseException(401, "Must be authenticated");
            }

            // get current game's state and make sure it exists
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                throw new IllegalArgumentException("Game not found");
            }

            // verify player is in the game
            String username = authDAO.getAuth(authToken).username();
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                throw new ResponseException(403, "You aren't a player in this game");
            }

            // get the chess game
            ChessGame chessGame = gameData.game();

            // verify game is not over
            if (chessGame.isGameOver()) {
                throw new ResponseException(400, "Game is already over");
            }

            chessGame.setGameOver(true);

            // update game in db
            gameDAO.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error resigning from game: " + e.getMessage());
        }
    }

    public boolean isInCheck(int gameID, ChessGame.TeamColor playerColor) {
        // check if the players team is in check
        try {
            GameData gameData = gameDAO.getGame(gameID);

            // verify gameData isn't null/empty
            if (gameData == null) {
                throw new IllegalArgumentException("Game not found");
            }

            return gameData.game().isInCheck(playerColor);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking game: " + e.getMessage());
        }
    }

    public boolean isInCheckmate(int gameID, ChessGame.TeamColor playerColor) {
        // check if the players team is in checkmate
        try {
            GameData gameData = gameDAO.getGame(gameID);

            // verify gameData isn't null/empty
            if (gameData == null) {
                throw new IllegalArgumentException("Game not found");
            }

            return gameData.game().isInCheckmate(playerColor);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking game: " + e.getMessage());
        }
    }

    public boolean isInStalemate(int gameID, ChessGame.TeamColor playerColor) {
        // check if the players team is in stalemate
        try {
            GameData gameData = gameDAO.getGame(gameID);

            // verify gameData isn't null/empty
            if (gameData == null) {
                throw new IllegalArgumentException("Game not found");
            }

            return gameData.game().isInStalemate(playerColor);
        } catch (DataAccessException e) {
            throw new RuntimeException("Error checking game: " + e.getMessage());
        }
    }
}
