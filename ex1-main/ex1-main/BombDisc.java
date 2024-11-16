public class BombDisc implements Disc
{
    private Player owner;

    public BombDisc(Player currentPlayer)
    {
        this.owner=currentPlayer;
    }

    @Override
    public Player getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(Player player) {
        this.owner=player;

    }

    @Override
    public String getType() {
        return "💣";
    }
    public BombDisc copy() {
        return new BombDisc(owner);
    }
}
