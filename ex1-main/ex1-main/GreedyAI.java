import java.util.ArrayList;
import java.util.List;

public class GreedyAI extends AIPlayer {

    public GreedyAI(boolean isPlayerOne)
    {
        super((isPlayerOne));

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
            if (flips > maxFlips) {
                maxFlips = flips;
                bestMove = move;
            }
        }

        Disc chosenDisc = new SimpleDisc(this);
        return new Move(bestMove, chosenDisc, new ArrayList<>(), null); // Pass null for board snapshot
    }

    }

