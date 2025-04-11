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


package ui.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.gson.Gson;

import chess.ChessMove;
import model.ResponseException;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketFacade {
    private Session session;
    private NotificationHandler notificationHandler;
    private WebSocketClient client;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            client = new WebSocketClient();
            client.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
//            client.connect(new WebSocketClientHandler(notificationHandler), socketURI, request).get(5, TimeUnit.SECONDS);
            WebSocketClientHandler handler = new WebSocketClientHandler(notificationHandler);
            client.connect(handler, socketURI, request).get(5, TimeUnit.SECONDS);
            this.session = handler.getSession();
        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
            throw new ResponseException(500, "Failed to establish a connection to ws");
        }
    }

    public void connectToGame(String authToken, Integer gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            session.getRemote().sendString(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void leaveGame(String authToken, Integer gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            session.getRemote().sendString(new Gson().toJson(command));
            session.close();
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID);
            command.setMove(move);
            session.getRemote().sendString(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    public void resignGame(String authToken, Integer gameID) throws ResponseException {
        try {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            session.getRemote().sendString(new Gson().toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static class WebSocketClientHandler extends org.eclipse.jetty.websocket.api.WebSocketAdapter {
        private final NotificationHandler notificationHandler;
        private Session session;

        public WebSocketClientHandler(NotificationHandler notificationHandler) {
            this.notificationHandler = notificationHandler;
        }

        @Override
        public void onWebSocketConnect(Session session) {
            this.session = session;
            super.onWebSocketConnect(session);
        }

        @Override
        public void onWebSocketText(String message) {
            ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
            notificationHandler.notify(notification);
        }

        public Session getSession() {
            return session;
        }
    }
}
