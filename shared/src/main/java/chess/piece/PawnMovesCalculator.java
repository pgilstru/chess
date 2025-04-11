package chess.piece;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

import static chess.ChessPiece.PieceType.*;


public class PawnMovesCalculator implements PieceMovesCalculator{
    @Override
    public Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor) {
        // for each direction, check for a piece
        // if there is NO piece one square in front, it is an option
        // if there IS an enemy piece  forward diagonal {1,1} or {-1,1}, it is an option
        // if it is the first time that pawn is being moved, it can move forward 2 squares
        // when they move to the end of the board (row 8 for white and row 1 for black), they get promoted
        // and can be replaced with player's choice of Rook, Knight, Bishop, or Queen

        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        int myRow = myPosition.getRow();
        int myCol = myPosition.getColumn();

        int[][] directions;

        if (pieceColor == ChessGame.TeamColor.WHITE){
            // piece is white and starts on the close side of the board
            directions = new int[][]{{1,0}, {1,-1}, {1,1}};
            if (myRow == 2) {
                // the pawn is in its starting position and can also move forward two spaces
                ChessPosition oneForward = new ChessPosition(myRow + 1, myCol);
                ChessPosition twoForward = new ChessPosition(myRow + 2, myCol);
                if (board.getPiece(oneForward) == null && board.getPiece(twoForward) == null) {
                    possibleMoves.add(new ChessMove(myPosition, twoForward, null));
                }
            }
        } else {
            // piece is black and starts on the far side of the board
            directions = new int[][]{{-1, 0}, {-1, 1}, {-1, -1}};
            if (myRow == 7) {
                // the pawn is in its starting position and can also move forward two spaces
                ChessPosition oneForward = new ChessPosition(myRow - 1, myCol);
                ChessPosition twoForward = new ChessPosition(myRow - 2, myCol);
                if (board.getPiece(oneForward) == null && board.getPiece(twoForward) == null) {
                    possibleMoves.add(new ChessMove(myPosition, twoForward, null));
                }
            }
        }

        // if there is an opponent piece, it is an option
        for (int[] direction : directions) {
            int x = myPosition.getRow() + direction[0];
            int y = myPosition.getColumn() + direction[1];

            // verify it is in bounds
            if (x > 0 && x <= 8 && y > 0 && y <= 8) {
                ChessPosition currPos = new ChessPosition(x, y);
                ChessPiece targetPiece = board.getPiece(currPos);

                if ((targetPiece == null && y == myCol) ||
                        (targetPiece != null && y != myCol && targetPiece.getTeamColor() != pieceColor)) {
                    // if a forward move doesn't have a piece blocking it
                    if ((pieceColor == ChessGame.TeamColor.WHITE && x == 8) ||
                    (pieceColor == ChessGame.TeamColor.BLACK && x == 1)) {
                        possibleMoves.add(new ChessMove(myPosition, currPos, QUEEN));
                        possibleMoves.add(new ChessMove(myPosition, currPos, ROOK));
                        possibleMoves.add(new ChessMove(myPosition, currPos, BISHOP));
                        possibleMoves.add(new ChessMove(myPosition, currPos, KNIGHT));
                    } else {
                        // forward move available and isn't the end of the board
                        possibleMoves.add(new ChessMove(myPosition, currPos, null));
                    }
                }
            }
        }
        return possibleMoves;
    }
}
