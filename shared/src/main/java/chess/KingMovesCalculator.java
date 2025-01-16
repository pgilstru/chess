package chess;
import java.util.ArrayList;
import java.util.Collection;

public class KingMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        // moves left/right first then up/down
        int[][] directions = {{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};

        // as long as there isn't a friendly piece, it is an option
        // for each direction, check for a piece
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
