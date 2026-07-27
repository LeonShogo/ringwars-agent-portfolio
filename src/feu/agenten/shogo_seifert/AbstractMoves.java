package feu.agenten.shogo_seifert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import feu.agenten.shogo_seifert.GameUtils.MoveResult;

/**
 * The abstract base class for all Move-classes.
 * 
 * {@code AbstractMoves} provides functionality, common methods and fall-back
 * moves that are required by other, concrete Move-subclasses.
 * 
 */

public abstract class AbstractMoves {

	/** The game model provides the game state of the current step. */
	protected final GameModel gm;

	/** Analyzer for comparing and interpreting step-based information. */
	protected final StepAnalyzer sa;

	/**
	 * Constructs a new {@code AbstractMoves} instance with the specified
	 * {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel}
	 * @param sa the {@link StepAnalyzer}
	 */
	public AbstractMoves(GameModel gm, StepAnalyzer sa) {
		super();
		this.gm = gm;
		this.sa = sa;
	}

	/**
	 * Returns the total available budget.
	 * 
	 * @param budget the budget map that contains all nodes and the amount of
	 *               Fernies of each node
	 * 
	 * @return the total available amount of Fernies
	 */
	protected int getNodesBudget(Map<Integer, Integer> budget) {
		int total = 0;
		for (int value : budget.values())
			total += value;
		return total;
	}

	/**
	 * This method builds a move String of multiple moves that take Fernies from
	 * owned nodes, updates the budget map accordingly and return also the sum of
	 * the taken Fernies. The combined move String and the sum are attributes of the
	 * returned {@link MoveResult} object.
	 * <p>
	 * The method iteratively takes Fernies from owned nodes until the given number,
	 * which is the parameter {@code necessary} is reached or no further Fernies can
	 * be taken.
	 * <p>
	 * For each {@code double} entry in {@code take}, the budget entries are
	 * processed in the order, given as the {@code order} parameter. (@code ASC,
	 * DEC, RND}.
	 * <p>
	 * For each node, a part of the Fernie on it gets taken. The {@code limit}
	 * ensures, that at least this number of Fernies will remain on the node.
	 * <p>
	 * The number of Fernies of each node gets validated against the current game
	 * state provided by the {@link GameModel} gm to prevent inconsistencies.
	 * 
	 * @param budget    the map of {@link Node}s of the currently available Fernies per
	 *                  owned {@link Node}
	 * @param gm        the current {@link GameModel} representing the current game state
	 * @param limit     the minimum of Fernies that should remain on each node
	 * @param necessary the number of Fernies that should be taken
	 * @param take      the fractions defining how aggressively Fernies should be
	 *                  taken each iteration
	 * @param order     the order in which the owned {@link Node}s are iterated over
	 * @return a {@link MoveResult} object that contains and represents the combined
	 *         move {@link String} of taken Fernies and the total number of taken Fernies
	 */
	protected MoveResult takeFerniesFromBudget(Map<Integer, Integer> budget, GameModel gm, int limit, int necessary,
			double[] take, OrderMap order) {

		// All move operations will be combined in this String
		String move = "";

		// The Fernies that still need to be taken
		int rest = necessary;

		// The amount of Fernies taken
		int sumTaken = 0;

		// Iterating over the take fractions
		for (double t : take) {
			if (rest <= 0)
				break;

			// Sortable List of the current budget map
			List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(budget.entrySet());

			// Sort nodes according the given order
			switch (order) {
			case ASC -> sorted.sort(Map.Entry.comparingByValue());
			case DEC -> sorted.sort(Map.Entry.<Integer, Integer>comparingByValue().reversed());
			case RND -> Collections.shuffle(sorted);
			}

			// Trying to take Fernies from each node
			for (Map.Entry<Integer, Integer> entry : sorted) {
				if (rest <= 0)
					break;

				int index = entry.getKey();
				int budgetAvail = entry.getValue();

				// Checking the real availability against the real game state
				int realAvail = gm.getRing().get(index).getFernieCounts();

				// Use the minimum to be safe
				int available = Math.min(budgetAvail, realAvail);

				// The node should have at least the number of "limit" remaining after the move
				if (available <= limit)
					continue;

				// Calculating the number of Fernies on the fraction of this iteration
				int candidate = (int) Math.ceil(available * t);
				candidate = Math.min(candidate, available);

				// Taking only the amount that has to be taken
				int took = Math.min(candidate, rest);

				// Safety: the node should keep "limit" Fernies on it
				if (available - took < limit)
					continue;

				// Finally: take the Fernies from the node, not more than available,
				// not more than needed, and not more than the limit allows
				move += takeFerniesFromNodeMoveBuilder(gm.getRing().get(index), took);

				// Updating the budget map
				budget.put(index, budgetAvail - took);

				// Updating the counters
				rest -= took;
				sumTaken += took;
			}
			// Stop to take if the necessary number of Fernies is reached
			if (sumTaken >= necessary)
				break;
		}
		// Return the move String representing all moves that took Fernies and the total
		// sum of
		// taken Fernies as a MoveResult object
		return new MoveResult(move, sumTaken);
	}

