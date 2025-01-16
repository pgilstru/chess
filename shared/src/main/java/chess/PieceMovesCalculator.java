package chess;
import java.util.Collection;

public interface PieceMovesCalculator {
    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor);
}
