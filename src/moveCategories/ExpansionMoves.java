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
 * This class is a collection of expansion moves.
 * <p>
 * These moves are used for expanding and increasing the number of controlled
 * nodes owned by the agent. They aim to occupy neutral nodes and to fight weak
 * enemy nodes. They help to gather information by expanding the visibility and
 * increase the ownership bonus.
 * 
 * <p>
 * The class extends {@link AbstractMoves} and therefore has access to the
 * shared game state and helper methods.
 */

public class ExpansionMoves extends AbstractMoves {

	/**
	 * Constructs a new instance of {@code ExpansionMoves} and initializes it with
	 * the specified {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel} representing the game state of the
	 *           current step
	 * @param sa the {@link StepAnalyzer} for step analysis
	 *
	 */
	public ExpansionMoves(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
	}

	/**
	 * Combines attacking and expanding move.
	 * <p>
	 * This method takes Fernies from the budget calculated relatively to the
	 * average Fernies per owned node scaled by the {@code averageFactor}. With the
	 * taken Fernies, enemy nodes get attacked if possible first and then neutral
	 * nodes get occupied.
	 * 
	 * Remaining Fernies are placed on the strongest node as a fallback.
	 * 
	 * @param budget        the budget of the Fernies available
	 * @param averageFactor scaling factor applied to the average number of Fernies
	 *                      per owned node to determine aggressiveness
	 * @return a move {@link String} representing multiple attack and placement actions, or
	 *         an empty {@link String} if no action is possible
	 */
	public String getAsManyNodesAndFight(Map<Integer, Integer> budget, double averageFactor) {

		int average = (int) GameUtils.Analysis.averageFernies(sa.getOwnedNodesCurr());

		String move = "";

		int limit = 0;
		// Take all available Fernies of the nodes, limit = 0
		double[] takeParts = { 1.0 };

		if (average == 0)
			return "";

		// Decide how many Fernies to use for each action for one node
		int perNode = (int) Math.ceil(average * averageFactor);

		// Constructing a list of target nodes
		List<Node> targets = new ArrayList<Node>();
		targets.addAll(sa.getEnemyNodesCurr());
		targets.addAll(sa.getNeutralNodesCurr());

		if (targets.isEmpty()) {
			return "";
		}

		// Calculating necessary number of Fernies for the move
		int necessary = (int) (perNode * (targets.size()));

		// Get all available Fernies
		int totalBudget = getNodesBudget(budget);

		// Only use the available Fernies
		if (necessary > totalBudget + gm.getNewFernies()) {
			necessary = totalBudget + gm.getNewFernies();
		}

		// Take Fernies from owned nodes based on the budget, the limit and other constraints
		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

		// Append the Strings representing the taken action to the move String that gets returned
		move += mR.move;
		
		int available = mR.take + gm.getNewFernies();
		
		// Only put what is necessary
		int put = Math.max(mR.take, Math.min(available, necessary));
		
		// Safety: Use all Fernies previously taken
		if (mR.take > put) {
			put = mR.take;
		}
		
		// Update the new Fernies
		gm.useNewFernies(put - mR.take);

		// First fight the enemies
		for (Node ts : targets) {

			// Only fight enemies
			if (!ts.isEnemy())
				continue;
			if (put <= 0)
				break;

			// Calculating the enemy strength including both neighbors
			int enemyStrength = GameUtils.Combat.getEnemyStrengthWithNeighbours(ts, gm);

			// Only proceed if enough Fernies available
			if (enemyStrength >= put)
				continue;
			
			int putFernies = Math.min(enemyStrength + 1, put);
			
			// Executing the attack
			String attack = fightEnemyWithAbsFernies(ts, sa.getOwnedNodesCurr(), putFernies, budget);
			
			// Append the String representing the attack of the enemy to the move String
			move += attack;
			
			// Updating the number of available Fernies for the next attack
			put -= putFernies;
		}

		// Secondly: occupy neutral nodes
		for (Node t : targets) {
			if (put <= 0)
				break;
			// Place only on neutral nodes
			if (!t.isNeutral())
				continue;
			if (t.isNeighborNeutral(gm.getRing()))
				move += putFerniesMoveBuilder(t.getIndex(), 1);
			put -= 1;

		}

		// If there is a rest for Fernies available, place it somewhere
		if (put > 0) {
			move += putAbsFerniesSmart(budget, put);
		}

		return move;
	}

