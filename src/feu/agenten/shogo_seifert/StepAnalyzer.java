package feu.agenten.shogo_seifert;

import java.util.ArrayList;
import java.util.List;

/**
 * Analyzes two different sequential game steps and provides information for the
 * strategy and decision making.
 * <p>
 * Provides methods and attributes to compare information about the current and
 * (if available) the previous {@link GameModel}.
 * <p>
 * The gathered information is analytical and be used by the strategy layer as a
 * foundation for tactical and strategic decisions.
 * 
 */
public class StepAnalyzer {

	/** Previous and current game states. */
	private final GameModel previous, current;

	/** Lists of nodes, forming the previous and current ring. */
	private List<Node> ringPrev, ringCurr;

	/** Lists of nodes owned by the agent, previous/current. */
	private List<Node> ownedNodesPrev, ownedNodesCurr;

	/** Lists of nodes owned by the enemy, previous/current. */
	private List<Node> enemyNodesPrev, enemyNodesCurr;

	/** Lists of neutral nodes, previous/current. */
	private List<Node> neutralNodesPrev, neutralNodesCurr;

	/** All currently visible nodes */
	private List<Node> allVisibleNodesCurr;

	/** Total number of Fernies owned by the agent, previous/current. */
	private int ownedFernieSumPrev, ownedFernieSumCurr;

	/** Total number of visible Fernies owned by the enemy, previous/current. */
	private int enemyFernieSumPrev, enemyFernieSumCurr;

	/** Total number of visible Fernies, previous/current. */
	private int totalFerniesPrev, totalFerniesCurr;

	/** Ownership ratio of the agent, previous/current. */
	private double ownedRatioPrev, ownedRatioCurr;

	/** Ownership ratio of the enemy, previous/current. */
	private double enemyRatioPrev, enemyRatioCurr;

	/**
	 * Constructs a new instance of a {@code StepAnalyzer} with the specified given
	 * parameters.
	 * <p>
	 * Initializes all analytical attributes based on the given {@code GameModel}
	 * instances.
	 * 
	 * @param previous {@code GameModel} or the previous step or {@code null} if
	 *                 first step
	 * @param current  the {@code GameModel} of the current step
	 */

	public StepAnalyzer(GameModel previous, GameModel current) {
		super();

		this.previous = previous;
		this.current = current;
		// Previous step is null if current is first step
		if (previous != null) {
			this.ringPrev = previous.getRing();

			this.ownedNodesPrev = GameUtils.Analysis.getMyNodes(previous);
			this.enemyNodesPrev = GameUtils.Analysis.getEnemyNodes(previous);
			this.neutralNodesPrev = GameUtils.Analysis.getNeutralNodes(previous);

			this.ownedFernieSumPrev = GameUtils.Analysis.sumFernies(GameUtils.Analysis.getMyNodes(previous));
			this.enemyFernieSumPrev = GameUtils.Analysis.sumFernies(GameUtils.Analysis.getEnemyNodes(previous));

			this.totalFerniesPrev = ownedFernieSumPrev + enemyFernieSumPrev;

			this.ownedRatioPrev = GameUtils.Analysis.ownedRatio(ownedNodesPrev, ringPrev.size());
			this.enemyRatioPrev = GameUtils.Analysis.ownedRatio(enemyNodesPrev, ringPrev.size());
		}

		if (current != null) {
			this.ringCurr = current.getRing();

			this.ownedNodesCurr = GameUtils.Analysis.getMyNodes(current);
			this.enemyNodesCurr = GameUtils.Analysis.getEnemyNodes(current);
			this.neutralNodesCurr = GameUtils.Analysis.getNeutralNodes(current);

			this.ownedFernieSumCurr = GameUtils.Analysis.sumFernies(GameUtils.Analysis.getMyNodes(current));
			this.enemyFernieSumCurr = GameUtils.Analysis.sumFernies(GameUtils.Analysis.getEnemyNodes(current));
			this.totalFerniesCurr = ownedFernieSumCurr + enemyFernieSumCurr;

			this.ownedRatioCurr = GameUtils.Analysis.ownedRatio(ownedNodesCurr, ringCurr.size());
			this.enemyRatioCurr = GameUtils.Analysis.ownedRatio(enemyNodesCurr, ringCurr.size());

			this.allVisibleNodesCurr = GameUtils.Analysis.allVisibleNodes(ownedNodesCurr, neutralNodesCurr,
					enemyNodesCurr);
		}
	}

	// ******************************************************************************************//
	// Methods that return lists

