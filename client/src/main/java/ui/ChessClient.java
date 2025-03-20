package ui;

import com.sun.nio.sctp.NotificationHandler;
import server.Server;

public class ChessClient {

    private final Server server;
    private final String serverUrl;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        server = new Server(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }
}
