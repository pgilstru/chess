package ui;

import chess.*;

public class GameplayUI {

    public static void drawChessboard(ChessBoard chessBoard, ChessGame.TeamColor playerColor) {
        // get players perspective
        boolean whitePerspective = playerColor != ChessGame.TeamColor.BLACK;

        // draw the chessboard from the player's perspective
        String[][] board = new String[8][8];

        fillOutBoard(chessBoard, board);

        // check the perspective and update the board's perspective as needed
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (!whitePerspective) {
                    // user is not a whitePlayer or observingPlayer (use black perspective)
                    board[row][col] = board[7 - row][7 - col];
                }
            }
        }

        // then print the board accordingly
        for (int row = 0; row < 8; row++) {
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
