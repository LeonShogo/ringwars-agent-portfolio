package strategyDeciders;

import java.util.Map;
import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.Node;
import feu.agenten.shogo_seifert.StepAnalyzer;

/**
 * Abstract base class for all {@code StrategyDeciders}.
 * <p>
 * A {@code StrategyDecider} represents the highest decision-making level of the
 * agent. It evaluates the current game state and combines multiple move
 * categories into a final move for one game step.
 * <p>
 * It is uses information gained from the shared game state of the
 * {@link GameModel} and methods of the {@link StepAnalyzer} as a foundation for
 * the decision making in the {@link #decide()} methods. Different
 * {@code StrategyDeciders} can be implemented and tested due to modular design.
 * 
 */
public abstract class AbstractStrategyDecider {

	/** The game model provides the game state of current step. */
	protected final GameModel gm;

	/** Analyzer for comparing and interpreting step-based information. */
	protected final StepAnalyzer sa;

	/** Internal budget used to track available Fernies during move composition. */
	protected Map<Integer, Integer> ferniesBudget;

	/**
	 * Constructs a new {@code StrategyDecider} instance and initializes it with the
	 * specific {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel}
	 * @param sa the  {@link StepAnalyzer}
	 */
	public AbstractStrategyDecider(GameModel gm, StepAnalyzer sa) {
		this.gm = gm;
		this.sa = sa;
	}

	/**
	 * Computes and returns the complete move for the current game step. Involves
	 * decision making for the composing of moves.
	 * 
	 * The {@code #updateBudget(Map, StepAnalyzer)} method has to always be invoked
	 * in the very beginning of this method, otherwise, other methods can not work
	 * accordingly.
	 * 
	 * In the next version of this agent, a template that guarantees this will be
	 * implemented!
	 * 
	 * @return a valid move {@link String} representing the agent's decision for the current
	 *         move
	 */
	public abstract String decide();

	/**
	 * Calculates the relative growth factor based on the number of new Fernies and
	 * the ring size.
	 * 
	 * @return growth factor for the current step
	 */
	public double getGrowthFactor() {
		return (double) gm.getNewFernies() / (double) gm.getRingSize();
	}

	/**
	 * Categorizes the ring size into categories.
	 * 
	 * @return the ring size category (SMALL, MID, LARGE)
	 */
	public String ringCategory() {

		int size = gm.getRingSize();

		if (size <= 20)
			return "SMALL";
		if (size <= 30)
			return "MID";
		if (size > 30)
			return "LARGE";

		else
			return "MID";
	}

	/**
	 * Updates the given budget map with the current number of Fernies of all nodes
	 * owned by the agent.
	 * <p>
	 * The map gets cleared before updated.
	 * 
	 * @param budget the budget map that gets updated
	 * @param sa     the StepAnalyzer that has a list of the current owned nodes as
	 *               an attribute
	 * 
	 */
	protected static void updateBudget(Map<Integer, Integer> budget, StepAnalyzer sa) {
		// Clearing, then updating the map to avoid outdated entries
		budget.clear();
		for (Node n : sa.getOwnedNodesCurr()) {
			budget.put(n.getIndex(), n.getFernieCounts());
		}
	}

}
