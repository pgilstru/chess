package server.websocket;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import model.GameData;
import model.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import service.GameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.ChessMove;

import java.io.IOException;
import java.util.Collection;

@WebSocket
public class WebSocketHandler {
    private final ConnectionManager connections = new ConnectionManager();

    private final GameService gameService;
//    private final Gson serializer = new Gson();
    private final Gson gson;

    // constructor for WebSocketHandler
    public WebSocketHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new GsonBuilder() .registerTypeAdapter(UserGameCommand.class, new UserGameCommand.GameSerializer())
                .create();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket is connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket connection closed: " + session.getRemoteAddress() + " with status " + statusCode);
        connections.removeConnection(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);

        try {
            // verify user is authenticated
            checkAuth(command.getAuthToken());

            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
                case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove(), session);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), session);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
            }
        } catch (ResponseException | DataAccessException ex) {
            sendError(session, ex.getMessage());
        }
    }

    private void checkAuth(String authToken) throws ResponseException{
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(401, "Error: You are unauthorized");
        }

        if (!gameService.isGoodAuthToken(authToken)) {
            throw new ResponseException(401, "Error: Provided invalid authToken");
        }
    }

    private void validateGameState(GameData gameData, String authToken) throws ResponseException {
        // verify game was provided
        if (gameData == null) {
            throw new ResponseException(400, "Game not found");
        }

        // verify game isn't over
        if (gameData.game().isGameOver()) {
            throw new ResponseException(400, "Game is over");
        }

        // verify it is the user's turn
        String username = gameService.getUsername(authToken);
        ChessGame.TeamColor turn = gameData.game().getTeamTurn();
        if ((turn == ChessGame.TeamColor.WHITE && !username.equals(gameData.whiteUsername())) ||
                (turn == ChessGame.TeamColor.BLACK && !username.equals(gameData.blackUsername()))) {
            throw new ResponseException(403, "It isn't your turn yet");
        }
    }

    private void validateMove(ChessMove move, GameData gameData) throws ResponseException {
        System.out.println("Validating move: " + move);

        // verify the move was given
        if (move == null) {
            throw new ResponseException(400, "Move is null");
        }

        ChessPosition start = move.getStartPosition();
        if (start == null) {
            throw new ResponseException(400, "start position is null");
        }

        System.out.println("Start position: " + start);
        ChessPiece piece = gameData.game().getBoard().getPiece(start);
        System.out.println("Piece at start position: " + piece);

        if (piece == null) {
            throw new ResponseException(400, "No piece at the position");
        }

        // make sure the move is valid for the game in question
        Collection<ChessMove> validMoves = gameData.game().validMoves(start);
        System.out.println("valid moves: " + validMoves);

        if (!validMoves.contains(move)) {
            throw new ResponseException(400, "Not a valid move");
        }
    }

    private void checkGameEnd(GameData gameData) throws IOException {
        ChessGame chessGame = gameData.game();
        String message = null;
        ChessGame.TeamColor whiteTeam = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor blackTeam = ChessGame.TeamColor.BLACK;

        // check if game ended in checkmate or stalemate
        if (chessGame.isInCheckmate(whiteTeam)) {
            message = "White team is in checkmate. Black team wins";
        } else if (chessGame.isInCheckmate(blackTeam)) {
            message = "Black team is in checkmate. White team wins";
        } else if (chessGame.isInStalemate(whiteTeam) || chessGame.isInStalemate(blackTeam)) {
            message = "Game has ended in stalemate";
        }

        if (message != null) {
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameData.gameID(), null, notification);
        }
    }

    private void connect(String authToken, Integer gameID, Session session) throws IOException, DataAccessException {
        // verify user is authenticated
        checkAuth(authToken);

        // verify the game actually exists
        if (!gameService.gameExists(gameID)) {
            String message = "Game doesn't exist";
            sendError(session, message);
            return;
        }

        try {
            // load the game and verify it exists
            GameData gameData = gameService.load(gameID);
            if (gameData == null) {
                throw new ResponseException(400, "Game wasn't found");
            }

            // connect to a specific game
            connections.add(authToken, gameID, session);

            // send a notification
            String username = gameService.getUsername(authToken);
            String message = String.format("user joined the game: " + username);

            // notify player
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, authToken, notification);

            // send the game state to the new connection
            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
            connections.sendMessage(session, loadGame);
        } catch (ResponseException | NullPointerException ex) {
            sendError(session, ex.getMessage());
            session.close();
        }
    }

    private void makeMove(String authToken, Integer gameID, ChessMove chessMove, Session session) throws IOException {
        // make a specific move in a specific game
        try {
            System.out.println("Processing move request for game " + gameID);

            // find the current game
            GameData gameData = gameService.load(gameID);
            if (gameData == null) {
                throw new ResponseException(400, "Game not found");
            }
            System.out.println("Loaded game data: " + gameData);

            // verify the game and move
            validateGameState(gameData, authToken);
            validateMove(chessMove, gameData);

            // update the game with the move
            gameService.makeMove(authToken, gameID, chessMove);
            System.out.println("Move applied to game");

            // check for any game end conditions
            checkGameEnd(gameData);

            // notify players about the move made
            ChessPosition startPos = chessMove.getStartPosition();
            ChessPosition endPos = chessMove.getEndPosition();
            String message = String.format("Move made: " + startPos + " to " + endPos);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            System.out.println("Sending notification: " + message);
            connections.broadcast(gameID, authToken, notification);

            // update game state for all players
            gameData = gameService.load(gameID);
            ServerMessage loadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData);
//            System.out.println("Sending LOAD_GAME message with updated game state");
            connections.broadcast(gameID, null, loadGame);
        } catch (ResponseException ex) {
            System.out.println("Error processing move: " + ex.getMessage());
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, ex.getMessage(), null);
            connections.sendMessage(session, error);
        }
    }

    private void leave(String authToken, Integer gameID, Session session) throws IOException {
        // leave a specific game
        try {
            // verify game exists
            GameData gameData = gameService.load(gameID);
            if (gameData == null) {
                throw new ResponseException(400, "Game wasn't found");
            }

            // remove the connection
            connections.remove(authToken, gameID);

            // send a notification
            String message = String.format("User left the game: " + gameID);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcast(gameID, authToken, notification);
        } catch (ResponseException ex) {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, ex.getMessage(), null);
            connections.sendMessage(session, error);
        }
    }

    private void resign(String authToken, Integer gameID, Session session) throws IOException {
        // resign from a specific game
        try {
            // verify game exists
            GameData gameData = gameService.load(gameID);
            if (gameData == null) {
                throw new ResponseException(400, "Game wasn't found");
            }

            String username = gameService.getUsername(authToken);

            // update game to be over
            gameService.resignGame(gameID, authToken);

            // notify players about resignation
//            connections.remove(authToken, gameID);
            String message = String.format("User [" + username + "] resigned from the game: " + gameID);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
            connections.broadcastToAll(gameID, notification);
        } catch (ResponseException ex) {
//            throw new RuntimeException(e);
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, ex.getMessage(), null);
            connections.sendMessage(session, error);
        }
    }

    private void sendError(Session session, String message) {
        // function for sending errors
        try {
            ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, message, null);
            String errorString = gson.toJson(error);
            session.getRemote().sendString(errorString);
        } catch (IOException e) {
            System.out.println("Failed to send error message: " + e.getMessage());
        }
    }
}
