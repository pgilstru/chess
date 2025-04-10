//package ui.websocket;
//
//import chess.ChessMove;
//import com.google.gson.Gson;
//import model.ResponseException;
//import websocket.commands.UserGameCommand;
//import websocket.messages.ServerMessage;
//
//import javax.websocket.*;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//
//@ClientEndpoint
//public class WebSocketFacade extends Endpoint {
//
//    private Session session;
//    private NotificationHandler notificationHandler;
//
//    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
//        try {
//            // change the url to use websocket 'ws'
//            url = url.replace("http", "ws");
//
//            // use the 'ws' endpoint as well
//            URI socketURI = new URI(url + "/ws");
//            this.notificationHandler = notificationHandler;
//
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            this.session = container.connectToServer(this, socketURI);
//
//            //set message handler
//            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
//                @Override
//                public void onMessage(String message) {
//                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
//                    notificationHandler.notify(notification);
//                }
//            });
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    @Override
//    public void onOpen(Session session, EndpointConfig endpointConfig) {
//        this.session = session;
//    }
//
//    @OnMessage
//    public void onMessage(String message) {
//        ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
//        notificationHandler.notify(notification);
//    }
//
//    public void connectToGame(String authToken, Integer gameID) throws ResponseException {
//        // user makes a ws connection as a player or observer
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
////            this.session.getBasicRemote().sendText(new Gson().toJson(command));
//            session.getBasicRemote().sendText(new Gson().toJson(command));
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void leaveGame(String authToken, Integer gameID) throws ResponseException {
//        // tells server you are leaving the game so it won't send you notifications
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
////            this.session.getBasicRemote().sendText(new Gson().toJson(command));
////            this.session.close();
//            session.getBasicRemote().sendText(new Gson().toJson(command));
//            session.close();
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
//        // used to request to make a move in game
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
//
//            // add the move to the command
//            command.setMove(move);
////            this.session.getBasicRemote().sendText(new Gson().toJson(command));
//            session.getBasicRemote().sendText(new Gson().toJson(command));
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void resignGame(String authToken, Integer gameID) throws ResponseException {
//        // forfeits the match and ends the game so no more moves can be made
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
////            this.session.getBasicRemote().sendText(new Gson().toJson(command));
//            session.getBasicRemote().sendText(new Gson().toJson(command));
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//}

//
//package ui.websocket;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import org.eclipse.jetty.websocket.api.Session;
//import org.eclipse.jetty.websocket.api.annotations.WebSocket;
//import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
//import org.eclipse.jetty.websocket.client.WebSocketClient;
//
//import com.google.gson.Gson;
//
//import chess.ChessMove;
//import model.ResponseException;
//import websocket.commands.UserGameCommand;
//import websocket.messages.ServerMessage;
//
//import javax.websocket.ClientEndpoint;
//import javax.websocket.*;
//
//@ClientEndpoint
//public class WebSocketFacade extends Endpoint {
//    private final String url;
//    private Session session;
//    private NotificationHandler notificationHandler;
////    private WebSocketClient client;
//
//    public WebSocketFacade(String url, NotificationHandler notificationHandler) {
//        this.url = url;
//        this.notificationHandler = notificationHandler;
//    }
//
//    public void connectToGame(String authToken, Integer gameID) throws ResponseException {
//        try {
//            URI uri = new URI(url.replace("http", "ws") + "/connect");
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
//            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().configurator(
//                    new ClientEndpointConfig.Configurator() {
//                @Override
//                public void beforeRequest(Map<String, List<String>> headers) {
//                    headers.put("authorization", Collections.singletonList(authToken));
//                }
//            }) .build();
////            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
////            session.getRemote().sendString(new Gson().toJson(command));
//            container.connectToServer(this, config, uri);
//        } catch (DeploymentException | IOException | URISyntaxException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    @OnOpen
//    public void onOpen(Session session, EndpointConfig config) {
//        this.session = session;
//    }
//
//    @OnMessage
//    public void onMessage(Session session, String message) {
//        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
//        notificationHandler.notify(serverMessage);
//    }
//
//    @OnClose
//    public void onClose(Session session, CloseReason closeReason) {
//        this.session = null;
//    }
//
//    public void leaveGame(String authToken, Integer gameID) throws ResponseException {
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
////            session.getRemote().sendString(new Gson().toJson(command));
////            session.close();
//            sendMessage(command);
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
////            command.setMove(move);
////            session.getRemote().sendString(new Gson().toJson(command));
//            sendMessage(command);
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void resignGame(String authToken, Integer gameID) throws ResponseException {
//        try {
//            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
////            session.getRemote().sendString(new Gson().toJson(command));
//            sendMessage(command);
//        } catch (IOException ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    private void sendMessage(UserGameCommand command) throws IOException {
//        if (session != null && session.isOpen()) {
//            session.getBasicRemote().sendText(new Gson().toJson(command));
//        } else {
//            throw new IOException("WebSocket session not open");
//        }
//    }
//
////    private static class WebSocketClientHandler extends org.eclipse.jetty.websocket.api.WebSocketAdapter {
////        private final NotificationHandler notificationHandler;
////        private Session session;
////
////        public WebSocketClientHandler(NotificationHandler notificationHandler) {
////            this.notificationHandler = notificationHandler;
////        }
////
////        @Override
////        public void onWebSocketConnect(Session session) {
////            this.session = session;
////            super.onWebSocketConnect(session);
////        }
////
////        @Override
////        public void onWebSocketText(String message) {
////            ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
////            notificationHandler.notify(notification);
////        }
////
////        public Session getSession() {
////            return session;
////        }
////    }
//}


package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ClientEndpoint
public class WebSocketFacade extends Endpoint {
    private final String serverUrl;
    private final NotificationHandler notificationHandler;
    private Session session;

    public WebSocketFacade(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
    }

    public void connectToGame(String authToken, int gameID) throws ResponseException {
        try {
            URI uri = new URI(serverUrl.replace("http", "ws") + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(Map<String, List<String>> headers) {
                            headers.put("Authorization", Collections.singletonList(authToken));
                        }
                    })
                    .build();
            container.connectToServer(this, config, uri);

            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            sendMessage(command);
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    @OnWebSocketConnect
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
        notificationHandler.notify(serverMessage);
    }

    @OnWebSocketClose
    public void onClose(Session session, CloseReason closeReason) {
        this.session = null;
    }

    public void leaveGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
            command.setMove(move);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resignGame(String authToken, int gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            sendMessage(command);
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private void sendMessage(UserGameCommand command) throws IOException {
        if (session != null && session.isOpen()) {
            session.getBasicRemote().sendText(new Gson().toJson(command));
        } else {
            throw new IOException("WebSocket session is not open");
        }
    }
}
