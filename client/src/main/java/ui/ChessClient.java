package ui;

import model.AuthData;
import model.GameData;
import model.ResponseException;
import server.ServerFacade;

import java.util.Arrays;

public class ChessClient {

    private final ServerFacade server;
//    private final String serverUrl;
    // stores authData after logging in or registering
    private AuthData sessionAuthData;

    public ChessClient(String serverUrl) {
        // initialize connection to the server
        this.server = new ServerFacade(serverUrl);
//        this.serverUrl = serverUrl;
    }

    // way to process user input and send it to applicable ui
    public String eval(String input) {
        try {
            if (sessionAuthData == null) {
                // user is logged in, use PreLoginUI
                return new PreLoginUI(this, server).eval(input);
            } else {
                // user is not logged in, use PostLoginUI
                return new PostLoginUI(this, server).eval(input);
            }

        } catch (ResponseException e) {
            return "Error: problem processing user input " + e.getMessage();
        }
    }

    public void setAuthData(AuthData authData) {
        // when session starts, set the authdata
        this.sessionAuthData = authData;
    }

    public AuthData getAuthData() {
        return sessionAuthData;
    }

    public void logout() {
        // clear the authData when user logs out
        this.sessionAuthData = null;
    }
}
