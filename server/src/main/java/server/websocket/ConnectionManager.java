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

        // verify there is a connection to remove
        if (gameConnections != null) {
            // remove only the connection for this authToken
            gameConnections.removeIf(c -> c.authToken.equals(authToken));

            if (gameConnections.isEmpty()) {
                // there are no more connections for the game, so remove it
                connections.remove(gameID);
            }
        }
    }

    // will broadcast a message to all clients in a game except specified one (one who makes a move for example)
    public void broadcast(int gameID, String excludeAuthToken, ServerMessage message) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null || gameConnections.isEmpty()) {
            // there isn't a connection to broadcast to
            return;
        }

        var removeList = new ArrayList<Connection>();

        for (var c : gameConnections) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeAuthToken)) {
                    c.send(message.toString());
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any closed connections
        for (var c : removeList) {
            gameConnections.remove(c);
        }

        // remove game entry if there are no more connections for the game
        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }

    public void sendMessage(String authToken, ServerMessage message) throws IOException{
        // find the connection associated with the authToken
        for (var gameConnections : connections.values()) {
            for (var c : gameConnections) {
                if (c.authToken.equals(authToken) && c.session.isOpen()) {
                    c.send(message.toString());
                    return;
                }
            }
        }
    }

    // will broadcast a message to all clients in the game, including the sender (for game updates for example)
    public void broadcastToAll(int gameID, ServerMessage message) throws IOException {
        var gameConnections = connections.get(gameID);
        if (gameConnections == null || gameConnections.isEmpty()) {
            // there isn't a connection to broadcast to
            return;
        }

        var removeList = new ArrayList<Connection>();

        for (var c : gameConnections) {
            if (c.session.isOpen()) {
                c.send(message.toString());
            } else {
                removeList.add(c);
            }
        }

        // Clean up any closed connections
        for (var c : removeList) {
            gameConnections.remove(c);
        }

        // remove game entry if there are no more connections for the game
        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }
    }
}
