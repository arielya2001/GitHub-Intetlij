import java.util.*;

public class GameLogic implements PlayableLogic {
    private Disc[][] board = new Disc[8][8];
    private Player player1;
    private Player player2;
    private Stack<Move> moveHistory = new Stack<>();
    private Player currentPlayer;


    private final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},  // Vertical and Horizontal
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1} // Diagonal
    };

    public GameLogic() {
        initializeBoard();
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    private void initializeBoard() {
        if (player1 == null || player2 == null) return;

        board[3][3] = new SimpleDisc(player1);
        board[3][4] = new SimpleDisc(player2);
        board[4][3] = new SimpleDisc(player2);
        board[4][4] = new SimpleDisc(player1);
        player1.reset_bombs_and_unflippedable();
        player2.reset_bombs_and_unflippedable();
        currentPlayer = player1;

    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && col >= 0 && row < board.length && col < board[0].length;
    }

    private List<Position> getFlippableDiscs(int row, int col, int rowAdd, int colAdd) {
        List<Position> flippable = new ArrayList<>();
        int x = row + rowAdd;
        int y = col + colAdd;

        while (isValidPosition(x, y)) {
            Disc currentDisc = board[x][y];
            if (currentDisc == null) break;

            if (currentDisc.getOwner() != currentPlayer) {
                flippable.add(new Position(x, y));
            } else {
                return flippable; // Stop if a disc of the same player is found
            }
            x += rowAdd;
            y += colAdd;
        }
        return new ArrayList<>();
    }

    private boolean isValidMove(int row, int col, Disc disc) {
        if (!isValidPosition(row, col) || board[row][col] != null) return false;
        for (int[] dir : DIRECTIONS) {
            if (!getFlippableDiscs(row, col, dir[0], dir[1]).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void triggerExplosion(int row, int col, Player owner, Set<Position> flippedPositions, Set<Position> triggeredBombs) {
        Queue<Position> queue = new LinkedList<>();
        Position startPos = new Position(row, col);

        queue.add(startPos);
        triggeredBombs.add(startPos);

        while (!queue.isEmpty()) {
            Position currentPos = queue.poll();

            for (int[] dir : DIRECTIONS) {
                int adjRow = currentPos.getRow() + dir[0];
                int adjCol = currentPos.getCol() + dir[1];

                if (isValidPosition(adjRow, adjCol)) {
                    Position adjPos = new Position(adjRow, adjCol);
                    Disc adjDisc = board[adjRow][adjCol];

                    // Skip unflippable discs
                    if (adjDisc instanceof UnflippableDisc) {
                        continue;
                    }

                    if (adjDisc != null && !flippedPositions.contains(adjPos)) {
                        adjDisc.setOwner(owner); // Flip the disc
                        flippedPositions.add(adjPos);

                        // Process bomb discs
                        if (adjDisc instanceof BombDisc && !triggeredBombs.contains(adjPos)) {
                            queue.add(adjPos);
                            triggeredBombs.add(adjPos);
                        }
                    }
                }
            }
        }
    }





    private List<Position> flipDiscs(int row, int col, Disc disc) {
        Set<Position> flippedPositions = new HashSet<>();
        Set<Position> triggeredBombs = new HashSet<>();

        for (int[] dir : DIRECTIONS) {
            List<Position> flippable = getFlippableDiscs(row, col, dir[0], dir[1]);
            for (Position pos : flippable) {
                Disc flippedDisc = board[pos.getRow()][pos.getCol()];
                // Only flip discs that do not already belong to the current player
                if (flippedDisc != null && !(flippedDisc instanceof UnflippableDisc) && flippedDisc.getOwner() != disc.getOwner()) {
                    flippedDisc.setOwner(disc.getOwner());
                    flippedPositions.add(pos);

                    // Trigger explosions only for valid bomb discs
                    if (flippedDisc instanceof BombDisc && !triggeredBombs.contains(pos)) {
                        System.out.println("Triggering bomb at: " + pos.getRow() + ", " + pos.getCol());
                        triggerExplosion(pos.getRow(), pos.getCol(), disc.getOwner(), flippedPositions, triggeredBombs);
                    }
                }
            }
        }
        return new ArrayList<>(flippedPositions);
    }



    @Override
    public boolean locate_disc(Position a, Disc disc) {
        if (disc instanceof BombDisc) {
            if (currentPlayer.getNumber_of_bombs() <= 0) {
                return false;
            }
            currentPlayer.reduce_bomb();
        } else if (disc instanceof UnflippableDisc) {
            if (currentPlayer.getNumber_of_unflippedable() <= 0) {
                return false;
            }
            currentPlayer.reduce_unflippedable();
        }
        int row = a.getRow();
        int col = a.getCol();
        if (!isValidMove(row, col, disc)) return false;

        // Create a snapshot of the board
        Disc[][] boardSnapshot = deepCopyBoard();

        board[row][col] = disc;
        List<Position> flippedPositions = flipDiscs(row, col, disc);
        moveHistory.push(new Move(a, disc, flippedPositions, boardSnapshot)); // Include snapshot
        switchTurn();
        return true;
    }
    private Disc[][] deepCopyBoard() {
        Disc[][] copy = new Disc[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] != null) {
                    if (board[i][j] instanceof SimpleDisc) {
                        copy[i][j] = new SimpleDisc(board[i][j].getOwner());
                    } else if (board[i][j] instanceof BombDisc) {
                        copy[i][j] = new BombDisc(board[i][j].getOwner());
                    } else if (board[i][j] instanceof UnflippableDisc) {
                        copy[i][j] = new UnflippableDisc(board[i][j].getOwner());
                    }
                }
            }
        }
        return copy;
    }



    @Override
    public Disc getDiscAtPosition(Position position) {
        return board[position.getRow()][position.getCol()];
    }

    @Override
    public int getBoardSize() {
        return board.length;
    }

    @Override
    public List<Position> ValidMoves() {
        List<Position> validMoves = new ArrayList<>();
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                Position pos = new Position(row, col);
                if (isValidMove(row, col, new SimpleDisc(currentPlayer)) && countFlips(pos) > 0) {
                    validMoves.add(pos);
                }
            }
        }
        return validMoves;
    }

    private void simulateBombExplosion(int row, int col, Set<Position> flippedPositions, Set<Position> processedBombs) {
        Queue<Position> queue = new LinkedList<>();
        queue.add(new Position(row, col));

        while (!queue.isEmpty()) {
            Position current = queue.poll();

            // Skip already processed bombs
            if (processedBombs.contains(current)) continue;
            processedBombs.add(current);

            // Check all adjacent positions
            for (int[] dir : DIRECTIONS) {
                int adjRow = current.getRow() + dir[0];
                int adjCol = current.getCol() + dir[1];

                if (isValidPosition(adjRow, adjCol)) {
                    Position adjPos = new Position(adjRow, adjCol);
                    Disc adjDisc = board[adjRow][adjCol];

                    // Only count discs that do not already belong to the current player
                    if (adjDisc != null && !(adjDisc instanceof UnflippableDisc) && !flippedPositions.contains(adjPos) && adjDisc.getOwner() != currentPlayer) {
                        flippedPositions.add(adjPos);
                        System.out.println("Flipped position added: " + adjPos.getRow() + ", " + adjPos.getCol());

                        // If it's another bomb, add it to the queue
                        if (adjDisc instanceof BombDisc) {
                            queue.add(adjPos);
                        }
                    }
                }
            }
        }
    }

    private Player[][] getBoardOwners() {
        Player[][] owners = new Player[board.length][board[0].length];
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                Disc disc = board[row][col];
                owners[row][col] = (disc != null) ? disc.getOwner() : null;
            }
        }
        return owners;
    }

    private Player[][] getBoardOwners(Set<Position> flippedPositions) {
        Player[][] owners = getBoardOwners(); // Start with the current board state
        for (Position p : flippedPositions) {
            owners[p.getRow()][p.getCol()] = currentPlayer; // Assume flipped to the current player's color
        }
        return owners;
    }

    private void winnerCount() {
        int countPlayer1 = 0;
        int countPlayer2 = 0;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Disc disc = board[row][col];
                if (disc != null) {
                    if (disc.getOwner() == player1) {
                        countPlayer1++;
                    } else if (disc.getOwner() == player2) {
                        countPlayer2++;
                    }
                }
            }
        }
        int totalDiscs = countPlayer1 + countPlayer2;
        if (countPlayer1 > countPlayer2) {
            player1.addWin();
            System.out.println("Player 1 wins with " + countPlayer1 + " discs! Player 2 had " + countPlayer2 + " discs.");
        } else if (countPlayer2 > countPlayer1) {
            player2.addWin();
            System.out.println("Player 2 wins with " + countPlayer2 + " discs! Player 1 had " + countPlayer1 + " discs.");
        } else {
            System.out.println("The game is a tie! Both players have " + countPlayer1 + " discs.");
        }
        System.out.println("Total discs on the board: " + totalDiscs);
    }

    @Override
    public int countFlips(Position pos) {
        Set<Position> flippedPositions = new HashSet<>();
        Set<Position> processedBombs = new HashSet<>();

        for (int[] dir : DIRECTIONS) {
            List<Position> flippable = getFlippableDiscs(pos.getRow(), pos.getCol(), dir[0], dir[1]);
            for (Position p : flippable) {
                Disc disc = board[p.getRow()][p.getCol()];
                if (disc != null && !(disc instanceof UnflippableDisc) && disc.getOwner() != currentPlayer) {
                    flippedPositions.add(p);

                    if (disc instanceof BombDisc && !processedBombs.contains(p)) {
                        simulateBombExplosion(p.getRow(), p.getCol(), flippedPositions, processedBombs);
                    }
                }
            }
        }

        return flippedPositions.size();
    }









    @Override
    public Player getFirstPlayer() {
        return player1;
    }

    @Override
    public Player getSecondPlayer() {
        return player2;
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        currentPlayer = player1;
        initializeBoard();
    }

    @Override
    public boolean isFirstPlayerTurn() {
        return currentPlayer == player1;
    }

    @Override
    public boolean isGameFinished() {
        boolean isBoardFull = true;
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == null) {
                    isBoardFull = false;
                    break;
                }
            }
            if (!isBoardFull) break;
        }
        if (isBoardFull) {
            winnerCount();
            return true;
        }
        if (ValidMoves().isEmpty()) {
            winnerCount();
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        for (int i = 0; i < board.length; i++) {
            Arrays.fill(board[i], null);
        }
        moveHistory.clear();
        player1.reduce_bomb();
        player2.reduce_bomb();
        player1.reduce_unflippedable();
        player2.reduce_unflippedable();
        initializeBoard();
    }

    public void undoLastMove() {
        if (!moveHistory.isEmpty()) {
            Move lastMove = moveHistory.pop();
            // Restore the board to the saved snapshot if available
            if (lastMove.getBoardSnapshot() != null) {
                restoreBoardFromSnapshot(lastMove.getBoardSnapshot());
            }
            Disc placedDisc = lastMove.getPlacedDisc();
            if (placedDisc instanceof BombDisc) {
                if (placedDisc.getOwner() == player1) {
                    player1.reduce_bomb();
                } else {
                    player2.reduce_bomb();
                }
            } else if (placedDisc instanceof UnflippableDisc) {
                if (placedDisc.getOwner() == player1) {
                    player1.reduce_unflippedable();
                } else {
                    player2.reduce_unflippedable();
                }
            }
            switchTurn();
        }
    }
    private void restoreBoardFromSnapshot(Disc[][] snapshot) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = snapshot[i][j];
            }
        }
    }
}