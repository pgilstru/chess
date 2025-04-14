package ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import model.ResponseException;
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;
import websocket.messages.ServerMessage;

public class GameplayUI implements NotificationHandler {

    private final ChessBoard chessBoard;
    private final ChessGame.TeamColor playerColor;
    private WebSocketFacade ws;
    private final String authToken;
    private final int gameID;
    private final ChessClient chessClient;

    public GameplayUI(ChessBoard chessBoard, ChessGame.TeamColor playerColor, WebSocketFacade ws,
                      String authToken, int gameID, ChessClient chessClient) {
        this.chessBoard = chessBoard;
        this.playerColor = playerColor;
        this.ws = ws;
        this.authToken = authToken;
        this.gameID = gameID;
        this.chessClient = chessClient;
    }

    @Override
    public void notify(ServerMessage notification) {
        System.out.println("GameplayUI received notification: " + notification.getServerMessageType());
        switch (notification.getServerMessageType()) {
            case LOAD_GAME -> {
                // Update the game state
                System.out.println("Processing load_game message");
                GameData gameData = notification.getGame();
                if (gameData == null || gameData.game() == null) {
                    System.out.println("Game data null");
                }
                assert gameData != null;

                ChessGame game = gameData.game();
                assert game != null;

                // create new board from game state (don't reset existing one)
                ChessBoard newBoard = game.getBoard();

                // update chessboard
                chessBoard.resetBoard();
                updateBoard(newBoard);

                System.out.println("Drawing updated board:");
                drawChessboard(chessBoard, playerColor);

                System.out.println("Board update complete");
            }
            case NOTIFICATION -> {
                // Display the notification message
                String msg = notification.getMessage();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_GREEN + msg + EscapeSequences.RESET_TEXT_COLOR);
            }
            case ERROR -> {
                // Display the error message
                String msg = notification.getErrorMessage();
                System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error: " + msg + EscapeSequences.RESET_TEXT_COLOR);
            }
        }
    }

    private void updateBoard(ChessBoard newBoard) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = newBoard.getPiece(position);
                if (piece != null) {
                    chessBoard.addPiece(position, piece);
                }
            }
        }
    }

    public void setWsFacade(WebSocketFacade ws) {
        this.ws = ws;
    }

    // process user commands
    public String eval(String input) {
        try {
            var tokens = input.trim().split(" ");
            if (tokens.length == 0) {
                // input is empty, call help
                return help();
            }

            // standardize the type of input we receive
            var cmd = tokens[0].toLowerCase();
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            // commands for gameplay
            return switch (cmd) {
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> isObserver() ? "Observers can't make moves" : makeMove(params);
                case "resign" -> isObserver() ? "Observers can't resign" : resign();
                case "highlight" -> highlightLegalMoves(params);
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: " + e.getMessage();
        }
    }

    private boolean isObserver() {
        return playerColor == null;
    }

    public String redraw() throws ResponseException{
        // redraws the chess board
        drawChessboard(chessBoard, playerColor);
        return "Finished redrawing the chessboard!";
    }

    public String leave() throws ResponseException{
        // removes the user from the game
        ws.leaveGame(authToken, gameID);
        chessClient.clearGameplayUI();
        return "You left the game. Type 'help' to see available commands.";
    }

    private String makeMove(String... params) throws ResponseException{
        // verify 2 arguments provided (start position & end position)
        if (params.length != 2) {
            throw new ResponseException(400, "Must provide two arguments. Expected: <START> <END>");
        }

        try {
            // get the start position
            ChessPosition start = parseChessPosition(params[0]);

            // get the end position
            ChessPosition end = parseChessPosition(params[1]);

            // make chessMove object
            ChessMove chessMove = new ChessMove(start, end, null);

            System.out.println("player color: " + playerColor);
            System.out.println("current game state: " + chessBoard.toString());
            System.out.println("Attempting move from " + start + " to " + end);

            ChessPiece piece = chessBoard.getPiece(start);
            if (piece == null) {
                return "No piece at the specified position";
            }
            System.out.println("Moving piece: " + piece.getPieceType() + " with color " + piece.getTeamColor());

            if (piece.getTeamColor() != playerColor) {
                return "You can only move your own pieces";
            }

            // update board for all clients involved in the game to reflect the result of the move
            ws.makeMove(authToken, gameID, chessMove);

            // redraw and print the chessboard
            drawChessboard(chessBoard, playerColor);
            return "Move sent to server";
        } catch (IllegalArgumentException e) {
//            throw new RuntimeException("Incorrect format.");
            return "User Error: Incorrect position format";
        }
    }

    private String resign() throws ResponseException{
        Scanner scanner = new Scanner(System.in);

        // prompt user to confirm they want to resign
        System.out.println("Are you sure you want to forfeit the game? (yes/no)");
        String answer = scanner.nextLine().toLowerCase();

        // check their response
        if (answer.equals("yes")) {
            // user forfeits game and game ends
            ws.resignGame(authToken, gameID);
            chessClient.clearGameplayUI();
            return "You resigned from the game. Type 'help' to see available commands.";
        } else if (answer.equals("no")) {
            // user doesn't forfeit game and game continues
            return "You did not resign from the game";
        } else {
            // user didn't answer "yes" or "no"; game continues
            return "Try again. Must answer either 'yes' or 'no'.";
        }
    }

    // highlight selected piece's current squares and all squares it can legally move to
    private String highlightLegalMoves(String... params) throws ResponseException {
        // verify 1 argument provided (position)
        if (params.length != 1) {
            return "Expected: <position>";
        }

        try {
            ChessPosition position = parseChessPosition(params[0]);
            ChessPiece piece = chessBoard.getPiece(position);

            // verify there is a piece at the given position
            if (piece == null) {
                return "No piece at the given position";
            }

            // find the valid/possible moves to highlight
            Collection<ChessMove> validMoves = chessBoard.getPiece(position).pieceMoves(chessBoard, position);

            // copy of board to highlight and return to user
            String[][] boardCopy = new String[8][8];
            fillOutBoard(chessBoard, boardCopy);

            // highlight given piece (adjust for 0-based array indexing)
            int row = 8 - position.getRow();  // Convert from chess notation to array index
            int col = position.getColumn() - 1;
            boardCopy[row][col] = EscapeSequences.SET_BG_COLOR_YELLOW + boardCopy[row][col] + EscapeSequences.RESET_BG_COLOR;

            // highlight valid moves
            for (ChessMove move : validMoves) {
                int endRow = 8 - move.getEndPosition().getRow();  // Convert from chess notation to array index
                int endCol = move.getEndPosition().getColumn() - 1;
                boardCopy[endRow][endCol] = EscapeSequences.SET_BG_COLOR_GREEN + boardCopy[endRow][endCol] + EscapeSequences.RESET_BG_COLOR;
            }

            if (playerColor != ChessGame.TeamColor.WHITE && playerColor != null) {
                boardCopy = flipBoard(boardCopy); // Flip rows only for black's perspective
            }

            boolean whitePerspective = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);

            // print highlighted board
            printChessboard(boardCopy, whitePerspective, chessBoard);

            return "Highlighted legal moves in green";
        } catch (IllegalArgumentException e) {
            return "User Error: Incorrect position format";
        }
    }

    private String help() {
        // display list of available commands the user can use/actions they can take
        if (isObserver()) {
            // return less commands, only ones observers can do
            return """
               Available game commands:
               redraw - to redraw the current chess game's board
               leave - to leave the current chess game
               highlight <POSITION> - to highlight legal moves the given piece can make
               help - to get help with possible commands
               """;
        }
        return """
               Available game commands:
               redraw - to redraw the current chess game's board
               leave - to leave the current chess game
               move <START> <END> - to make a move from start to end (EX: move b2, b4)
               resign - to resign from, or forfeit, a game
               highlight <POSITION> - to highlight legal moves the given piece can make
               help - to get help with possible commands
               """;
    }

    // converts the given position into a ChessPosition object
    private ChessPosition parseChessPosition(String position) {
        // verify position was provided and is two characters (col and row)
        if (position.length() != 2) {
            throw new IllegalArgumentException("Position formatted incorrectly");
        }

        // get the column letter provided (a, b, c, d, e, f, g, h)
        char col = position.charAt(0);

        // get the row number provided (1, 2, 3, 4, 5, 6, 7, 8)
        char row = position.charAt(1);

        if (col < 'a' || col > 'h' || row < '1' || row > '8') {
            throw new IllegalArgumentException("Must provide a valid position");
        }

        // convert row && column to appropriate numbers
        int rowNum = row - '0';
        int colNum = col - 'a' + 1;

        return new ChessPosition(rowNum, colNum); // create new position with row num and col num
    }

    public static void drawChessboard(ChessBoard chessBoard, ChessGame.TeamColor playerColor) {
//        boolean whitePerspective = (playerColor == ChessGame.TeamColor.WHITE); // Fix the perspective flag
        boolean whitePerspective = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);

        String[][] board = new String[8][8];
        fillOutBoard(chessBoard, board);

        if (!whitePerspective) {
            board = flipBoard(board); // Flip rows only for black's perspective
        }

        // Print chessboard based on player's perspective
        printChessboard(board, whitePerspective, chessBoard);
    }

    private static void printChessboard(String[][] board, boolean whitePerspective, ChessBoard chessBoard) {
        System.out.println(EscapeSequences.ERASE_SCREEN); // Clear screen before printing

        // check the perspective and update the board's perspective as needed
        for (int row = 0; row < 8; row++) {
            if (whitePerspective) {
                System.out.print((8-row) + " ");
            } else {
                System.out.print((row + 1) + " ");
            }

            for (int col = 0; col < 8; col++) {
                // check if square is odd or even and set background color
                if ((row + col) % 2 == 0) {
                    // square is 'even', so it should be printed as a light square
                    System.out.print(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                } else {
                    // square is 'odd', so it should be printed as a dark square
                    System.out.print(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }

                // print out the piece at the current position
                System.out.print(board[row][col]);

                // reset the background color
                System.out.print(EscapeSequences.RESET_BG_COLOR);
            }
            // after every row, print a new line
            System.out.println();
        }

        if (whitePerspective) {
            System.out.println("   a  b  c  d  e  f  g  h");
        } else {
            System.out.println("   h  g  f  e  d  c  b  a");
        }
    }

    private static String[][] flipBoard(String[][] board) {
        String[][] flippedBoard = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                flippedBoard[row][col] = board[7 - row][7 - col]; // Flip both row & column
            }
        }
        return flippedBoard;
    }

    private static void fillOutBoard(ChessBoard chessBoard, String[][] board) {
        // fills out the board with correct pieces
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(position);

                if (piece == null) {
                    // no piece at position, use an empty square
                    board[row - 1][col - 1] = EscapeSequences.EMPTY;
                } else {
                    // format the piece to use correlating symbol
                    board[row - 1][col - 1] = formatPiece(piece);
                }
            }
        }
    }

    private static String formatPiece(ChessPiece piece) {
        ChessGame.TeamColor pieceColor = piece.getTeamColor();
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;

        // get the proper symbol/string for a given piece
        switch (piece.getPieceType()) {
            case BISHOP -> {
                return pieceColor == white ? EscapeSequences.WHITE_BISHOP : EscapeSequences.BLACK_BISHOP;
            }
            case KING -> {
                return pieceColor == white ? EscapeSequences.WHITE_KING : EscapeSequences.BLACK_KING;
            }
            case KNIGHT -> {
                return pieceColor == white ? EscapeSequences.WHITE_KNIGHT : EscapeSequences.BLACK_KNIGHT;
            }
            case PAWN -> {
                return pieceColor == white ? EscapeSequences.WHITE_PAWN : EscapeSequences.BLACK_PAWN;
            }
            case QUEEN -> {
                return pieceColor == white ? EscapeSequences.WHITE_QUEEN : EscapeSequences.BLACK_QUEEN;
            }
            case ROOK -> {
                return pieceColor == white ? EscapeSequences.WHITE_ROOK : EscapeSequences.BLACK_ROOK;
            }
            default -> {
                return EscapeSequences.EMPTY;
            }
        }
    }
}
