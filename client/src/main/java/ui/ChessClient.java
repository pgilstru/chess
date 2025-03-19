package ui;

import com.sun.nio.sctp.NotificationHandler;

public class ChessClient {

    private final ServerFacade server;
    private final String serverUrl;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }
}
