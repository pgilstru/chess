package server.websocket;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();
}
