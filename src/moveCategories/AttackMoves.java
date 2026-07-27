package moveCategories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import feu.agenten.shogo_seifert.AbstractMoves;
import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.GameUtils;
import feu.agenten.shogo_seifert.GameUtils.MoveResult;
import feu.agenten.shogo_seifert.Node;
import feu.agenten.shogo_seifert.OrderMap;
import feu.agenten.shogo_seifert.StepAnalyzer;

/**
 * Collection of attacking moves.
 * <p>
 * These moves are used to actively attack and eliminating nodes controlled by
 * the enemy to reduce the enemies strength.
 * <p>
 * The class extends {@link AbstractMoves} and therefore has access to the
 * shared game state and helper methods.
 */
public class AttackMoves extends AbstractMoves {

	/** List of the visible enemy nodes. */
	private final List<Node> enemyNodes;

	/** List of the enemy nodes sorted by the number of Fernies ascending. */
	private final List<Node> enemiesSorted;

	/**
	 * Constructs a new instance of {@code AttackMoves} with the specified current
	 * {@link GameModel} and {@link StepAnalyzer}.
	 * <p>
	 * Initializes the enemy {@link Node}s lists, sorted list of enemy {@link Node}s
	 * and the strongest {@link Node} owned by the agent.
	 *
	 * @param gm the current {@link GameModel} representing the game state of the
	 *           current step
	 * @param sa the {@link StepAnalyzer} for step analysis
	 */
	public AttackMoves(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
		this.enemyNodes = GameUtils.Analysis.getEnemyNodes(gm);
		this.enemiesSorted = GameUtils.Analysis.getSortedListFernies(enemyNodes);
	}

	/**
	 * Generates several attack moves against enemy nodes.
	 * <p>
	 * Enemy nodes are getting attacked in ascending order of strength. For each
	 * enemy, the best node for the attack gets calculated. The threshold of
	 * necessary Fernies for a (probably) successful attack gets calculated.
	 * <p>
	 * If the threshold is too high for the remaining budget, the enemy node gets
	 * skipped.
	 * After each move, the remaining budget and the {@code newFernies} get updated
	 * by the used number of (new) Fernies.
	 * <p>
	 * The method executes attack actions until the budget is used.
	 * <p>
	 * See {@link #fightStrongestEnemiesBestPlacement(Map, int, double)} for the corresponding method 
	 * that fights the strong enemy {@link Node}s first.
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @param limit  the minimum number of Fernies that shall stay on a node after
	 *               the move
	 * @param factor the scaling factor applied to the calculated enemy strength to
	 *               control the aggressiveness of the attack
	 * @return a move {@link String} containing several generated attack actions
	 */
	public String fightWeakestEnemiesBestPlacement(Map<Integer, Integer> budget, int limit, double factor) {
		String move = "";

		double scale = factor;
		if (factor < 1)
			scale = 1.05;

		// Fallback if no enemy nodes are visible
		if (enemiesSorted == null || enemiesSorted.isEmpty()) {
			move += putNewFerniesOnMaxNode(budget);
			gm.setNewFernies(0);
			return move;
		}

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1.0 };

		// Ascending order by number of Fernies to take from the weaker nodes first
		List<Node> enemies = new ArrayList<>(sa.getEnemyNodesCurr());
		enemies.sort((n1, n2) -> Integer.compare(n1.getFernieCounts(), n2.getFernieCounts()));

		// Iterating over all enemy nodes to attack as many as possible
		for (Node enemy : enemies) {

			// Get the best placement to attack the enemy node
			int bestPlacementIndex = GameUtils.Combat.getBestPlacement(enemy, gm);

			Node bestNode = gm.getRing().get(bestPlacementIndex);

			// Safety
			if (bestNode.isInvisible())
				bestNode = enemy;

			// Calculating the enemy strength including both neighbors
			int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(bestNode, gm);

			// Get all available Fernies
			int myBudget = getNodesBudget(budget);

			// Calculating the number of Fernies for attacking efficiently
			int necessary = (int) Math.ceil(enemyFernies * scale);

			// If not enough Fernies are available
			if (myBudget + gm.getNewFernies() < necessary) {
				continue;
			}

			// Take Fernies from owned nodes based on the budget, the limit and other
			// constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

			// Append the Strings representing the taken action to the move String that gets
			// returned
			move += mR.move;

			// Calculate the number of taken Fernies form the MoveResult object,
			// plus the remaining new Fernies
			int available = mR.take + gm.getNewFernies();

			// Safety: do not take more than available
			int put = Math.max(mR.take, Math.min(available, necessary));

			if (mR.take > put) {
				put = mR.take;
			}

			move += putFerniesMoveBuilder(bestNode.getIndex(), put);

			// Update the number of newFernies
			gm.useNewFernies(put - mR.take);

			if (getNodesBudget(budget) <= 0)
				break;

		}

