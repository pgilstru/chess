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
        // if there is a piece, and it is friendly, it is not an option
        // if there is a piece, and it is opponent, you can capture it but further moves in that direction aren't allowed
        for (int[] direction : directions) {
            int x = direction[0];
            int y = direction[1];
            ChessPosition currPos = myPosition;

            while (true) {
                currPos = currPos.update(x, y); //move one step in direction
                int cX = currPos.getRow();
                int cY = currPos.getColumn();

                // verify it is in bounds
                if (cX > 0 && cX <= 8 && cY > 0 && cY <= 8) {
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
