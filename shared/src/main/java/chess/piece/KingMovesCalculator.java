package chess.piece;

public class KingMovesCalculator extends SingleStepMovesCalculator {
    @Override
    int[][] directions() {
        return new int[][]{{0,1}, {1,1}, {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}};
    }
}
