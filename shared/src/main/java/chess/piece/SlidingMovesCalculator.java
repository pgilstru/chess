package chess.piece;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class SlidingMovesCalculator implements PieceMovesCalculator {
    // subclasses provide directions
    abstract int[][] directions();

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // move in straight lines and diagonals as far as there is open space
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        // for each direction, check for a piece
        // if there is a piece, and it is opponent, you can capture it but not move further in that direction
        for (int[] direction : directions()) {
            int x = direction[0];
            int y = direction[1];
            ChessPosition currPos = myPosition;

            while (true) {
                // move further diagonal or forward as long as there isn't a piece preventing it
                currPos = currPos.update(x, y);
                int cX = currPos.getRow();
                int cY = currPos.getColumn();

                if (cX <= 0 || cX > 8 || cY <= 0 || cY > 8) {
                    System.out.println("Out of bounds!");
                    break;
                }

                ChessPiece targetPiece = board.getPiece(currPos);

                if (targetPiece == null) {
                    System.out.println("possible move: (" + x + ", " + y + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    continue;
                }

                if (targetPiece.getTeamColor() != pieceColor) {
                    System.out.println("possible move (op): (" + x + ", " + y + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                }
                System.out.println("not possible move: (" + x + ", " + y + ")");
                break;
            }
        }
        return possibleMoves;
    }
}
