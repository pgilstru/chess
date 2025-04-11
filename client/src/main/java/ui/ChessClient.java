package ui;

import chess.ChessMove;
import model.AuthData;
import model.ResponseException;
import server.ServerFacade;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import java.util.Objects;

public class ChessClient {

    private final ServerFacade server;
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private GameplayUI gameplayUI;

    // stores authData after logging in or registering
    private AuthData sessionAuthData;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        // initialize connection to the server
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        this.gameplayUI = null;
    }

    // way to process user input and send it to applicable ui
    public String eval(String input) {
        try {
            if (sessionAuthData == null) {
                // user is logged in, use PreLoginUI
                return new PreLoginUI(this, server).eval(input);
            } else if (gameplayUI != null) {
                String result = gameplayUI.eval(input);

                // if result shows you left the game, go back to the postLoginUI
                if (result.contains("left the game")) {
                    clearGameplayUI();
                }
                return result;
            } else {
                // user is not logged in, use PostLoginUI
                return new PostLoginUI(this, server, serverUrl, notificationHandler).eval(input);
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

    public void setGameplayUI(GameplayUI gameplayUI) {
        this.gameplayUI = gameplayUI;
    }

    public void clearGameplayUI() {
        this.gameplayUI = null;
    }

    public void logout() {
        if (gameplayUI != null) {
            try {
                gameplayUI.leave();
            } catch (ResponseException e) {
//                throw new RuntimeException(e);
                System.out.println("failed to leave the game");
            }
        }

        this.sessionAuthData = null;
        this.gameplayUI = null;
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