	/**
	 * Occupies as many visible neutral nodes as possible.
	 * <p>
	 * Fixed number of Fernies placed on each neutral node, defined by the
	 * {@code perNode} parameter. If the neighbor of a neutral node is an enemy with
	 * a strength {@code <=3}, the enemy gets attacked.
	 * <p>
	 * Fernies are taken from the budget, remaining Fernies are placed on the
	 * stronges owend node as a fallback move.
	 * 
	 * @param budget  the budget of the Fernies available
	 * @param perNode number of Fernies to place on each neutral node
	 * @return a move {@link String} representing the expansion and attack actions, or an
	 *         empty {@link String} if no aciton is possible (edge cases)
	 */
	public String getAsManyNeutralNodes(Map<Integer, Integer> budget, int perNode) {

		String move = "";
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		if (myMax == null)
			return "";

		// Keep at least on Fernie per node
		int limit = 1;

		// Fallback if no neutral nodes exist
		if (sa.getNeutralNodesCurr() == null || sa.getNeutralNodesCurr().isEmpty())
			return putNewFerniesOnMaxNode(budget);

		List<Node> neutrals = new ArrayList<>(sa.getNeutralNodesCurr());

		// Prefer neutral nodes that are close to the owned node with the highest amount
		// of Fernies
		// to defend it
		neutrals.sort((a, b) -> {
			int da = GameUtils.Ring.distanceOnRing(myMax.getIndex(), a.getIndex(), gm.getRingSize());
			int db = GameUtils.Ring.distanceOnRing(myMax.getIndex(), b.getIndex(), gm.getRingSize());
			return Integer.compare(da, db);
		});

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1 };

		// Calculating necessary number of Fernies for the move
		int necessary = neutrals.size() * perNode;

		// Get all available Fernies
		int totalBudget = getNodesBudget(budget);

		// Only proceed if enough Fernies available
		if (necessary > totalBudget) {
			necessary = totalBudget;
		}

