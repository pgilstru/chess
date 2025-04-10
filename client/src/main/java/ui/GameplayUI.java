package ui;

import chess.*;
import model.ResponseException;
import ui.websocket.WebSocketFacade;

import java.util.Arrays;
import java.util.Scanner;

public class GameplayUI {

    private final ChessBoard chessBoard;
    private final ChessGame.TeamColor playerColor;
    private final WebSocketFacade ws;
    private final String authToken;
    private final int gameID;

    public GameplayUI(ChessBoard chessBoard, ChessGame.TeamColor playerColor, WebSocketFacade ws, String authToken, int gameID) {
        this.chessBoard = chessBoard;
        this.playerColor = playerColor;
        this.ws = ws;
        this.authToken = authToken;
        this.gameID = gameID;
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
                case "move" -> makeMove(params);
                case "resign" -> resign();
                case "highlight" -> highlightLegalMoves(params);
                default -> help();
            };
        } catch (ResponseException e) {
            return "Error: " + e.getMessage();
        }
    }

    private String redraw() throws ResponseException{
        // redraws the chess board
        drawChessboard(chessBoard, playerColor);
        return "Finished redrawing the chessboard!";
    }

    private String leave() throws ResponseException{
        // removes the user from the game
        // transitions back to PostLoginUI
        ws.leaveGame(authToken, gameID);
        return "You left the game";
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
            ChessMove move = new ChessMove(start, end, null);

            // update board for all clients involved in the game to reflect the result of the move
            ws.makeMove(authToken, gameID, move);
            return "Successfully made the move!";
        } catch (IllegalArgumentException e) {
//            throw new RuntimeException("Incorrect format.");
            return "Error: Incorrect position format";
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
            return "You resigned from the game";
        } else if (answer.equals("no")) {
            // user doesn't forfeit game and game continues
            return "You did not resign from the game";
        } else {
            // user didn't answer "yes" or "no"; game continues
            return "Try again. Must answer either 'yes' or 'no'.";
        }
    }

    private String highlightLegalMoves(String... params) throws ResponseException{
        // highlight selected piece's current squares and all squares it can legally move to
        // (doesn't update for other players)

    }

    private String help() {
        // display list of available commands the user can use/actions they can take
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

    private ChessPosition parseChessPosition(String position) {
        // converts the given position into a ChessPosition object
    }

    public static void drawChessboard(ChessBoard chessBoard, ChessGame.TeamColor playerColor) {
        boolean whitePerspective = (playerColor == ChessGame.TeamColor.WHITE); // Fix the perspective flag

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
