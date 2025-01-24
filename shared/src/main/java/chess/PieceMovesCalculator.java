package chess;
import java.util.Collection;

public interface PieceMovesCalculator {
    // gets implemented by each pieces moves calculator
    Collection<ChessMove> calculateMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor pieceColor);
}