	/**
	 * Returns all nodes that are direct neighbors to enemy nodes.
	 * <p>
	 * A node is considered a border node if the distance to the nearest enemy node
	 * on the ring is exactly {@code 1}.
	 * 
	 * @param myNodes list of owned {@link Node}s
	 * @param ring    list of  {@link Node}s forming the ring
	 * @return list of owned  {@link Node}s that are adjacent to enemy  {@link Node}s
	 */

	public List<Node> borderTypeNodes(List<Node> myNodes, List<Node> ring) {

		List<Node> borderTypeNodes = new ArrayList<Node>();

		for (Node n : myNodes) {
			if (GameUtils.Ring.distanceNearestEnemy(n, ring) == 1) {
				borderTypeNodes.add(n);
			}
		}
		return borderTypeNodes;
	}

	/**
	 * Returns all nodes that are threatened by enemy nodes.
	 * <p>
	 * A node is considered in danger when the distance to the nearest enemy node on
	 * the ring is exactly {@code <=2}.
	 * 
	 * @param myNodes list of owned  {@link Node}s
	 * @param ring    list of  {@link Node}s forming the ring
	 * @return list of owned  {@link Node}s that are adjacent to enemy  {@link Node}s
	 */
	public List<Node> inDangerNodes(List<Node> myNodes, List<Node> ring) {

		List<Node> inDangerNodes = new ArrayList<Node>();

		for (Node n : myNodes) {
			if (GameUtils.Ring.distanceNearestEnemy(n, ring) <= 2) {
				inDangerNodes.add(n);
			}
		}
		return inDangerNodes;
	}

	// ******************************************************************************************//
	// Analytical methods

	/**
	 * Checks whether the whole ring is visible.
	 * 
	 * @param gm {@link GameModel} providing the current game state
	 * @return {@code true} if no invisible  {@link Node} was found
	 */

