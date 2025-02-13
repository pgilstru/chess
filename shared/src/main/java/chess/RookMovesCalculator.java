package chess;

import java.util.ArrayList;
import java.util.Collection;

public class RookMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // move in a straight line; up, down, left, or right
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        int[][] directions = {{0,1}, {1,0}, {-1,0}, {0,-1}};

        // for each direction, check for a piece
        // if there is a piece, and it is opponent, you can capture it but not move further in that direction
        for (int[] direction : directions) {
            int x = direction[0];
            int y = direction[1];
            ChessPosition currPos = myPosition;

            while (true) {
                // move further sideways or forward as long as there isn't a piece preventing it
                currPos = currPos.update(x, y);
                int cX = currPos.getRow();
                int cY = currPos.getColumn();

                // verify it is in bounds
                if (cX > 0 && cX <= 8 && cY > 0 && cY <= 8) {
                    ChessPiece targetPiece = board.getPiece(currPos);

                    if (targetPiece == null) {
                        System.out.println("possible move: (" + x + ", " + y + ")");
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    } else if (targetPiece.getTeamColor() != pieceColor) {
                        System.out.println("possible move (op): (" + x + ", " + y + ")");
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                        break;
                    } else {
                        break;
                    }
                } else {
                    System.out.println("Out of bounds!");
                    break;
                }
            }
        }
        return possibleMoves;
    }
}
