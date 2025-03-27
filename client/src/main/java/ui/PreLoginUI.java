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
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: problem processing user commands in PreLogin " + e.getMessage();
        }
    }

    private String register(String... params) throws ResponseException {
        try {
            // if all arguments provided, make them an account and transition to the PostLoginUI
            if (params.length < 3) {
                throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD> <EMAIL>\n");
            }

            // create new userData and register it
            String username = params[0];
            String password = params[1];
            String email = params[2];
            UserData userData = new UserData(username, password, email);

            AuthData authData = server.register(userData);

//        AuthData authData = server.login(userData);

            // update chessClient sessionAuthData
            chessClient.setAuthData(authData);
            return String.format("You successfully registered! Logged in as: " + authData.username());
        } catch (ResponseException e) {
            throw new RuntimeException("Username already taken.");
        }
    }

    private String login(String... params) throws ResponseException {
        // if all arguments provided, attempt to log them in
        try {
            if (params.length != 2) {
                throw new ResponseException(400, "Expected: <USERNAME> <PASSWORD>");
            }

            // create new userData and login user
            UserData userData = new UserData(params[0], params[1], null);
            // !!!! COME BACK TO THIS ^^^^

            AuthData authData = server.login(userData);

            // update chessClient sessionAuthData
            chessClient.setAuthData(authData);
            server.setAuthToken(authData.authToken());
            return String.format("You successfully logged in as: " + authData.username());
        } catch (ResponseException e) {
            throw new RuntimeException("Username or password is incorrect.");
        }
    }

    private String help() {
        // display list of available commands the user can use/actions they can take
        return """
               Available commands:
               register <USERNAME> <PASSWORD> <EMAIL> - to create an account
               login <USERNAME> <PASSWORD> - to play chess
               quit - to quit playing chess
               help - to get help with possible commands
               """;
    }
}