	public boolean wholeRingVisible(GameModel gm) {
		for (Node n : gm.getRing()) {
			if (n.getOwner().equals("U")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks whether a given node is visible to the enemy.
	 * 
	 * @param n     {@link Node} to check if its visible to the enemy
	 * @param ring list of  {@link Node}s that is forming the ring
	 * @param gm   {@link GameModel} providing the absolute visibility
	 * @return {@code true} if the given node is visible to the enemy
	 */

	public boolean isVisibleToEnemy(Node n, List<Node> ring, GameModel gm) {
		int visibility = gm.getAbsVis();
		int enemyDis = GameUtils.Ring.distanceNearestEnemy(n, ring);
		return (enemyDis <= visibility);
	}

	// ******************************************************************************************//
	// Methods that return boolean for events or game states

	/**
	 * Checks whether the agent lost Fernies in total in the last step.
	 * 
	 * @return {@code true} if the agent's total number of Fernies decreased
	 */
	public boolean lostFernies() {
		if (previous == null)
			return false;
		return (ownedFernieSumCurr - ownedFernieSumPrev < 0);
	}

	/**
	 * Checks whether the enemy lost Fernies in total in the last step.
	 * 
	 * @return {@code true} if the enemy's total number of Fernies decreased
	 */
	public boolean enemyLostFernies() {
		if (previous == null)
			return false;
		return (enemyFernieSumCurr - enemyFernieSumPrev < 0);
	}

	/**
	 * Checks whether the agent gained nodes in the last step.
	 *
	 * @return {@code true} if the agent's total number of nodes increased
	 */
	public boolean gainedNodes() {
		if (previous == null)
			return true;
		return this.ownedNodesCurr.size() > this.ownedNodesPrev.size();
	}

	/**
	 * Checks whether the enemy gained nodes in the last step.
	 *
	 * @return {@code true} if the agent's total number of Fernies increased
	 */
	public boolean enemyGainedNodes() {
		if (previous == null)
			return true;
		return this.enemyNodesCurr.size() > this.enemyNodesPrev.size();
	}

	/**
	 * Checks whether the agent made progress in the last step.
	 * <p>
	 * Progress is defined as an increase in total Fernies AND nodes OR an increased
	 * ownership ratio combined with a decreased ownership of the enemy.
	 * 
	 * @return {@code true} if the agent made progress
	 */
	public boolean madeProgress() {
		if (previous == null)
			return true;
		return ((ownedFernieSumPrev < ownedFernieSumCurr && ownedNodesPrev.size() < ownedNodesCurr.size())
				|| (ownedRatioPrev < ownedRatioCurr && enemyRatioPrev > enemyRatioCurr));
	}

	// ******************************************************************************************
	// Comparison methods that return integers

	/**
	 * Calculates the difference between the total number of Fernies owned by the
	 * agent in the previous and the current step.
	 * 
	 * @return a positive value if the agent gained Fernies, zero if unchanged, and
	 *         a negative value if the agent lost Fernies
	 */
	public int myFerniesComparator() {
		if (previous == null)
			return 0;
		return ownedFernieSumCurr - ownedFernieSumPrev;
	}

	/**
	 * Calculates the difference between the number of nodes owned by the agent in
	 * the previous and the current step.
	 * 
	 * @return a positive value if the agent gained  {@link Node}s, zero if unchanged, and a
	 *         negative value if the agent lost  {@link Node}s
	 * 
	 */
	public int myNodesComparator() {
		if (previous == null)
			return 0;
		return ownedNodesCurr.size() - ownedNodesPrev.size();
	}

	/**
	 * Calculates the difference between the number of visible enemy nodes in the
	 * previous and the current step.
	 * 
	 * @return a positive value if more enemy  {@link Node}s are visible, zero if unchanged,
	 *         and a negative value if fewer enemy nodes are visible
	 */
	public int visibleEnemiesComparator() {
		if (previous == null)
			return 0;
		return enemyNodesCurr.size() - enemyNodesPrev.size();
	}

	/**
	 * Calculates the difference between the number of total visible Fernies owned
	 * by the enemy in the previous and the current step.
	 * 
	 * @return a positive value if the enemy gained Fernies, zero if unchanged, and
	 *         a negative value if the enemy lost Fernies
	 * 
	 */
	public int enemyFerniesComparator() {
		if (previous == null)
			return 0;
		return enemyFernieSumCurr - enemyFernieSumPrev;
	};

	// ******************************************************************************************//
	// Rating methods and methods that interpret steps

	/**
	 * Rates the enemy's offensive behavior based on changes between the current and the previous step. 
	 * <p>
	 * The rating is calculated by adding indicators. The maximum rating is at {@code 1}.
	 * 
	 * @return a double value between {@code 0.0} and {@code 1.0} representing the  
	 * 			estimated offensive behavior of the enemy
	 */
	public double offenseEnemy() {

		double rating = 0.0;
		if (previous == null)
			return 0;

		if (enemyNodesCurr.size() < enemyNodesPrev.size())
			rating += 0.3;
		if (!enemyGainedNodes())
			rating += 0.2;
		if (wasAGoodMove())
			rating += 0.1;
		if (ownedNodesCurr.size() > enemyNodesCurr.size())
			rating += 0.1;
		if (ownedFernieSumCurr <= enemyFernieSumPrev)
			rating += 0.2;
		if (enemyLostFernies() && wholeRingVisible(current))
			rating += 0.3;
		// Maximum rating of 1.0
		if (rating > 1)
			rating = 1;
		return rating;
	}

	/**
	 * Rates the enemy's defensive behavior based on changes between the current and the previous step. 
	 * <p>
	 * The rating is calculated by adding indicators. The maximum rating is at {@code 1}.
	 * 
	 * @return a double value between {@code 0.0} and {@code 1.0} representing the  
	 * 			estimated defensive behavior of the enemy
	 */
	public double defensiveEnemy() {

		double rating = 0.0;
		if (previous == null)
			return 0;

		if (!enemyLostFernies())
			rating += 0.25;
		if (!enemyGainedNodes())
			rating += 0.25;
		if (lostFernies())
			rating += 0.35;
		if (enemyRatioCurr <= enemyRatioCurr)
			rating += 0.2;
		
		// Maximum rating of 1.0
		if (rating > 1)
			rating = 1;

		return rating;
	}

	/**
	 * Rates the enemy's expanding behavior based on changes between the current and the previous step. 
	 * <p>
	 * The rating is calculated by adding indicators. The maximum rating is at {@code 1}.
	 * 
	 * @return a double value between {@code 0.0} and {@code 1.0} representing the  
	 * 			estimated expanding behavior of the enemy
	 */
	public double expandingEnemy() {

		double rating = 0.0;
		if (previous == null)
			return 0;

		if (enemyRatioCurr > enemyRatioPrev)
			rating += 0.5;
		if (enemyGainedNodes())
			rating += 0.35;
		if (!enemyLostFernies())
			rating += 0.25;
		
		// Maximum rating of 1.0
		if (rating > 1)
			rating = 1;

		return rating;
	}

	/**
	 * Checks whether the last step can be considered mostly neutral.
	 * <p>
	 * A move is considered as neutral if the ownership ratios, the nodes counts and
	 * visible enemy nodes did not changes significantly.
	 * 
	 * @return {@code true} if the move was mostly neutral.
	 */
	public boolean wasANeutralMove() {
		if (this.ownedRatioCurr == this.ownedRatioPrev && this.myNodesComparator() == 0
				&& this.visibleEnemiesComparator() == 0
				&& Math.abs(this.neutralNodesCurr.size() - this.neutralNodesPrev.size()) <= 2)
			return true;
		else
			return false;
	}

	/**
	 * Determines whether the last step can be considered a good move.
	 * 
	 * @return {@code true} if the agent made progress, did not lose Fernies, and
	 *         the enemy lost Fernies
	 */
	public boolean wasAGoodMove() {
		return this.madeProgress() && !this.lostFernies() && this.enemyLostFernies();
	}

	/**
	 * Calculates a heuristic rating for the last step.
	 * 
	 * @return a score representing the quality of the last step
	 */
	public double stepRating() {
		double Rating = 0;
		if (this.madeProgress())
			Rating += 0.25;
		if (!this.lostFernies())
			Rating += 0.05;
		if (this.enemyLostFernies())
			Rating += 0.2;
		if (this.ownedRatioPrev < this.ownedRatioCurr)
			Rating += 0.1;
		if (this.gainedNodes())
			Rating += 0.15;
		if (this.enemyGainedNodes())
			Rating -= 3;
		if (this.myFerniesComparator() > 0)
			Rating += 0.5;
		if (this.enemyFerniesComparator() > 0)
			Rating += -0.5;
		if (this.myNodesComparator() > 0)
			Rating += 1.5;
		if (this.visibleEnemiesComparator() > 0)
			Rating += -3;
		return Rating;
	}

	// ******************************************************************************************//
	// GETTERS

	/**
	 * Gets the previous {@link GameModel}.
	 * 
	 * @return the current {@link GameModel} representing the previous game state
	 */
	public GameModel getPreviousGameModel() {
		return previous;
	}

	/**
	 * Gets the current {@link GameModel}.
	 * 
	 * @return the current {@link GameModel} representing the current game state
	 */
	public GameModel getCurrentGameModel() {
		return current;
	}

	/**
	 * Gets a list of nodes currently owned by the agent in this step.
	 * 
	 * @return a list of  {@link Node}s owned by the agent
	 */
	public List<Node> getOwnedNodesCurr() {
		return ownedNodesCurr;
	}

	/**
	 * Gets a list of nodes currently owned by the enemy in this step.
	 * 
	 * @return a list of  {@link Node}s owned by the enemy
	 */
	public List<Node> getEnemyNodesCurr() {
		return enemyNodesCurr;
	}

	/**
	 * Gets a list of currently neutral nodes.
	 * 
	 * @return a list of currently neutral  {@link Node}s
	 */
	public List<Node> getNeutralNodesCurr() {
		return neutralNodesCurr;
	}

	/**
	 * Gets a list of all currently visible node.
	 * 
	 * @return a list of all currently visible  {@link Node}
	 */
	public List<Node> getAllVisibleNodesCurr() {
		return allVisibleNodesCurr;
	}

	/**
	 * Gets the total number of Fernies currently owned by the agent.
	 * 
	 * @return the total number of Fernies currently owned by the agent
	 */
	public int getOwnedFernieSumCurr() {
		return ownedFernieSumCurr;
	}

	/**
	 * Gets the total number of currently visible Fernies owned by the enemy.
	 * 
	 * @return the total number of currently visible Fernies owned by the enemy
	 */
	public int getEnemyFerniesCurr() {
		return enemyFernieSumCurr;
	}

	/**
	 * Gets the total number of currently visible Fernies.
	 * 
	 * @return the total number of currently visible Fernies
	 */
	public int getTotalFerniesCurr() {
		return totalFerniesCurr;
	}

	/**
	 * Gets the total number of visible Fernies of the previous step.
	 * 
	 * @return the total number of visible Fernies of the previous step
	 */
	public int getTotalFerniesPrev() {
		return totalFerniesPrev;
	}

	/**
	 * Gets the ownership ratio of nodes owned by the agent.
	 * 
	 * @return the ownership ration of nodes owned by the agent
	 */
	public double getOwnedRatioCurr() {
		return ownedRatioCurr;
	}

	/**
	 * Gets the ownership ration of nodes owned by the enemy.
	 * 
	 * @return the ownership ration of nodes owned by the enemy
	 */
	public double getEnemyRatioCurr() {
		return enemyRatioCurr;
	}

}
