package chess.piece;

public class RookMovesCalculator extends SlidingMovesCalculator{
    @Override
    int[][] directions() {
        // move in straight lines as far as there is open space
        // up, down, left, or right
        return new int[][]{{0,1}, {1,0}, {-1,0}, {0,-1}};
    }
}
