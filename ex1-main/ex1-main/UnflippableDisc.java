public class UnflippableDisc implements Disc{
    private Player owner;

    public UnflippableDisc(Player currentPlayer) {
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
        return "⭕";
    }
    // Add the copy method
    public UnflippableDisc copy() {
        return new UnflippableDisc(this.owner);
    }
}
