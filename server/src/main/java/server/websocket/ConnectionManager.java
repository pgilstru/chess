package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, List<Connection>> connections = new ConcurrentHashMap<>();

    public void add(String authToken, int gameID, Session session) {
        var connection = new Connection(authToken, session);

        // CopyOnWriteArray is used bc its thread safe for read and writing
        connections.computeIfAbsent(gameID, k -> new CopyOnWriteArrayList<>()).add(connection);
    }

    public void remove(String authToken, int gameID) {
        var gameConnections = connections.get(gameID);
        if (gameConnections != null && !gameConnections.isEmpty()) {
            // verify there is a connection to remove
            connections.remove(gameID);
        }
    }

    public void broadcast(int gameID, String excludeAuthToken, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();

        for (var c : connections.get(gameID)) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeAuthToken)) {
                    c.send(message.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            // COME BACK TO THIS!
            connections.remove(gameID);
        }
    }
}
