package chess.piece;

public class QueenMovesCalculator extends SlidingMovesCalculator {
    @Override
    int[][] directions() {
        // move in straight lines and diagonals as far as there is open space
        return new int[][]{{0, 1}, {1, 0}, {-1, 0}, {0, -1}, {1, 1}, {1, -1}, {-1, -1}, {-1, 1}};
    }
}