	/**
	 * Builds and returns a String in correct move format from the given node and
	 * the given number of Fernies.
	 * 
	 * @param takeFrom the {@link Node} from which the Fernies should be taken
	 * @param part     the fixed number of Fernies that has to be taken
	 * @return a correct formatted {@link String} representing the move
	 */
	protected String takeFerniesFromNodeMoveBuilder(Node takeFrom, int part) {
		int limit = Math.min(takeFrom.getFernieCounts(), part);
		return takeFrom.getIndex() + "," + -limit + "\n";
	}

	/**
	 * Builds and returns a String in a correct format from the given index and the
	 * given number of Fernies.
	 * 
	 * @param nodeIndex the index of the target {@link Node}
	 * @param Fernies   the amount of Fernies to place
	 * @return a correct formatted {@link String} representing the move
	 */
	protected String putFerniesMoveBuilder(int nodeIndex, int Fernies) {
		// Building a correct formatted String for the move.txt file
		return nodeIndex + "," + Fernies + "\n";
	}

	/**
	 * Attempts to attack a given enemy node with a given number of Fernies.
	 * <p>
	 * If the attack is not possible due to the strength of the enemy node combined
	 * with the strengths of its neighbors, a fallback move is applied.
	 * 
	 * @param enemy      the enemy {@link Node} that gets attacked
	 * @param myNodes    the list of {@link Node}s owned by the agent
	 * @param ferniesUse the number of Fernies available for attacking the enemy
	 *                   {@link Node}
	 * @param budget     the budget of the Fernies currently available
	 * @return a move {@link String} representing the action, or an empty {@link String} if the
	 *         given enemy node was null
	 */
	public String fightEnemyWithAbsFernies(Node enemy, List<Node> myNodes, int ferniesUse,
			Map<Integer, Integer> budget) {

		String move = "";

		if (enemy == null)
			return "";

		// Get the best placement to attack the enemy node
		int bestPlacement = GameUtils.Combat.getBestPlacement(enemy, gm);

		Node bestNode = gm.getRing().get(bestPlacement);

		// Calculate the enemy strength including both neighbors
		int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(bestNode, gm);

		// Only fight when given Fernies are stronger than the enemy
		if (enemyFernies < ferniesUse) {

			move += putFerniesMoveBuilder(bestNode.getIndex(), ferniesUse);

		} else {
			// Fallback, use Fernies in a defensive move
			move += absFerniesRandomNeutralNode(budget, ferniesUse);
		}
		return move;
	}

