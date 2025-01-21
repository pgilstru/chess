package chess;

import java.util.ArrayList;
import java.util.Collection;

// !come back to later
import static chess.ChessPiece.PieceType.QUEEN;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // move forward one square if that square is unoccupied
        // if it is the first time that pawn is being moved, it can move forward 2 squares
        // if blocked, cannot move
        // capture forward diagonally {1,1} or {-1,1}
        // only move diagonal if capturing enemy piece
        // when they move to the end of the board (row 8 for white and row 1 for black), they get promoted
        // and can be replaced with player's choice of Rook, Knight, Bishop, or Queen

        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();

        int[][] directions;

        if (pieceColor == ChessGame.TeamColor.WHITE){
            // piece is white and starts on the close side of the board
            if (myRow == 2) {
                // the pawn is in its starting position and can also move forward two spaces
                directions = new int[][]{{1,0}, {1,-1}, {1,1}, {2,0}};
            } else {
                // pawn has already made its first move and can only move once forward
                directions = new int[][]{{1,0}, {1,-1}, {1,1}};
            }
        } else {
            // piece is black and starts on the far side of the board
            if (myRow == 7) {
                // the pawn is in its starting position and can also move forward two spaces
                directions = new int[][]{{-1, 0}, {-1, 1}, {-1, -1}, {-2,0}};
            } else {
                // pawn has already made its first move and can only move once forward
                directions = new int[][]{{-1, 0}, {-1, 1}, {-1, -1}};
            }
        }

        // for each direction, check for a piece
        // if there is a piece then you cannot move forward
        // check for a piece to the right and left of each direction
        // if there is a piece, check if it is friendly or opponent
        // if it is an opponent, it is an option
        for (int[] direction : directions) {
            int x = myPosition.getRow() + direction[0];
            int y = myPosition.getColumn() + direction[1];

            // verify it is in bounds
            if (x > 0 && x <= 8 && y > 0 && y <= 8) {
                ChessPosition currPos = new ChessPosition(x, y);
                ChessPiece targetPiece = board.getPiece(currPos);

                if (targetPiece == null && currPos.getColumn() == myCol) {
                    // if a forward move doesn't have a piece blocking it
                    System.out.println("possible move: (" + x + ", " + y + ")");
                    if (pieceColor == ChessGame.TeamColor.WHITE && myRow == 8) {
// !                       ChessPiece.PieceType promotionPiece = QUEEN;
                        possibleMoves.add(new ChessMove(myPosition, currPos, QUEEN));
                    }
                    if (pieceColor == ChessGame.TeamColor.BLACK && myRow == 1) {
//                        ChessPiece.PieceType promotionPiece = getPromotionPiece();
                        possibleMoves.add(new ChessMove(myPosition, currPos, QUEEN));
                    } else {
                        // forward move available and isn't the end of the board
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    }
                } else {
                    // there is a piece blocking us from moving there
                    // if it is diagonal and belongs to our opponent, add it
                    if (targetPiece != null && currPos.getColumn() != myCol && targetPiece.getTeamColor() != pieceColor) {
                        System.out.println("possible move (op): (" + x + ", " + y + ")");
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    }
                    System.out.println("not possible move: (" + x + ", " + y + ")");
                }
            }
        }
        return possibleMoves;
    }
}
