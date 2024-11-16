import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAI extends AIPlayer{
    private Random random;
    private static final int INITIAL_BOMB_COUNT = 3;
    private static final int INITIAL_UNFLIPPABLE_COUNT = 2;
    private int bombsRemaining;
    private int unflippableRemaining;
    public RandomAI(boolean isPlayerOne)
    {
        super(isPlayerOne);
        this.bombsRemaining=initial_number_of_bombs;
        this.unflippableRemaining=initial_number_of_unflippedable;
        this.random=new Random();
    }
    private Disc chooseRandomDisc()
    {
        List<Disc> availableDiscs = new ArrayList<>();

        availableDiscs.add(new SimpleDisc(this));

        if (bombsRemaining > 0) {
            availableDiscs.add(new BombDisc(this));
        }

        if (unflippableRemaining > 0) {
            availableDiscs.add(new UnflippableDisc(this));
        }

        return availableDiscs.get(random.nextInt(availableDiscs.size()));
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        List<Position> validMoves = gameStatus.ValidMoves();
        if (validMoves.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(validMoves.size());
        Position selectedMove = validMoves.get(randomIndex);

        Disc chosenDisc = chooseRandomDisc();
        if (chosenDisc instanceof BombDisc) {
            bombsRemaining--;
        } else if (chosenDisc instanceof UnflippableDisc) {
            unflippableRemaining--;
        }

        return new Move(selectedMove, chosenDisc, new ArrayList<>(), null); // Pass null for board snapshot
    }

}