	/**
	 * Attempts to attack a given enemy node by calculating the best placement for
	 * it.
	 * <p>
	 * If the attack is not possible due to the strength of the enemy node combined
	 * with the strengths of its neighbors, a fallback move is applied. Other
	 * fallback moves are applied for different edge cases.
	 * 
	 * @param enemy   the enemy {@link Node}s that gets attacked
	 * @param myNodes the list of {@link Node}s owned by the agent
	 * @param budget  the budget of the Fernies currently available
	 * @param limit   minimum number of Fernies that shall stay on a {@link Node} after the
	 *                move
	 * @return move {@link String} representing the action, or an empty {@link String} if the given
	 *         enemy {@link Node} was null
	 */
	public String fightEnemy(Node enemy, List<Node> myNodes, Map<Integer, Integer> budget, int limit) {

		String move = "";

		// Fallback 1 if owned nodes are not available, perform do defensive move
		if (myNodes == null || myNodes.isEmpty())
			return putNewFerniesOnMaxNode(budget);

		// Get all available Fernies
		int myBudget = getNodesBudget(budget);

		// Get the best placement to attack the enemy node
		int bestPlacement = GameUtils.Combat.getBestPlacement(enemy, gm);

		Node bestNode = gm.getRing().get(bestPlacement);

		// Calculate the enemy strength including both neighbors
		int enemyFernies = GameUtils.Combat.getEnemyStrengthWithNeighbours(bestNode, gm);

		double[] take = { 0.5, 1.0 };

		// Calculate the number of Fernies for attack efficiently
		int necessary = (int) Math.ceil(enemyFernies * 1.05);

		if (myBudget >= necessary) {

			// Take Fernies from owned nodes based on the budget, the limit and other constraints
			MoveResult mR = takeFerniesFromBudget(budget, gm, limit, necessary, take, OrderMap.ASC);
			
			int available = mR.take + gm.getNewFernies();

			// Safety
			int put = Math.max(mR.take, Math.min(available, necessary));

			move += mR.move;
			move += putFerniesMoveBuilder(bestNode.getIndex(), put);
			gm.setNewFernies(0);

		} else {
			// Fallback 2 if budget is not enough for attacking
			move += putNewFerniesOnMaxNode(budget);
		}
		// Fallback 3 if new Fernies were not used
		if (move.isEmpty() && gm.getNewFernies() > 0) {
			move += putNewFerniesOnMaxNode(budget);
		}
		return move;
	}

	/**
	 * Places the new Fernies on the node owned by the agent with the highest number
	 * of Fernies.
	 * <p>
	 * For several edge cases, fallback moves are applied.
	 * 
	 * @param budget the budget of the Fernies currently available
	 * @return a {@link String} move representing the action or an empty {@link String} if no
	 *         maxNode was found
	 */
	public String putNewFerniesOnMaxNode(Map<Integer, Integer> budget) {

		String move = "";
		
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		if (myMax == null)
			return "";

		int index = myMax.getIndex();

		int available = budget.getOrDefault(index, myMax.getFernieCounts());

		// If myMax is nearly full, place only the difference
		// until maximum capacity per Node
		int possible = gm.getMaxFernies() - available;

		// Safety: fallback move if myMax is full
		if (possible <= 0) {
			return newFerniesRandomMyOrNeutralNode(budget);
		}

		int put = Math.min(possible, gm.getNewFernies());
		// Fallback 2 if no Fernies can be placed
		if (put <= 0) {
			return newFerniesRandomMyOrNeutralNode(budget);
		}

		move += putFerniesMoveBuilder(index, put);
		budget.put(index, available + put);
		
		// Update the new Fernies
		gm.useNewFernies(put);
		return move;
	}

