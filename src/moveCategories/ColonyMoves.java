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

// UNUSED: Experimental move category.
// This class is NOT used currently by the final agent, 
// the implementation was used only for exploration of 
// colony based strategies.
class ColonyMoves extends AbstractMoves {

	// UNUSED, therefore private.
	private ColonyMoves(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);

	}

	// UNUSED: Experimental move, currently not used in the final agent.
	@SuppressWarnings("unused")
	private String defendColoniesInDanger(Map<Integer, Integer> budget, int limit) {

		String move = "";

		// Do not take all Fernies from the nodes until the limit is reached, if
		// possible
		double[] takeParts = { 0.75, 1 };

		List<List<Node>> myColonies = GameUtils.Analysis.getColonies(gm.getRing(), "Y");
		if (myColonies.isEmpty())
			return "";

		List<List<Node>> inDangerColonies = new ArrayList<List<Node>>();

		// Only nodes that are in danger
		for (List<Node> colony : myColonies) {
			List<Node> inDangerNodes = sa.inDangerNodes(colony, gm.getRing());
			if (!inDangerNodes.isEmpty())
				inDangerColonies.add(inDangerNodes);
		}

		// Fallback if no nodes are in danger
		if (inDangerColonies == null || inDangerColonies.isEmpty())
			return putNewFerniesOnMaxNode(budget);

		for (List<Node> colony : inDangerColonies) {

			for (Node n : colony) {

				if (colony == null || colony.isEmpty())
					continue;

				// Nearest enemy that threatens
				Node enemy = GameUtils.Combat.getNearestEnemy(n, gm.getRing());
				if (enemy == null)
					continue;

				// Calculating the enemy strength including both neighbors
				int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(enemy, gm);

				// Calculating the number of Fernies for attack efficiently
				int threshold = (int) Math.ceil(enemyFernies * 1.05);

				MoveResult mR = takeFerniesFromBudget(budget, gm, limit, threshold, takeParts, OrderMap.ASC);

				int total = mR.take;

				move += mR.move;

				move += putFerniesMoveBuilder(enemy.getIndex(), total);
			}
		}
		return move;
	}

	// UNUSED: Experimental move, currently not used in the final agent.
	@SuppressWarnings("unused")
	private String stableColony(Map<Integer, Integer> budget) {

		String move = "";

		List<List<Node>> myColonies = GameUtils.Analysis.getColonies(gm.getRing(), "Y");

		if (myColonies.isEmpty())
			return "";

		// Stable only the strongest colony
		List<Node> strongest = myColonies.getLast();

		double average = GameUtils.Analysis.averageFernies(sa.getOwnedNodesCurr());

		// Limit of Fernies that have to remain on each node
		int limit = (int) Math.ceil(average * 0.75);

		// Get the weak nodes that have to get stabilized
		List<Node> weakNodes = new ArrayList<Node>();
		for (Node n : strongest) {
			if (n.getFernieCounts() < average * 0.6) // 0.6
				weakNodes.add(n);
		}

		// Fallback if no weak nodes got found
		if (weakNodes.isEmpty()) {
			return putNewFerniesOnMaxNode(budget);
		}

		int putOnNode = (int) Math.ceil(average - average * 0.5);

		// Calculating necessary number of Fernies for the move
		int necessary = weakNodes.size() * putOnNode;

		// Do not take all Fernies from the nodes until the limit is reached, if
		// possible
		double[] takeParts = { 0.25, 0.5, 0.75 };

		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.DEC);

		int total = mR.take;
		move += mR.move;

		if (total < 0)
			return "";

		int perNode = total / weakNodes.size();

		for (Node n : weakNodes) {
			int use = Math.min(perNode, total);
			move += putFerniesMoveBuilder(n.getIndex(), use);
			budget.put(n.getIndex(), n.getFernieCounts() + use);
			total -= use;
		}
		// Fallback if there is a rest, place it somewhere
		if (total > 0) {
			move += putAbsFerniesSmart(budget, total);
			gm.setNewFernies(0);
		}
		return move;
	}

	// UNUSED: Experimental move, currently not used in the final agent.
	@SuppressWarnings("unused")
	private String expandTheBordersLongestColony(Map<Integer, Integer> budget, int limit) {

		String move = "";

		// Do not take all Fernies from the nodes until the limit is reached, if
		// possible
		double[] takeParts = { 0.75, 1.0 };

		double avg = GameUtils.Analysis.averageFernies(sa.getOwnedNodesCurr());

		// Calculate number of Fernies to expand the edges of the colony
		int necessary = (int) Math.ceil(avg * 1.5);

		MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, takeParts, OrderMap.ASC);

		int ferniesUse = mR.take + gm.getNewFernies();
		move += mR.move;

		// Use all remaining new Fernies
		gm.setNewFernies(0);

		List<List<Node>> myColonies = GameUtils.Analysis.getColonies(gm.getRing(), "Y");

		if (myColonies == null || myColonies.isEmpty()) {
			return "";
		}
		// Expand only the longest owned colony
		List<Node> longestColony = myColonies.getLast();

		// Get edge nodes of the colony
		Node R = longestColony.getLast();
		Node L = longestColony.getFirst();

		Node neighbourR = GameUtils.Ring.neighborR(R, gm.getRing());
		Node neighbourL = GameUtils.Ring.neighborL(L, gm.getRing());

		int half = ferniesUse / 2;
		int rest = ferniesUse - 2 * half;

		// Distributes the Fernies to both ends of the colony
		if (neighbourR.getOwner().equals("N")) {
			move += putFerniesMoveBuilder(neighbourR.getIndex(), half);
			ferniesUse -= half;

		} else if (neighbourR.isEnemy()) {
			move += fightEnemyWithAbsFernies(neighbourR, sa.getOwnedNodesCurr(), half, budget);
			ferniesUse -= half;
		}

		if (neighbourL.getOwner().equals("N")) {
			move += putFerniesMoveBuilder(neighbourL.getIndex(), half);
			ferniesUse -= half;
		} else if (neighbourL.isEnemy()) {
			move += fightEnemyWithAbsFernies(neighbourL, sa.getOwnedNodesCurr(), half, budget);
			ferniesUse -= half;
		}

		// Fallback for the rest if ferniesUse was odd
		// If there is a rest, place it somewhere
		if (rest > 0) {
			move += putAbsFerniesSmart(budget, rest);
			gm.setNewFernies(0);
		}

		return move;
	}

}
