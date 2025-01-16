package chess;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // move in an L shape, moving 2 squares in one direction and 1 square in the other direction.
        // can ignore pieces in the in-between squares (they can "jump" over other pieces)
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        // moves left/right first then up/down
        int[][] directions = {{1,2}, {-1,2}, {1,-2}, {-1,-2}, {2,1}, {-2,1}, {2,-1}, {-2,-1}};

        // for each direction, check for a piece at the END of the L shape
        // if there is a piece then check if it is friendly or opponent
        // if it is friendly, it is not an option
        for (int[] direction : directions) {
            int x = direction[0];
            int y = direction[1];

            ChessPosition currPos = myPosition;
            currPos = currPos.update(x, y); //move one step in direction
            int cX = currPos.getRow();
            int cY = currPos.getColumn();

            // check if it is out of bounds
            if (cX <= 0 || cX > 8 || cY <= 0 || cY > 8) {
                System.out.println("Out of bounds!");
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
                    System.out.println("possible move (op): (" + cX + ", " + cY + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                }
                System.out.println("not possible move: (" + cX + ", " + cY + ")");
                break; // stop further movement in this direction since there is a piece blocking
            }
        }
        return possibleMoves;
    }
}
