import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomAI extends AIPlayer{
    private Random random;


    public RandomAI(boolean isPlayerOne)
    {
        super(isPlayerOne);

        this.random=new Random();
    }
    private Disc chooseRandomDisc()
    {
        List<Disc> availableDiscs = new ArrayList<>();

        availableDiscs.add(new SimpleDisc(this));

        if ( this.getNumber_of_bombs()> 0) {
            availableDiscs.add(new BombDisc(this));
        }

        if (this.getNumber_of_unflippedable() > 0) {
            availableDiscs.add(new UnflippableDisc(this));
        }

        return availableDiscs.get(random.nextInt(availableDiscs.size()));
    }

    @Override
    public Move makeMove(PlayableLogic gameStatus) {
        List<Position>validMoves=gameStatus.ValidMoves();
        if (validMoves.isEmpty())
        {
            return null;
        }
        int randomIndex = random.nextInt(validMoves.size());
        Position selectedMove = validMoves.get(randomIndex);

        Disc chosenDisc=chooseRandomDisc();
        if (chosenDisc instanceof BombDisc) {
            reduce_bomb();
        } else if (chosenDisc instanceof UnflippableDisc) {
            reduce_unflippedable();
        }
        return new Move(selectedMove, chosenDisc, new ArrayList<>());
    }
}