	/**
	 * Distributes the new Fernies on nodes that have not reached the maximum
	 * capacity per node set by the game.
	 * <p>
	 * If the preferred action is not possible or for edge cases, a fallback move is
	 * applied.
	 * 
	 * @param budget  the budget of the Fernies currently available
	 * @return a move {@link String} representing the action or fallback move or an empty
	 *         {@link String} if the new Fernies were {@code <=0}
	 * 
	 */
	public String putNewFerniesSmart(Map<Integer, Integer> budget) {
		String move = "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		
		if (myMax == null) return "";

		int newFernies = gm.getNewFernies();
		// Use all new Fernies in this move
		gm.setNewFernies(0);

		if (newFernies <= 0)
			return "";

		// Fallback 1 if myMax is full and the only owned node
		if (sa.getOwnedNodesCurr().size() == 1 && myMax.getFernieCounts() == gm.getMaxFernies())
			return newFerniesRandomMyOrNeutralNode(budget);

		// Get all nodes that have not reached the maximum capacity per node
		List<Node> targets = new ArrayList<>();
		for (Node n : sa.getOwnedNodesCurr()) {
			int available = budget.getOrDefault(n.getIndex(), n.getFernieCounts());
			if (available < gm.getMaxFernies()) {
				targets.add(n);
			}
		}
		// Fallback 2 if all owned nodes have reached the maximum capacity
		if (targets.isEmpty()) {
			if (!sa.getOwnedNodesCurr().isEmpty()) {
				move += newFerniesRandomMyOrNeutralNode(budget);
				return move;
			}
		}

		// Sort based on the distance to strongest owned node in ascending order
		targets.sort((a, b) -> {
			int da = GameUtils.Ring.distanceOnRing(myMax.getIndex(), a.getIndex(), gm.getRingSize());
			int db = GameUtils.Ring.distanceOnRing(myMax.getIndex(), b.getIndex(), gm.getRingSize());
			return Integer.compare(da, db);
		});

		// Distributing the new Fernies
		for (Node t : targets) {
			if (newFernies <= 0)
				break;

			int available = budget.getOrDefault(t.getIndex(), t.getFernieCounts());
			int diff = gm.getMaxFernies() - available;
			int put = Math.min(diff, newFernies);

			move += putFerniesMoveBuilder(t.getIndex(), put);
			// Update the budget of the nodes where Fernies were placed
			budget.put(t.getIndex(), available + put);
			// Update the new Fernies container
			newFernies -= put;
		}

		return move;
	}

	/**
	 * Distributes the a fixed and given number of Fernies on nodes that have not
	 * reached the maximum capacity per node set by the game.
	 * <p>
	 * If the preferred action is not possible or for edge cases, a fallback move is
	 * applied.
	 * 
	 * @param budget     the budget of the Fernies currently available
	 * @param absFernies fixed number of Fernies to place on {@link Node}s
	 * @return a move {@link String} representing the action or fallback move or an empty
	 *         {@link String} if the new Fernies were {@code <=0}
	 * 
	 */
	public String putAbsFerniesSmart(Map<Integer, Integer> budget, int absFernies) {
		String move = "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());
		if (myMax == null)
			return "";
		// Get the Fernies to put
		int putFernies = absFernies;

		if (putFernies <= 0)
			return "";

		// Fallback 1 if myMax is full and the only owned node
		if (sa.getOwnedNodesCurr().size() == 1 && myMax.getFernieCounts() == gm.getMaxFernies())
			return absFerniesRandomNeutralNode(budget, absFernies);

		// Get all nodes that have not reached the maximum capacity
		List<Node> targets = new ArrayList<>();
		for (Node n : sa.getOwnedNodesCurr()) {
			int available = budget.getOrDefault(n.getIndex(), n.getFernieCounts());
			if (available < gm.getMaxFernies()) {
				targets.add(n);
			}
		}
		// Fallback 2 if all owned nodes have reached the maximum capacity
		if (targets.isEmpty()) {
			if (!sa.getOwnedNodesCurr().isEmpty()) {
				move += absFerniesRandomNeutralNode(budget, absFernies);
				return move;
			}
		}

		// Sort distance to myMax in ascending order
		targets.sort((a, b) -> {
			int da = GameUtils.Ring.distanceOnRing(myMax.getIndex(), a.getIndex(), gm.getRingSize());
			int db = GameUtils.Ring.distanceOnRing(myMax.getIndex(), b.getIndex(), gm.getRingSize());
			return Integer.compare(da, db);
		});

		// Distributing the new Fernies
		for (Node t : targets) {
			if (putFernies <= 0)
				break;

			// Calculating the difference to the maximum capacity per node
			int available = budget.getOrDefault(t.getIndex(), t.getFernieCounts());
			int diff = gm.getMaxFernies() - available;
			int put = Math.min(diff, putFernies);

			move += putFerniesMoveBuilder(t.getIndex(), put);
			// Update the budget of the nodes where Fernies were placed
			budget.put(t.getIndex(), available + put);
			putFernies -= put;
		}

		// If there is a rest for Fernies available, place it somewhere
		if (putFernies > 0) {
			move += putAbsFerniesSmart(budget, putFernies);
		}

		return move;
	}

