package chess;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
//        throw new RuntimeException("Not implemented");
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
//        throw new RuntimeException("Not implemented");
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
//        return new ArrayList<>();
        // store valid moves
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        switch (type){
            case BISHOP:
                // find where other pieces on board are located
                // calculate all the moves possible
                bishopMoves(board, myPosition, possibleMoves);
                break;
        }
        return possibleMoves;
    }

    private void bishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> possibleMoves){
        // possible moves: {1, 1} {1, -1} {-1, 1} {-1, -1} ... {7, 7}
        // calculate board edge distance from piece

        int[][] directions = {{1,1}, {-1,1}, {1,-1}, {-1,-1}};

        for (int[] direction : directions) {
            int x = direction[0];
            int y = direction[1];
            ChessPosition currPos = myPosition;
            while (true) {
                currPos = currPos.update(x,y); //move one step in direction
                int cX = currPos.getRow();
                int cY = currPos.getColumn();

//                if (!(cX >= 0 && cX <= 7 && cY >= 0 && cY <= 7)) {
//                    break; // break if out of bounds
//                }
                if (cX < 0 || cX > 7 || cY < 0 || cY > 7) {
                    break; //break if out of bounds
                }

                ChessPiece targetPiece = board.getPiece(currPos);

                if (targetPiece == null) {
                    // there is no piece blocking our piece from moving there
                    // add the empty square
                    System.out.println("possible move: (" + cX + ", " + cY + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                } else {
                    // there is a piece blocking us from moving there
                    // if it belongs to our opponent, add it
                    if (targetPiece.getTeamColor() != pieceColor) {
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    }
                    break; // stop further movement in this direction since there is a piece blocking
                }
            }
        }
    }
}
