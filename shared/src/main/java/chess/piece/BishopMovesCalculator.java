package chess.piece;

public class BishopMovesCalculator extends SlidingMovesCalculator {
    @Override
    int[][] directions() {
        // move in diagonal lines as far as there is open space
        return new int[][]{{1,1}, {1,-1}, {-1,-1}, {-1,1}};
    }
}