	/**
	 * Distributes the new Fernies of the step randomly on neutral nodes or nodes
	 * owned by the agent.
	 * <p>
	 * Fernies get placed on the nodes incrementally if a node has reached the
	 * maximum number of Fernies per node until the given number is exhausted.
	 * 
	 * @param budget  the budget of the Fernies currently available
	 * @return a move {@link String} representing the action or an empty {@link String} if not ring
	 *         or no neutral or owned {@link Node}s are available
	 */
	public String newFerniesRandomMyOrNeutralNode(Map<Integer, Integer> budget) {

		String move = "";
		if (gm == null || gm.getRing() == null || gm.getRing().isEmpty())
			return "";

		List<Node> nodes = new ArrayList<Node>();

		nodes.addAll(sa.getOwnedNodesCurr());
		nodes.addAll(sa.getNeutralNodesCurr());

		if (nodes.isEmpty())
			return "";

		Random random = new Random();
		
		// Loop variable to prevent endless loops
		int i = 0;

		while (gm.getNewFernies() > 0) {

			Node rand = nodes.get(random.nextInt(nodes.size()));

			int available = budget.getOrDefault(rand.getIndex(), rand.getFernieCounts());

			// Only place the difference to the maximum capacity per node
			int possible = gm.getMaxFernies() - available;
			int put = Math.min(possible, gm.getNewFernies());

			// Safety: Fallback move
			if (put <= 0) {
				return putNewFerniesSmart(budget);
			}

			move += putFerniesMoveBuilder(rand.getIndex(), put);
			
			// If the node was owned by the agent, update the budget of the node
			if (rand.getOwner().equals("Y")) {
				budget.put(rand.getIndex(), available + put);
			}
			
			// Update the new Fernies 
			gm.useNewFernies(put);

			// Safety: all nodes are full, put on a random node
			if (i >= 80) {
				Node r = nodes.get(random.nextInt(nodes.size()));
				put = gm.getNewFernies();
				move += putFerniesMoveBuilder(r.getIndex(), put);
				gm.setNewFernies(0);
			}
			i++;
		}
		return move;
	}

	/**
	 * Places new Fernies on each visible neutral node and shifts all Fernies of the
	 * strongest node on one node next to it.
	 * 
	 * This move is useful for a game setting with the visibility range of
	 * {@code 1}.
	 * 
	 * 
	 * @param budget  the budget of the Fernies currently available
	 * @return move {@link String} representing the placing action or an empty {@link String}, if
	 *         the ring is {@code null}
	 */
	public String nextNeutralMaxStep(Map<Integer, Integer> budget) {

		String move = "";
		if (gm == null || gm.getRing() == null || gm.getRing().isEmpty())
			return "";

		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		if (myMax == null)
			return "";
		
		
		// Calculate the real index on the ring 
		int index = GameUtils.Ring.correctIndex(myMax.getIndex() + gm.getAbsVis(), gm.getRingSize());

		List<Node> neutrals = sa.getNeutralNodesCurr();

		// Fallback if no neutral nodes exists
		if (neutrals.isEmpty())
			return putAbsFerniesSmart(budget, gm.getNewFernies());

		// Distributing one Fernie on each neutral node
		for (Node n : neutrals) {

			move += putFerniesMoveBuilder(n.getIndex(), 1);
			gm.useNewFernies(1);
		}

		move += putFerniesMoveBuilder(myMax.getIndex(), 1);
		
		// Update the new Fernies
		gm.useNewFernies(1);
		
		// Take all Fernies form the strongest owned node
		move += takeFerniesFromNodeMoveBuilder(myMax, myMax.getFernieCounts());
		
		// Shift all Fernies from the strongest owend node
		move += putFerniesMoveBuilder(index, myMax.getFernieCounts() + gm.getNewFernies());

		return move;
	}

