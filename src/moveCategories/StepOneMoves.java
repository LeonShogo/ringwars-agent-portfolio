package moveCategories;

import java.util.Map;

import feu.agenten.shogo_seifert.AbstractMoves;
import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.GameUtils;
import feu.agenten.shogo_seifert.Node;
import feu.agenten.shogo_seifert.StepAnalyzer;

/**
 * This class is a collection of moves used only in the first step.
 * <p>
 * These moves are used in the very beginning of the game and for the first
 * step. Different moves in the first step can be useful for different game
 * settings or against different opponents.
 * <p>
 * The class extends {@link AbstractMoves} and therefore has access to the
 * shared game state and helper methods.
 */

public class StepOneMoves extends AbstractMoves {

	/**
	 * Constructs a new instance of {@code StepOneMoves} and initializes it with the
	 * specified {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel} representing the game state of the
	 *           current step
	 * @param sa the {@link StepAnalyzer} for step analysis
	 *
	 */
	public StepOneMoves(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
	}

	/**
	 * Used usually only in the first step of the game, but could also be used in
	 * other steps.
	 * <p>
	 * Distributes the new Fernies and Fernies from the strongest node (which is the
	 * only node in step one) to all visible neutral (and owned) nodes, in order to
	 * occupy neutral nodes in the early game or to stabilize owned nodes. Placement
	 * is done symmetrical around the strongest node.
	 * 
	 * Fallback moves are applied for all previously taken Fernies that were not
	 * placed.
	 *
	 * @param budget the budget of the Fernies available
	 * @return a move {@link String} representing the expansion action
	 */
	public String expandStepOneOriginal(Map<Integer, Integer> budget) {

		String move = "";
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		int useFernies = gm.getNewFernies();

		// Get enough Fernies to expand in both directions across
		// the full visibility range
		if (useFernies < gm.getAbsVis() * 2) {
			int diff = gm.getAbsVis() * 4 - useFernies;
			// Take more Fernies form the strongest node if new Fernies are not enough
			if (diff > 0) {
				useFernies += diff;
				move += takeFerniesFromNodeMoveBuilder(myMax, diff);
				budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - diff);
			} else {
				useFernies = gm.getNewFernies();
				move += takeFerniesFromNodeMoveBuilder(myMax, useFernies);
				budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - useFernies);
			}

		}

		// Number of Fernies distributed across all visible neutral node
		int perNode = (int) Math.floor((int) (Math.max(1, useFernies / Math.max(gm.getAbsVis() * 2, 1))));

		// Distributing the Fernies on the neutral nodes
		for (int i = 1; i <= gm.getAbsVis(); i++) {

			// Stop if no Fernies left
			if (useFernies <= 0)
				break;

			// Calculate positions around the strongest node
			int right = GameUtils.Ring.correctIndex(myMax.getIndex() + i + gm.getAbsVis() * (gm.getStep() - 1),
					gm.getRingSize());
			int left = GameUtils.Ring.correctIndex(myMax.getIndex() - i - gm.getAbsVis() * (gm.getStep() - 1),
					gm.getRingSize());
			
			// Getting the nodes to place 
			Node r = gm.getRing().get(right);
			Node l = gm.getRing().get(left);
			
			// Place only on neutral or owned nodes
			if (r.isNeutral() || r.getOwner().equals("Y")) {
				move += putFerniesMoveBuilder(right, perNode);
				useFernies -= perNode;
			}
			if (l.isNeutral() || l.getOwner().equals("Y")) {
				move += putFerniesMoveBuilder(left, perNode);
				useFernies -= perNode;
			}
		}
		
		// Updating the new Fernies
		gm.setNewFernies(0);

		// Place remaining Fernies back on the strongest node
		if (useFernies > 0)
			move += putFerniesMoveBuilder(myMax.getIndex(), useFernies);

		return move;
	}

	/**
	 * Used usually only in the first step of the game, but could also be used in
	 * other steps.
	 * <p>
	 * Distributes the new Fernies and Fernies from the strongest node in an
	 * aggressive way to all visible neutral (and owned) nodes, in order to occupy
	 * neutral nodes in the early game or to stabilize owned nodes. Placement is
	 * done symmetrical around the strongest node.
	 * <p>
	 * Fallback moves are applied for all previously taken Fernies that were not
	 * placed.
	 * 
	 * @param budget the budget of the Fernies available
	 * @return a move {@link String} representing the expansion action and a 
	 * 		   move {@link String} representing the fallback move if required
	 * 
	 */
	public String expandStepOneExtra(Map<Integer, Integer> budget) {

		String move = "";
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		int useFernies = gm.getNewFernies();
		gm.setNewFernies(0);

		// Get enough Fernies to expand in both directions across
		// the full visibility range
		if (useFernies < gm.getAbsVis() * 2) {
			int diff = gm.getAbsVis() * 4 - useFernies;
			// Take more Fernies form the strongest node if new Fernies are not enough
			if (diff > 0) {
				useFernies += diff;
				move += takeFerniesFromNodeMoveBuilder(myMax, diff);
				budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - diff);
			} else {
				useFernies = gm.getNewFernies();
				move += takeFerniesFromNodeMoveBuilder(myMax, useFernies);
				budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - useFernies);
			}

		}

		// Number of Fernies distributed across all visible neutral node
		int perNode = (int) Math.floor((int) (Math.max(1, useFernies / Math.max(gm.getAbsVis() * 2, 1))));

		// Take an additional fraction of Fernies from the strongest node
		// to reinforce nearby positions aggressively
		int extra = (int) Math.floor(myMax.getFernieCounts() / 4);
		move += takeFerniesFromNodeMoveBuilder(myMax, extra);
		budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - extra);

		// Symmetric aggressive distribution
		int perDirect = extra * 3 / 8; // 3/8 to direct neighbors
		int perNext = extra / 8; // 1/4 to second neighbors

		int right0 = GameUtils.Ring.correctIndex(myMax.getIndex() + 1, gm.getRingSize());
		int left0 = GameUtils.Ring.correctIndex(myMax.getIndex() - 1, gm.getRingSize());
		int right1 = GameUtils.Ring.correctIndex(myMax.getIndex() + 2, gm.getRingSize());
		int left1 = GameUtils.Ring.correctIndex(myMax.getIndex() - 2, gm.getRingSize());

		// Strengthen the neighbors
		move += putFerniesMoveBuilder(right0, perDirect);
		move += putFerniesMoveBuilder(left0, perDirect);
		move += putFerniesMoveBuilder(right1, perNext);
		move += putFerniesMoveBuilder(left1, perNext);

		// Place unused Fernies to the strongest node
		int leftover = extra - (2 * perDirect + 2 * perNext);
		if (leftover > 0)
			move += putFerniesMoveBuilder(myMax.getIndex(), leftover);

		// Distributing remaining Fernies within the visibility range symmetrical
		for (int i = 1; i <= gm.getAbsVis(); i++) {

			// Getting the correct indices on the ring
			int right = GameUtils.Ring.correctIndex(myMax.getIndex() + i, gm.getRingSize()); 
			int left = GameUtils.Ring.correctIndex(myMax.getIndex() - i, gm.getRingSize());
			
			// Getting the nodes in both directions
			Node r = gm.getRing().get(right);
			Node l = gm.getRing().get(left);
			
			// Place only on neutral or owned nodes
			if (r.isNeutral() || r.getOwner().equals("Y")) {
				move += putFerniesMoveBuilder(right, perNode);
				useFernies -= perNode;
			}
			if (l.isNeutral() || l.getOwner().equals("Y")) {

				move += putFerniesMoveBuilder(left, perNode);
				useFernies -= perNode;

			}
		}
		// Second reinforcement wave using remaining budget
		int extra2 = budget.getOrDefault(myMax.getIndex(), 0) / 4;
		move += takeFerniesFromNodeMoveBuilder(myMax, extra2);
		budget.put(myMax.getIndex(), budget.get(myMax.getIndex()) - extra2);

		int half = extra2 / 2;
		int lonely = extra2 - 2 * half;
		// Place Fernies to the outer edge of the visibility range
		int R = GameUtils.Ring.correctIndex(myMax.getIndex() + gm.getAbsVis(), gm.getRingSize());
		int L = GameUtils.Ring.correctIndex(myMax.getIndex() - gm.getAbsVis(), gm.getRingSize());

		move += putFerniesMoveBuilder(R, half);
		move += putFerniesMoveBuilder(L, half);
		// Place remaining Fernies back on the strongest node
		if (useFernies > 0)
			move += putFerniesMoveBuilder(myMax.getIndex(), useFernies);
		if (lonely > 0)
			move += putFerniesMoveBuilder(myMax.getIndex(), lonely);

		return move;

	}

	/**
	 * Used only in the first step of the game, since the new Fernies do not get
	 * placed.
	 * <p>
	 * Places all Fernies taken from the strongest node at the edge of the
	 * visibility range and distributes one Fernie on each visible neutral nodes.
	 * <p>
	 * Fallback moves are applied for all previously taken Fernies that were not
	 * placed.
	 * 
	 * @param budget the budget of the Fernies available
	 * @return a move {@link String} representing the expansion action and a 
	 * 		   move {@link String} representing the fallback move if required
	 */
	public String expandStepOneMaxEdge(Map<Integer, Integer> budget) {

		// Use only in the first step
		if (gm.getStep() != 1)
			return putNewFerniesSmart(budget);

		String move = "";
		// Strongest owned node, only one exists in step one
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		// Get all Fernies from the strongest node
		move += takeFerniesFromNodeMoveBuilder(myMax, budget.get(myMax.getIndex()));
		budget.put(myMax.getIndex(), 0);

		// Place all Fernies to the edge of the visibility range
		move += putFerniesMoveBuilder(myMax.getIndex() + gm.getAbsVis(), myMax.getFernieCounts());
		// Get correct Index for the visibility range

		int correctIndex = GameUtils.Ring.correctIndex(myMax.getIndex() + gm.getAbsVis(), gm.getRingSize());
		// Remove Fernies that one Fernie can be placed on each neutral visible node
		move += putFerniesMoveBuilder(correctIndex, -(gm.getAbsVis() * 2 - gm.getNewFernies()));

		// Placing one Fernies on neutral nodes
		for (int i = 0; i <= gm.getAbsVis() * 2; i++) {

			int left = GameUtils.Ring.correctIndex(correctIndex - i, gm.getRingSize());

			Node l = gm.getRing().get(left);

			if (l.isNeutral()) {
				move += putFerniesMoveBuilder(left, 1);

			}
		}

		return move;
	}

	/**
	 * Used usually only in the first step of the game, but could also be used in
	 * other steps.
	 * <p>
	 * Distributes the new Fernies and Fernies from the strongest node in an
	 * aggressive way to all visible neutral (and owned) nodes, in order to occupy
	 * neutral nodes in the early game or to stabilize owned nodes. Placement is
	 * done symmetrical around the strongest node, whereby more Fernies get placed
	 * on the edge nodes of the visibility.
	 * <p>
	 * Fallback moves are applied for all previously taken Fernies that were not
	 * placed.
	 * 
	 * @param budget the budget of the Fernies available
	 * @return a move {@link String} representing the expansion action and a 
	 * 		   move {@link String} representing the fallback move if required
	 * 
	 */
	public String expandStepOneAggressive(Map<Integer, Integer> budget) {

		String move = "";
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		// First normal symmetric expansion
		move += expandStepOneOriginal(budget);

		// Get the indices at the edge of the visibility range
		int right = GameUtils.Ring.correctIndex(myMax.getIndex() + gm.getAbsVis() + gm.getAbsVis() * (gm.getStep() - 1),
				gm.getRingSize());
		int left = GameUtils.Ring.correctIndex(myMax.getIndex() - gm.getAbsVis() - gm.getAbsVis() * (gm.getStep() - 1),
				gm.getRingSize());

		// Use Fernies of the strongest node
		int putBoarders = myMax.getFernieCounts() / 3;
		int put = putBoarders / 2;
		
		// If the number of neutral nodes were odd
		int leftover = putBoarders - 2 * put;
		
		move += takeFerniesFromNodeMoveBuilder(myMax, putBoarders);
		// Strengthen the edge borders of the visibility range
		move += putFerniesMoveBuilder(right, put);
		move += putFerniesMoveBuilder(left, put);

		// Place remaining Fernies back on the strongest node
		if (leftover > 0)
			move += putFerniesMoveBuilder(myMax.getIndex(), leftover);

		return move;
	}

}
