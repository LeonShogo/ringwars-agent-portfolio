package moveCategories;

import java.util.List;
import java.util.Map;

import feu.agenten.shogo_seifert.AbstractMoves;
import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.GameUtils;
import feu.agenten.shogo_seifert.Node;
import feu.agenten.shogo_seifert.StepAnalyzer;

/**
 * This class is a collection of defensive moves.
 * <p>
 * These moves can be used both reactively (the agent is under pressure) and
 * preventively.
 * <p>
 * The class extends {@link AbstractMoves} and therefore has access to the
 * shared game state and helper methods.
 */
public class DefensiveMoves extends AbstractMoves {

	/**
	 * Constructs a new {@code DefensiveMoves} instance and initializes it with the
	 * specified {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel} representing the game state of the
	 *           current step
	 * @param sa the {@link StepAnalyzer} for step analysis
	 */
	public DefensiveMoves(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
	}

	/**
	 * Collects all Fernies of owned nodes and places them on the strongest owned
	 * node.
	 * <p>
	 * This move is useful in endgame situations, when the agent has only a few
	 * remaining spread on nodes.
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @return a move {@link String} representing the placement action
	 */
	public String collect(Map<Integer, Integer> budget) {

		// No enemy threatens
		if (sa.getEnemyNodesCurr() == null || sa.getEnemyNodesCurr().isEmpty())
			return "";
		// If no owned nodes exists
		if (sa.getOwnedNodesCurr() == null || sa.getOwnedNodesCurr().isEmpty())
			return "";

		String move = "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		
		int maxHave = budget.getOrDefault(myMax.getIndex(), 0);

		int put = 0;

		// Collecting the Fernies
		for (Node n : sa.getOwnedNodesCurr()) {

			if (n.getIndex() == myMax.getIndex())
				continue;

			int available = budget.getOrDefault(n.getIndex(), 0);
			
			// Take all Fernies of each owned node
			move += takeFerniesFromNodeMoveBuilder(n, available);
	
			// Update the budget of the owned node
			budget.put(n.getIndex(), 0);
			// Update the amount of Fernies that get placed on the strongest node
			put += available;

		}
		// Putting the Fernies
		move += putFerniesMoveBuilder(myMax.getIndex(), put);
		// Update the budget of the strongest node
		budget.put(myMax.getIndex(), maxHave + put);
		return move;
	}

	/**
	 * Moves all Fernies of the owned node with the highest number of Fernies to the
	 * weakest visible enemy node.
	 * 
	 * Usually, this move is used as a last move of a step to avoid harm as a result
	 * of an enemy attack on this node.
	 * 
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @return a move {@link String} representing the shift action or an empty {@link String} if no
	 *         enemy node was found or the enemy does not see this node
	 */
	public String moveMax(Map<Integer, Integer> budget) {

		// No enemy threatens
		if (sa.getEnemyNodesCurr() == null || sa.getEnemyNodesCurr().isEmpty())
			return "";
		// If no owned nodes exists
		if (sa.getOwnedNodesCurr() == null || sa.getOwnedNodesCurr().isEmpty())
			return "";

		String move = "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		Node weakEnemy = GameUtils.Analysis.nodeWithMinFernies(sa.getEnemyNodesCurr());

		if (myMax == null)
			return "";

		if (weakEnemy == null)
			return "";

		// If the enemy does not see the strongest node, do no action
		if (!sa.isVisibleToEnemy(myMax, gm.getRing(), gm))
			return "";

		int realHave = myMax.getFernieCounts();

		int budgetHave = budget.getOrDefault(myMax.getIndex(), realHave);

		// Only place the available Fernies
		int take = Math.min(realHave, budgetHave);

		move += takeFerniesFromNodeMoveBuilder(myMax, take);
		budget.put(myMax.getIndex(), 0);
		move += putFerniesMoveBuilder(weakEnemy.getIndex(), take);

		return move;
	}

	/**
	 * Shifts the Fernies of the node with the highest number of Fernies to a node
	 * that has the best defensive potential.
	 * <p>
	 * The best node to place is calculated by an helper method of
	 * {@link GameUtils.Combat}. If no good node was found, a fallback move is
	 * applied.
	 * 
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @return a move {@link String} representing the shift action, a fallback move if no
	 *         appropriate node was found or an empty {@link String} for edge cases
	 */
	public String runAway(Map<Integer, Integer> budget) {

		// No need to run away
		if (sa.getEnemyNodesCurr() == null || sa.getEnemyNodesCurr().isEmpty())
			return "";
		if (sa.getOwnedNodesCurr() == null || sa.getOwnedNodesCurr().isEmpty())
			return "";

		String move = "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		if (myMax == null)
			return "";

		// Getting the best node to run away
		Node bestNode = GameUtils.Combat.getBestNodeToRunAway(gm.getRing(), gm);

		// Fallback if no best Node was found
		if (bestNode == null)
			return putNewFerniesSmart(budget);

		int available = budget.getOrDefault(myMax.getIndex(), myMax.getFernieCounts());

		// Only take the available Fernies
		int take = Math.min(available, myMax.getFernieCounts());

		move += takeFerniesFromNodeMoveBuilder(myMax, take);
		
		// Update the budget of the strongest node
		budget.put(myMax.getIndex(), 0);
		move += putFerniesMoveBuilder(bestNode.getIndex(), take);

		return move;
	}

	// UNUSED: Experimental move, currently not used in the final agent.
	@SuppressWarnings("unused")
	private String putMaxNodeInTheCenterOfStrongestColony(Map<Integer, Integer> budget) {

		String move = "";
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		if (myMax == null)
			return "";

		// Getting all owned colonies, sorted in ascending order by the total number of Fernies
		List<List<Node>> myColonies = GameUtils.Analysis.getColonies(gm.getRing(), "Y");
		if (myColonies.isEmpty())
			return "";

		// Get the strongest colony
		List<Node> strongest = myColonies.getLast();

		// Get the center node of the colony
		Node center = strongest.get(strongest.size() / 2);

		int putIndex = center.getIndex();

		int maxFernies = budget.getOrDefault(myMax.getIndex(), myMax.getFernieCounts());

		if (maxFernies <= 0)
			return "";

		move += takeFerniesFromNodeMoveBuilder(myMax, maxFernies);
		// Update the budget of the strongest node
		budget.put(myMax.getIndex(), 0);

		// Get the number of Fernies on the center node
		int centerFernies = budget.getOrDefault(center.getIndex(), center.getFernieCounts());

		// Only place difference to maximum capacity per node
		int possible = gm.getMaxFernies() - centerFernies;
		// Safety
		if (possible <= 0) {
			return move;
		}

		int putFernies = Math.min(possible, maxFernies);

		move += putFerniesMoveBuilder(putIndex, putFernies);
		budget.put(putIndex, centerFernies + putFernies);

		int rest = maxFernies - putFernies;

		// If there is a rest for Fernies available, place it somewhere
		if (rest > 0) {
			move += putAbsFerniesSmart(budget, rest);
		}
		return move;
	}

}