	/**
	 * Places the currently available new Fernies on the next visible neutral node.
	 * This move is especially useful for game settings with a visibility range of
	 * {@code 1}, when it should be certain that new Fernies are getting placed on a
	 * neutral node.
	 *
	 * 
	 * @param budget  the budget of the Fernies currently available
	 * @return move {@link String} representing the placing action or an empty {@link String}, if
	 *         the ring is {@code null}
	 */
	public String newFerniesNextNeutal(Map<Integer, Integer> budget) {

		String move = "";
		if (gm == null || gm.getRing() == null || gm.getRing().isEmpty())
			return "";

		int newFernies = gm.getNewFernies();

		int index;

		List<Node> neutrals = sa.getNeutralNodesCurr();
		
		// Fallback if no neutral nodes exist, put the new Fernies on owned nodes
		if (neutrals.isEmpty())
			return putAbsFerniesSmart(budget, gm.getNewFernies());
		
		
		Node put = neutrals.get(0);;

		index = put.getIndex();

		move += putFerniesMoveBuilder(index, newFernies);

		return move;
	}

	/**
	 * Places a given number of Fernies on a random node out of a list of neutral
	 * nodes.
	 * <p>
	 * Usually is used as a fall back move to use the rest of Fernies not placed by
	 * other moves.
	 * 
	 * @param budget     the budget of the Fernies currently available
	 * @param absFernies the number of Fernies to place
	 * @return a move {@link String} representing the action or a {@link String}
	 * 		   representing the move of the fallback action of {@code #absFerniesRandomNode(Map, int)}
	 * 		   or an empty {@link String} if the given number of Fernies was {@code <= 0}
	 * 
	 */
	public String absFerniesRandomNeutralNode(Map<Integer, Integer> budget, int absFernies) {

		String move = "";
		
		if (absFernies <= 0)
			return "";
		
		List<Node> nodes = new ArrayList<Node>();
		
		nodes.addAll(sa.getNeutralNodesCurr());

		if (nodes.isEmpty())
			return absFerniesRandomNode(budget, absFernies);

		Random random = new Random();

		Node rand = nodes.get(random.nextInt(nodes.size()));

		move += putFerniesMoveBuilder(rand.getIndex(), absFernies);

		return move;
	}



	/**
	 * Places a given number of Fernies on a random node on the ring.
	 * <p>
	 * Usually is used as a fall back move to use the rest of Fernies not placed by
	 * other moves.
	 * 
	 * @param budget     the budget of the Fernies currently available
	 * @param absFernies the number of Fernies to place
	 * @return a move {@link String} representing the of placing on the given number of Fernies 
	 * 			on a random {@link Node} on a the ring
	 * 
	 */
	public String absFerniesRandomNode (Map<Integer, Integer> budget, int absFernies) {
		
		String move = "";

		if (absFernies <= 0)
			return "";
		
		Random random = new Random();

		Node rand = gm.getRing().get(random.nextInt(gm.getRing().size()));

		move += putFerniesMoveBuilder(rand.getIndex(), absFernies);
	
	return move;
	
	}
}
