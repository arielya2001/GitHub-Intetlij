import java.util.*;

/**
 * Represents an AI player in the game. The logic for making a move will be based
 * on AI algorithms such as minimax or heuristic-based decision-making.
 */
public abstract class AIPlayer extends Player {
    public static void registerAllAIPlayers() {
        registerAIPlayerType("RandomAI", RandomAI.class);
        registerAIPlayerType("GreedyAI", GreedyAI.class);
        // Add more AIPlayer subclasses here as needed
    }
    @Override
    public boolean isHuman() {
        return false;
    }

    // Map of AI player types and their corresponding classes
    private static final Map<String, Class<? extends AIPlayer>> aiPlayerRegistry = new HashMap<>();

    public AIPlayer(boolean isPlayerOne) {
        super(isPlayerOne);
    }

    // Register AI player types
    protected static void registerAIPlayerType(String name, Class<? extends AIPlayer> aiPlayerClass) {
        aiPlayerRegistry.put(name, aiPlayerClass);
    }

    public static AIPlayer createAIPlayer(String aiPlayerType, boolean isPlayerOne) {
        Class<? extends AIPlayer> aiPlayerClass = aiPlayerRegistry.get(aiPlayerType);
        if (aiPlayerClass == null) {
            throw new IllegalArgumentException("Unknown AI player type: " + aiPlayerType);
        }

        try {
            return aiPlayerClass.getConstructor(boolean.class).newInstance(isPlayerOne);
        } catch (Exception e) {
            throw new RuntimeException("Error creating AI player: " + aiPlayerType, e);
        }
    }

    // Retrieve the list of registered AI player types
    public static List<String> getAIPlayerTypes() {
        return new ArrayList<>(aiPlayerRegistry.keySet());
    }
    public abstract Move makeMove(PlayableLogic gameStatus);
}