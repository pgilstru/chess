package chess.piece;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class SingleStepMovesCalculator implements PieceMovesCalculator{
    // subclasses provide directions
    abstract int[][] directions();

    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();

        // for each direction, check for a piece
        // if there is a friendly piece, it is not an option
        for (int[] direction : directions()) {
            int x = myPosition.getRow() + direction[0];
            int y = myPosition.getColumn() + direction[1];

            // verify it is in bounds
            if (x > 0 && x <= 8 && y > 0 && y <= 8) {
                ChessPosition currPos = new ChessPosition(x, y);
                ChessPiece targetPiece = board.getPiece(currPos);

                if (targetPiece == null || targetPiece.getTeamColor() != pieceColor) {
                    System.out.println("possible move: (" + x + ", " + y + ")");
                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
                }
            }
        }
        return possibleMoves;
    }
}
