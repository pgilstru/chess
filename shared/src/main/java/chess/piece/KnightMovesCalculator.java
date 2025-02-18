package chess.piece;

public class KnightMovesCalculator extends SingleStepMovesCalculator{
    @Override
    int[][] directions() {
        // move in an L shape, moving 2 squares in one direction and 1 square in the other direction.
        // can ignore pieces in the in-between squares (they can "jump" over other pieces)
        return new int[][]{{1,2}, {2,1}, {2,-1}, {1,-2}, {-1,-2}, {-2,-1}, {-2,1}, {-1,2}};
    }
}
