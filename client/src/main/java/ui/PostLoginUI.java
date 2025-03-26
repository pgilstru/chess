package ui;

import model.GameData;
import model.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

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
                case "create" -> createGame();
                case "list" -> listGames();
                case "join" -> joinGame();
                case "observe" -> observeGame();
                case "quit" -> quit();
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: problem processing user commands in PreLogin " + e.getMessage();
        }
    }



    private String help() {
        // display list of available commands the user can use/actions they can take
        return """
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
