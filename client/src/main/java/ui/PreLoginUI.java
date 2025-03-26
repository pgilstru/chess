package ui;

import model.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class PreLoginUI {

    private final ChessClient chessClient;
    private final ServerFacade server;

    public PreLoginUI(ChessClient chessClient, ServerFacade server) {
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

            // parameters to pass in for command methods
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            // user not authenticated,
            return switch (cmd) {
                case "help" -> help();
                case "quit" -> quit();
                case "login" -> login(params);
                case "register" -> register(params);
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: problem processing user commands in PreLogin " + e.getMessage();
        }
    }

    private String help() {
        // display list of available commands the user can use/actions they can take
        return """
               register <USERNAME> <PASSWORD> <EMAIL> - to create an account
               login <USERNAME> <PASSWORD> - to play chess
               quit - playing chess
               help - with possible commands
               
               """;
    }
}
