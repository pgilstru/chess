package ui;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import model.ResponseException;
import server.ServerFacade;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PostLoginUI {

    private GameData gameData;
    private final ChessClient chessClient;
    private final ServerFacade server;

    public PostLoginUI(ChessClient chessClient, ServerFacade server) {
        this.chessClient = chessClient;
        this.server = server;
    }

    // process user commands
    public String eval(String input) {
        try {
            AuthData authData = chessClient.getAuthData();

            server.setAuthToken(authData.authToken());

            var tokens = input.trim().split(" ");
            if (tokens.length == 0) {
                // input is empty, call help
                return help();
            }

            // make command lowercase to standardize the type of input we receive
            var cmd = tokens[0].toLowerCase();

            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            // user not authenticated,
            return switch (cmd) {
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: problem processing user commands in PostLogin " + e.getMessage();
        }
    }

    private String logout() throws ResponseException{
        // attempt to log out the user
        String authToken = chessClient.getAuthData().authToken();
        server.logout(authToken);

        // update chessClient sessionAuthData to be null (aka logged out)
        chessClient.logout();

        return "You successfully logged out!";
    }

    private String listGames() throws ResponseException {
        // list all games that currently exist on the server
        List<GameData> games = server.listGames();
        if (games.isEmpty()) {
            // verify games exist
            return "No games to list yet";
        }

        // use stringbuilder to format our list for the ui
        StringBuilder result = new StringBuilder();

        int num = 1;

        for (GameData game : games) {
            String gameName = game.gameName();
            String whiteUser = (!Objects.equals(game.whiteUsername(), "") ? game.whiteUsername() : "No white user yet");
            String blackUser = (!Objects.equals(game.blackUsername(), "") ? game.blackUsername() : "No black user yet");

            // add each game to the result list string
            result.append(String.format(
                    num++ + ". Name: " + gameName + ", White: " + whiteUser + ", Black: " + blackUser + '\n'
            ));
        }

        return result.toString();
    }

    private String createGame(String... params) throws ResponseException {
        // create the game as long as a parameter (name) was provided
        if (params.length != 1) {
            throw new ResponseException(400, "Must provide one argument. Expected: <NAME>");
        }

        String gameName = params[0];

        // create game data (don't add user as a player, they must join)
        GameData gameData = new GameData(0, "", "", gameName, new ChessGame());

        AuthData authData = chessClient.getAuthData();
        String authToken = authData.authToken();
        // actually create the game in the server using the gameData
        server.createGame(gameData, authToken);

        return String.format("You successfully created the game! Game name: " + gameData.gameName());
    }

    private String joinGame(String... params) throws ResponseException {
        try {
            // adds user to the game as long as parameters (gameID, playerColor) were provided
            if (params.length != 2) {
                throw new ResponseException(400, "Expected: <ID> [WHITE|BLACK]");
            }

            int gameID = Integer.parseInt(params[0]);

            // verify game exists
            boolean exists = false;
            for (var game : server.listGames()) {
                if (game.gameID() == gameID) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                throw new ResponseException(400, "Must provide a valid gameID");
            }

            JoinRequest joinRequest = getJoinRequest(params, gameID);

            // add user to the specified game
            server.joinGame(joinRequest);

            // show the chessboard
            ChessBoard chessBoard = new ChessBoard();
            chessBoard.resetBoard();
            ChessGame.TeamColor color = joinRequest.playerColor();
            GameplayUI.drawChessboard(chessBoard, color);

            return String.format("You successfully joined the game! gameID: " + joinRequest.gameID());
        } catch (ResponseException e) {
            throw new RuntimeException("Must provide a valid gameID");
        }
    }

    private static JoinRequest getJoinRequest(String[] params, int gameID) {
        ChessGame.TeamColor playerColor;
        if (Objects.equals(params[1].toLowerCase(), "white")) {
            // provided color is white
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (Objects.equals(params[1].toLowerCase(), "black")) {
            // provided color is black
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new ResponseException(400, "Provided color must be either white or black");
        }

        return new JoinRequest(playerColor, gameID);
    }

    private String observeGame(String... params) throws ResponseException {
        // allow user to observe a game as long as it is specified in the parameters (gameID)
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <ID>");
        }

        int gameID = Integer.parseInt(params[0]);

        // implement functionality in next phase!

        return String.format("You are successfully observing the game! gameID: " + gameID);
    }

    private String help() {
        // display list of available commands the user can use/actions they can take
        return """
               Available commands:
               create <NAME> - to create a game
               list - to list all games
               join <ID> [WHITE|BLACK] - to join a game
               observe <ID> - to observe a game
               logout - to logout when you are done
               quit - to quit playing chess
               help - to get help with possible commands
               """;
    }
}
