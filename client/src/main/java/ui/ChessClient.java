package ui;

import model.AuthData;
import model.GameData;
import model.ResponseException;
import server.ServerFacade;

import java.util.Arrays;
import java.util.Objects;

public class ChessClient {

    private final ServerFacade server;
    private final String serverUrl;

    // stores authData after logging in or registering
    private AuthData sessionAuthData;

    public ChessClient(String serverUrl) {
        // initialize connection to the server
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
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
        server.setAuthToken(authData.authToken());
    }

    public AuthData getAuthData() {
        return sessionAuthData;
    }

    public void logout() {
        this.sessionAuthData = null;
        server.setAuthToken(null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessClient that)) {
            return false;
        }
        return Objects.equals(server, that.server) && Objects.equals(serverUrl, that.serverUrl) && Objects.equals(sessionAuthData, that.sessionAuthData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, serverUrl, sessionAuthData);
    }
}
