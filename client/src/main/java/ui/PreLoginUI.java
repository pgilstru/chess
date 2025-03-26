package ui;

import model.AuthData;
import model.ResponseException;
import model.UserData;
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
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> quit();
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: problem processing user commands in PreLogin " + e.getMessage();
        }
    }

    private String register(String... params) throws ResponseException {
        // if all arguments provided, make them an account and transition to the PostLoginUI
        if (params.length < 3) {
            throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>");
        }

        // create new userData and register it
        UserData userData = new UserData(params[0], params[1], params[2]);
        AuthData authData = server.register(userData);

        // update chessClient sessionAuthData
        chessClient.setAuthData(authData);
        return String.format("You successfully registered! Logged in as: " + authData.username());
    }

    private String help() {
        // display list of available commands the user can use/actions they can take
        return """
               register <USERNAME> <PASSWORD> <EMAIL> - to create an account
               login <USERNAME> <PASSWORD> - to play chess
               quit - to quit playing chess
               help - to get help with possible commands
               
               """;
    }
}