		// If a rest of new Fernies is still available, place it somewhere
		if (gm.getNewFernies() > 0) {
			move += putAbsFerniesSmart(budget, gm.getNewFernies());
			gm.setNewFernies(0);
		}
		return move;
	}

	/**
	 * Generates several attack moves against enemy nodes.
	 * <p>
	 * Enemy nodes are getting attacked in descending order of strength. For each
	 * enemy, the best node for the attack gets calculated. The threshold of
	 * necessary Fernies for a (probably) successful attack gets calculated.
	 * <p>
	 * If the threshold is too high for the remaining budget, the enemy node gets
	 * skipped.
	 * After each move, the remaining budget and the {@code newFernies} get updated
	 * by the used number of (new) Fernies.
	 * <p>
	 * The method executes attack actions until the budget is used.
	 * <p>
     * See {@link #fightWeakestEnemiesBestPlacement(Map, int, double)} for the corresponding method 
     * that fights the weak enemy {@link Node}s first.
	 * @param budget the budget of the Fernies currently available
	 * @param limit  the minimum number of Fernies that shall stay on a node after
	 *               the move
	 * @param factor the scaling factor applied to the calculated enemy strength to
	 *               control the aggressiveness of the attack
	 * @return a move {@link String} containing several generated attack actions
	 */
	public String fightStrongestEnemiesBestPlacement(Map<Integer, Integer> budget, int limit, double factor) {
		String move = "";

		double scale = factor;
		if (factor < 1)
			scale = 1.1;

		// Fallback if no enemy nodes are visible
		if (enemiesSorted == null || enemiesSorted.isEmpty()) {
			move += putNewFerniesOnMaxNode(budget);
			gm.setNewFernies(0);
			return move;
		}
		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1.0 };

		// Descending order by number of Fernies to take from the stronger nodes first
		List<Node> enemies = new ArrayList<>(sa.getEnemyNodesCurr());
		enemies.sort((n1, n2) -> Integer.compare(n2.getFernieCounts(), n1.getFernieCounts()));

		// Iterating over all enemy nodes to attack as many as possible
		for (Node enemy : enemies) {

			// Get the best placement to attack the enemy node
			int bestPlacementIndex = GameUtils.Combat.getBestPlacement(enemy, gm);

			Node bestNode = gm.getRing().get(bestPlacementIndex);

			// Safety
			if (bestNode.isInvisible())
				bestNode = enemy;

			// Calculating the enemy strength including both neighbors
			int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(bestNode, gm);

			// Get all available Fernies
			int myBudget = getNodesBudget(budget);

			// Calculating the number of Fernies for attack efficiently
			int necessary = (int) Math.ceil(enemyFernies * scale);

			if (myBudget + gm.getNewFernies() < necessary) {
				continue;
			}

			// Take Fernies from owned nodes based on the budget, the limit and other
			// constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

			// Append the Strings representing the taken action to the move String that gets
			// returned
			move += mR.move;

			// Calculate the number of taken Fernies form the MoveResult object,
			// plus the remaining new Fernies
			int available = mR.take + gm.getNewFernies();

			int put = Math.max(mR.take, Math.min(available, necessary));

			if (mR.take > put) {
				put = mR.take;
			}

			move += putFerniesMoveBuilder(bestNode.getIndex(), put);

			// Update the newFernies count
			gm.useNewFernies(put - mR.take);

			if (getNodesBudget(budget) <= 0)
				break;

		}
		// If a rest of new Fernies is still available, place it somewhere
		if (gm.getNewFernies() > 0) {
			move += putAbsFerniesSmart(budget, gm.getNewFernies());
			gm.setNewFernies(0);
		}
		return move;
	}

	/**
	 * Generates several attack moves against enemy nodes.
	 * <p>
	 * Enemy nodes are getting attacked in ascending order of strength. For each
	 * enemy, the threshold of necessary Fernies for a (probably) successful attack
	 * gets calculated. If the threshold is too high for the remaining budget, the
	 * enemy node gets skipped.
	 * <p>
	 * After each move, the remaining budget and the {@code newFernies} get updated
	 * by the used number of (new) Fernies.
	 * The method executes attack actions until the budget is used.
	 * <p>
	 * See {@link #fightStrongestEnemiesWithNeighbors} for the corresponding method
	 *      that fights strong enemy {@link Node}s first.
	 * @param budget the budget of the Fernies currently available
	 * @param limit  minimum number of Fernies that shall stay on a node after the
	 *               move
	 * @param factor the scaling factor applied to the calculated enemy strength to
	 *               control the aggressiveness of the attack
	 * @return a move {@link String} containing several generated attack actions
	 */
	public String fightWeakestEnemiesWithNeighbors(Map<Integer, Integer> budget, int limit, double factor) {
		String move = "";

		double scale = factor;
		if (factor < 1)
			scale = 1.1;

		// Fallback if no enemy nodes are visible
		if (enemiesSorted == null || enemiesSorted.isEmpty()) {
			return putNewFerniesOnMaxNode(budget);
		}

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1.0 };

		// Ascending order by number of Fernies to take from the weaker nodes first
		List<Node> enemies = new ArrayList<>(sa.getEnemyNodesCurr());
		enemies.sort((n1, n2) -> Integer.compare(n1.getFernieCounts(), n2.getFernieCounts()));

		// Iterating over all enemy nodes to attack as many as possible
		for (Node enemy : enemies) {

			if (gm.getNewFernies() <= 0 && getNodesBudget(budget) <= 0)
				break;

			// Calculating the enemy strength including both neighbors
			int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(enemy, gm);

			// Get amount of all available Fernies
			int myBudget = getNodesBudget(budget);

			// Calculating necessary number of Fernies for the move
			int necessary = (int) Math.ceil(enemyFernies * scale);

			// Only proceed if enough Fernies available
			if (myBudget + gm.getNewFernies() < necessary) {
				continue;
			}

			// Take Fernies from owned nodes based on the budget, the limit and other
			// constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

			// Append the Strings representing the taken action to the move String that gets
			// returned
			move += mR.move;

			// Calculate the number of taken Fernies form the MoveResult object,
			// plus the remaining new Fernies
			int available = mR.take + gm.getNewFernies();

			int put = Math.max(mR.take, Math.min(available, necessary));

			if (mR.take > put) {
				put = mR.take;
			}

			move += putFerniesMoveBuilder(enemy.getIndex(), put);

			gm.useNewFernies(put - mR.take);

			if (getNodesBudget(budget) <= 0)
				break;

		}

		// If a rest of new Fernies is still available, place it somewhere
		if (gm.getNewFernies() > 0) {
			move += putAbsFerniesSmart(budget, gm.getNewFernies());
			gm.setNewFernies(0);
		}
		return move;
	}

	/**
	 * Generates several attack moves against enemy nodes.
	 * <p>
	 * Enemy nodes are getting attacked in descending order of strength. For each
	 * enemy, the threshold of necessary Fernies for a (probably) successful attack
	 * gets calculated. If the threshold is too high for the remaining budget, the
	 * enemy node gets skipped.
	 * After each move, the remaining budget and the {@code newFernies} get updated
	 * by the used number of (new) Fernies.
	 * <p>
	 * The method executes attack actions until the budget is used.
	 * <p>
     * See {@link #fightWeakestEnemiesWithNeighbors} for the corresponding method
	 * that fights the weak enemy {@link Node}s first.
	 * @param budget the budget of the Fernies currently available
	 * @param limit  minimum number of Fernies that shall stay on a node after the
	 *               move
	 * @param factor the scaling factor applied to the calculated enemy strength to
	 *               control the aggressiveness of the attack
	 * @return a move {@link String} containing several generated attack actions
	 */
	public String fightStrongestEnemiesWithNeighbors(Map<Integer, Integer> budget, int limit, double factor) {
		String move = "";

		double scale = factor;
		if (factor < 1)
			scale = 1.1;

		// Fallback if no enemy nodes are visible
		if (enemiesSorted == null || enemiesSorted.isEmpty()) {
			return putNewFerniesOnMaxNode(budget);
		}

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1.0 };

		// Sort enemies by Fernies count (in descending order)
		List<Node> enemies = new ArrayList<>(sa.getEnemyNodesCurr());
		enemies.sort((n1, n2) -> Integer.compare(n2.getFernieCounts(), n1.getFernieCounts()));

		// Iterating over all enemy nodes to attack as many as possible
		for (Node enemy : enemies) {

			if (gm.getNewFernies() <= 0 && getNodesBudget(budget) <= 0)
				break;

			// Calculating the enemy strength including both neighbors
			int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(enemy, gm);

			// Get amount of all available Fernies
			int myBudget = getNodesBudget(budget);

			// Calculating necessary number of Fernies for the move
			int necessary = (int) Math.ceil(enemyFernies * scale);

			// Only proceed if enough Fernies available
			if (myBudget + gm.getNewFernies() < necessary) {
				continue;
			}

			// Taking Fernies from owned nodes based on the budget, the limit and other
			// constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

			// Appending the Strings representing the taken action to the move String that
			// gets returned
			move += mR.move;

			// Calculate the number of taken Fernies form the MoveResult object,
			// plus the remaining new Fernies
			int available = mR.take + gm.getNewFernies();

			int put = Math.max(mR.take, Math.min(available, necessary));

			if (mR.take > put) {
				put = mR.take;
			}

			move += putFerniesMoveBuilder(enemy.getIndex(), put);

			gm.useNewFernies(put - mR.take);

			if (getNodesBudget(budget) <= 0)
				break;
		}

		// If a rest of new Fernies is still available, place it somewhere
		if (gm.getNewFernies() > 0) {
			move += putAbsFerniesSmart(budget, gm.getNewFernies());
			gm.setNewFernies(0);
		}
		return move;
	}

	/**
	 * Generates several attack moves against enemy nodes with free border (neutral
	 * neighbor node).
	 * <p>
	 *
	 * For each enemy, the best node for the attack gets calculated. The threshold
	 * of necessary Fernies for a (probably) successful attack gets calculated.
	 * If the threshold is too high for the remaining budget, the enemy node gets
	 * skipped.
	 * <p>
	 * After each move, the remaining budget and the {@code newFernies} get updated
	 * by the used number of (new) Fernies.
	 * <p>
	 * The method executes attack actions until the budget is used.
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @param limit  minimum number of Fernies that shall stay on a node after the
	 *               move
	 * @param factor the scaling factor applied to the calculated enemy strength to
	 *               control the aggressiveness of the attack
	 * @return a move {@link String} containing several generated attack actions
	 * 
	 */
	public String doOnlyBorderFights(Map<Integer, Integer> budget, int limit, double factor) {

		double scale = factor;
		if (factor < 1)
			scale = 1;

		String move = "";

		List<Node> enemiesWithBorders = new ArrayList<Node>();

		// Fallback if no enemy nodes are visible
		if (enemyNodes == null || enemyNodes.isEmpty()) {
			move += putNewFerniesSmart(budget);
			gm.setNewFernies(0);
			return move;
		}

		// Only nodes with neutral borders
		for (Node n : enemyNodes) {
			if (n.isNeighborNeutral(gm.getRing())) {
				enemiesWithBorders.add(n);
			}
		}

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1.0 };

		if (enemiesWithBorders.isEmpty())
			return "";

		// Fight weakest enemies first
		GameUtils.Analysis.sortListFernies(enemiesWithBorders);

		for (Node n : enemiesWithBorders) {

			// Get the best placement to attack the enemy node
			int bestIndex = GameUtils.Combat.getBestPlacement(n, gm);

			if (gm.getNewFernies() <= 0 && getNodesBudget(budget) <= 0)
				break;

			// Get all available Fernies
			int myBudget = getNodesBudget(budget);

			// Calculating necessary number of Fernies for the move
			int necessary = (int) Math.ceil(n.getFernieCounts() * scale);

			// Only proceed if enough Fernies available
			if (myBudget + gm.getNewFernies() < necessary) {
				continue;
			}

			// Take Fernies from owned nodes based on the budget, the limit and other
			// constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

			// Append the Strings representing the taken action to the move String that gets
			// returned
			move += mR.move;

			// Calculate the number of taken Fernies form the MoveResult object,
			// plus the remaining new Fernies
			int put = mR.take + gm.getNewFernies();

			move += putFerniesMoveBuilder(bestIndex, put);

			gm.useNewFernies(put - mR.take);

			if (getNodesBudget(budget) <= 0)
				break;

		}
		// If a rest of new Fernies is still available, place it somewhere
		if (gm.getNewFernies() > 0) {
			move += putAbsFerniesSmart(budget, gm.getNewFernies());
			gm.setNewFernies(0);
		}
		return move;
	}

}
