package chess;
import java.util.ArrayList;
import java.util.Collection;

public class BishopMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        int[][] directions = {{1,1}, {1,-1}, {-1,-1}, {-1,1}};

        for (int[] direction : directions) {
            int x = direction[0];
            int y = direction[1];
            ChessPosition currPos = myPosition;

            while (true) {
                currPos = currPos.update(x, y); //move one step in direction
                int cX = currPos.getRow();
                int cY = currPos.getColumn();

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



//            int x = direction[0];
//            int y = direction[1];
//            ChessPosition currPos = myPosition;
//            while (true) {
//                currPos = currPos.update(x, y); //move one step in direction
//                int cX = currPos.getRow();
//                int cY = currPos.getColumn();
//
//                if (cX <= 0 || cX > 8 || cY <= 0 || cY > 8) {
//                    System.out.println("Out of bounds!");
//                    break; //break if out of bounds
//                }
//
//                ChessPiece targetPiece = board.getPiece(currPos);
//
//                if (targetPiece == null) {
//                    // there is no piece blocking our piece from moving there
//                    // add the empty square
//                    System.out.println("possible move: (" + cX + ", " + cY + ")");
//                    possibleMoves.add(new ChessMove(myPosition, currPos, null));
//                } else {
//                    // there is a piece blocking us from moving there
//                    // if it belongs to our opponent, add it
//                    if (targetPiece.getTeamColor() != pieceColor) {
//                        System.out.println("possible move (op): (" + cX + ", " + cY + ")");
//                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
//                    }
//                    System.out.println("not possible move: (" + cX + ", " + cY + ")");
//                    break; // stop further movement in this direction since there is a piece blocking
//                }
//            }
        }
        return possibleMoves;
    }
}