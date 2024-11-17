import java.util.ArrayList;
import java.util.List;

public class GreedyAI extends AIPlayer {

    public GreedyAI(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        List<Position> validMoves = gameStatus.ValidMoves();
        if (validMoves.isEmpty()) {
            return null;
        }

        Position bestMove = null;
        int maxFlips = -1;

        for (Position move : validMoves) {
            int flips = gameStatus.countFlips(move);

            // Update the best move based on priority rules
            if (flips > maxFlips) {
                maxFlips = flips;
                bestMove = move;
            } else if (flips == maxFlips) {
                // Tie-breaking: prioritize rightmost column and lowest row
                if (bestMove == null || move.getCol() > bestMove.getCol() ||
                        (move.getCol() == bestMove.getCol() && move.getRow() > bestMove.getRow())) {
                    bestMove = move;
                }
            }
        }

        Disc chosenDisc = new SimpleDisc(this);
        return new Move(bestMove, chosenDisc, new ArrayList<>(), null); // Pass null for board snapshot
    }
}
