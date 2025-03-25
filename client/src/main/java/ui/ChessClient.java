package ui;

import model.AuthData;
import server.ServerFacade;

public class ChessClient {

    private final ServerFacade server;
    private final String serverUrl;
    private AuthData sessionData;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }
}
