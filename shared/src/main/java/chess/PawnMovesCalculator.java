package chess;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // move forward one square if that square is unoccupied, though if it is the first time that pawn is being moved, it may be moved forward 2 squares
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        // moves left/right first then up/down
        int[][] directions = {{1,2}, {2,1}, {2,-1}, {1,-2}, {-1,-2}, {-2,-1}, {-2,1}, {-1,2}};

        // for each direction, check for a piece at the END of the L shape
        // if there is a piece then check if it is friendly or opponent
        // if it is friendly, it is not an option
        for (int[] direction : directions) {
            int x = myPosition.getRow() + direction[0];
            int y = myPosition.getColumn() + direction[1];

            // verify it is in bounds
            if (x > 0 && x <= 8 && y > 0 && y <= 8) {
                ChessPosition currPos = new ChessPosition(x, y);
                ChessPiece targetPiece = board.getPiece(currPos);

                if (targetPiece == null) {
                    System.out.println("possible move: (" + x + ", " + y + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                } else {
                    // there is a piece blocking us from moving there
                    // if it belongs to our opponent, add it
                    if (targetPiece.getTeamColor() != pieceColor) {
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