		// Take Fernies from owned nodes based on the budget, the limit and other constraints
		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);
		
		// Append the Strings representing the taken action to the move String that gets returned
		move += mR.move;

		int ferniesUse = mR.take + gm.getNewFernies();
		gm.setNewFernies(0);

		for (Node n : neutrals) {
			if (ferniesUse <= 0)
				break;

			// If the neighbor of the neutral node is a weak enemy, fight the enemy node
			if (n.isNeighborEnemy(gm.getRing())) {

				int enemyStrength = GameUtils.Combat.getEnemyStrengthWithNeighbours(n, gm);
				if (enemyStrength <= 3 && ferniesUse >= 3) {
					move += putFerniesMoveBuilder(n.getIndex(), 3);
					ferniesUse -= 3;

				}

			}
			int putFernies = Math.min(perNode, ferniesUse);

			move += putFerniesMoveBuilder(n.getIndex(), putFernies);
			ferniesUse -= putFernies;

		}
		// If there is a rest for Fernies available, place it somewhere
		if (ferniesUse > 0) {
			move += putAbsFerniesSmart(budget, ferniesUse);
		}

		return move;
	}

	/**
	 * Places Fernies on a neutral or owned node invisible for the enemy.
	 * <p>
	 * The candidate with the greatest distance to the enemy is selected as the
	 * target node.
	 * <p>
	 * A fixed part of the available Fernies budget is taken and placed on the
	 * target node. If no hidden node exists, a fallback placement strategy is
	 * applied.
	 * 
	 * @param budget the budget of the Fernies available
	 * @return a move {@link String} representing the placement action, or a fallback move {@link String}
	 *         if no hidden node is available
	 */
	public String putOnHiddenNode(Map<Integer, Integer> budget) {

		String move = "";
		List<Node> hiddenNodes = new ArrayList<Node>();
		int limit = 0;

		// Collect neutral or owned nodes that are hidden
		for (Node n : gm.getRing()) {
			if ((n.isNeutral() || n.getOwner().equals("Y")) && !sa.isVisibleToEnemy(n, gm.getRing(), gm)) {
				hiddenNodes.add(n);
			}
		}

		// Fallback move if no hidden nodes exist
		if (hiddenNodes.isEmpty()) {
			move += putNewFerniesSmart(budget);
			return move;
		}

		Node best = hiddenNodes.get(0);
		int dist = GameUtils.Analysis.distanceNearestEnemy(best, gm.getRing());

		// Get the hidden node with the greatest distance to the enemy
		for (Node n : hiddenNodes) {
			int d = GameUtils.Analysis.distanceNearestEnemy(n, gm.getRing());
			if (d > dist) {
				best = n;
				dist = d;
			}
		}

		// Get number of available Fernies
		int totalBudget = getNodesBudget(budget);

		// Calculating number of Fernies for hiding
		int necessary = (int) Math.ceil(totalBudget * 0.3);

		// Ensure that the number of Fernies placed does not exceed the node capacity
		if (necessary >= gm.getMaxFernies()) {
			necessary = necessary - gm.getMaxFernies();
		}

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1 };

		// Taking Fernies from owned nodes
		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.DEC);
		
		// Append the Strings representing the taken action to the move String that gets returned
		move += mR.move;

		int ferniesUse = mR.take + gm.getNewFernies();

		gm.setNewFernies(0);

		int currentBest = budget.getOrDefault(best.getIndex(), best.getFernieCounts());

		// Updating budget if node to place was owned node
		if (best.getOwner().equals("Y")) {
			budget.put(best.getIndex(), currentBest + ferniesUse);
		}
		move += putFerniesMoveBuilder(best.getIndex(), ferniesUse);
		return move;
	}

	/**
	 * Expands to nodes that are invisible for the enemy.
	 * <p>
	 * Neutral and owned nodes invisible for the enemy are collected. A fixed part
	 * of the available Fernies is taken and distributed to the collected candidates
	 * that are hidden for the enemy.
	 * <p>
	 * If no hidden nodes are available, a fallback move is applied.
	 * 
	 * @param budget the budget of the Fernies available
	 * @param limit  the minimum number of Fernies that must remain on a node when
	 *               taking the Fernies
	 * @return a move {@link String} representing the action, or a  move {@link String} 
	 * 		   representing the fallback action when no hidden nodes exists 
	 */
	public String expandToHiddenNodes(Map<Integer, Integer> budget, int limit) {

		String move = "";

		List<Node> hidden = new ArrayList<>();

		// Collect neutral or owned nodes that are hidden from the enemy
		for (Node n : gm.getRing()) {
			if ((n.isNeutral() || n.getOwner().equals("Y")) && !sa.isVisibleToEnemy(n, gm.getRing(), gm)) {
				hidden.add(n);
			}
		}

		// Fallback move if no hidden nodes exist
		if (hidden.isEmpty()) {
			return putNewFerniesSmart(budget);
		}

		// Get total number of available Fernies
		int totalBudget = getNodesBudget(budget);

		// Calculating number of Fernies for hidden expansion
		int necessary = (int) Math.ceil(totalBudget * 0.6);

		// Take all available Fernies of the nodes, respecting the limit
		double[] takeParts = { 1 };

		// Take Fernies from owned nodes based on the budget, the limit and other constraints
		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);
		
		// Append the Strings representing the taken action to the move String that gets returned
		move += mR.move;
		int ferniesUse = mR.take + gm.getNewFernies();
		gm.setNewFernies(0);

		if (ferniesUse <= 0)
			return "";
		
		// Distribute Fernies
		int nodesToUse = Math.min(hidden.size(), ferniesUse);
		
		int perNode = ferniesUse / nodesToUse;
		if (perNode == 0)
			perNode = 1;

		for (int i = 0; i < nodesToUse; i++) {
			if (ferniesUse <= 0)
				break;
			Node nd = hidden.get(i);
			int put = Math.min(perNode, ferniesUse);
			move += putFerniesMoveBuilder(nd.getIndex(), put);
			ferniesUse -= put;

		}

		// If there is a rest for Fernies available, place it somewhere
		if (ferniesUse > 0) {
			move += putAbsFerniesSmart(budget, ferniesUse);
		}
		return move;
	}

}
