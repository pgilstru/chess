package chess;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private static final Map<Character, ChessPiece.PieceType> CHAR_TO_TYPE_MAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP
    );

    private final ChessPiece[][] squares = new ChessPiece[9][9];
    public ChessBoard() {
        // just initializes empty board, resetboard does the rest
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        var defaultBoard = """
                |r|n|b|q|k|b|n|r|
                |p|p|p|p|p|p|p|p|
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                | | | | | | | | |
                |P|P|P|P|P|P|P|P|
                |R|N|B|Q|K|B|N|R|
                """;
        int row = 8;
        int column = 1;
        for (var c : defaultBoard.toCharArray()) {
            switch (c) {
//                case '\n':
//                    // if new line, move to next row
//                    column = 1;
//                    row--;
//                    break;
//                case ' ':
//                    // empty square, move to next column
//                    column++;
//                    break;
//                case '|':
//                    // ignore pipe
//                    break;
                case '\n' -> {
                    column = 1;
                    row--;
                }
                case ' ' -> column++;
                case '|' -> {
                }
                default -> {
                    if (row < 1 || row > 8 || column < 1 || column > 8) {
                        throw new IllegalStateException("row/column out of bounds");
                    }

                    // verify color
                    ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;

                    // find type
                    ChessPiece.PieceType type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));

                    // create piece
                    ChessPosition position = new ChessPosition(row, column);
                    ChessPiece piece = new ChessPiece(color, type);

                    // add piece to board
                    addPiece(position, piece);

                    // move onto next column
                    column++;
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessBoard that)) {
            return false;
        }
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
