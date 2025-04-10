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
    private WebSocketFacade ws;

    private Integer currGameID;

    // stores authData after logging in or registering
    private AuthData sessionAuthData;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        // initialize connection to the server
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        this.currGameID = null;
    }

    // way to process user input and send it to applicable ui
    public String eval(String input) {
        try {
            if (sessionAuthData == null) {
                // user is logged in, use PreLoginUI
                return new PreLoginUI(this, server).eval(input);
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

    public void logout() {
        if (ws != null && currGameID != null) {
            try {
                ws.leaveGame(sessionAuthData.authToken(), currGameID);
            } catch (ResponseException e) {
//                throw new RuntimeException(e);
            }
        }

        this.sessionAuthData = null;
        this.currGameID = null;
        server.setAuthToken(null);
    }

    public void connectToGame(int gameID) throws ResponseException {
        if (ws == null) {
            ws = new WebSocketFacade(serverUrl, notificationHandler);
        }
        this.currGameID = gameID;
        ws.connectToGame(sessionAuthData.authToken(), gameID);
    }

    public void leaveGame(int gameID) throws ResponseException {
        if (ws != null) {
            ws.leaveGame(sessionAuthData.authToken(), gameID);
            this.currGameID = null;
        }
    }

    public void makeMove(int gameID, ChessMove move) throws ResponseException {
        if (ws != null) {
            ws.makeMove(sessionAuthData.authToken(), gameID, move);
        }
    }

    public void resignGame(int gameID) throws ResponseException {
        if (ws != null) {
            ws. resignGame(sessionAuthData.authToken(), gameID);
        }
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
