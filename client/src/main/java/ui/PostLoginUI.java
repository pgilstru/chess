package ui;

import chess.ChessBoard;
import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import model.ResponseException;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PostLoginUI {

    private final ChessClient chessClient;
    private final ServerFacade server;
    private final String serverUrl;
    private Integer currGameID;

    public PostLoginUI(ChessClient chessClient, ServerFacade server, String serverUrl) {
        this.chessClient = chessClient;
        this.server = server;
        this.serverUrl = serverUrl;
        this.currGameID = null;
    }

    // process user commands
    public String eval(String input) {
        try {
            var tokens = input.trim().split(" ");
            if (tokens.length == 0) {
                // input is empty, call help
                return help();
            }

            // make command lowercase to standardize the type of input we receive
            var cmd = tokens[0].toLowerCase();

            // parameters to pass in for command methods
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            // user is authenticated, use these commands
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
        String authToken = server.getAuthToken();
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
        // adds user to the game as long as parameters (gameID, playerColor) were provided
        if (params.length != 2) {
            return "Expected: <ID> [WHITE|BLACK]";
        }

        try {
            int gameID = Integer.parseInt(params[0]);
            ChessGame.TeamColor playerColor = ChessGame.TeamColor.valueOf(params[1].toUpperCase());

            System.out.println("\nJoining game:");
            System.out.println("Game ID: " + gameID);
            System.out.println("Requested color: " + playerColor);

            // add user to the specified game, sending join req to server
            JoinRequest joinRequest = new JoinRequest(playerColor, gameID);
            String authToken = server.getAuthToken();
            server.joinGame(joinRequest, authToken);

            chessClient.connectGameplayUI(playerColor, gameID);

            return String.format("You successfully joined the game! gameID: " + gameID);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "gameID must be a valid number");
        } catch (ResponseException e) {
//            throw new RuntimeException("Must provide a valid gameID");
            throw e;
        } catch (Exception e) {
            throw new ResponseException(400, "Error joining game: " + e.getMessage());
        }
    }

    private String observeGame(String... params) throws ResponseException {
        // allow user to observe a game as long as it is specified in the parameters (gameID)
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <ID>");
        }

        try {
            int gameID = Integer.parseInt(params[0]);

            GameData game = getGame(gameID);

            // verify game exists
            if (game == null) {
                throw new ResponseException(400, "Game wasn't found: " + gameID);
            }

            // add user to the specified game
            AuthData authData = chessClient.getAuthData();
            String authToken = authData.authToken();

            if (authToken == null) {
                throw new ResponseException(401, "Must be authenticated to observe a game");
            }

            // set color to null to show they are an observer not a player
            ChessGame.TeamColor playerColor = null;

            // transition to gameplay
            chessClient.connectGameplayUI(playerColor, gameID);


            return String.format("You are successfully observing the game! gameID: " + gameID);
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "GameID must be a valid number");
        } catch (ResponseException e) {
//            throw new RuntimeException("Must provide a valid gameID");
            throw e;
        }
    }

    private GameData getGame(int gameID) throws ResponseException {
        try {
            List<GameData> games = server.listGames();
            if (games == null || games.isEmpty()) {
                throw new ResponseException(400, "no games are available");
            }

            for (var game : games) {
                if (game.gameID() == gameID) {
                    return game;
                }
            }

            throw new ResponseException(400, "game not found: " + gameID);
        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException(500, "Error when searching for game: " + e.getMessage());
        }
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
