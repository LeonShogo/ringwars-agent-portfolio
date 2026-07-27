package strategyDeciders;

import java.util.HashMap;
import java.util.Map;

import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.GameUtils;
import feu.agenten.shogo_seifert.Node;
import feu.agenten.shogo_seifert.StepAnalyzer;
import moveCategories.AttackMoves;
import moveCategories.DefensiveMoves;
import moveCategories.ExpansionMoves;
import moveCategories.StepOneMoves;

// ------------------ THE FINAL SUBMISSION STRATEGY DECIDER ------------------------ //

/**
 * This StrategyDecider is a concrete implementation of
 * {@link AbstractStrategyDecider}. It defines the final decision logic and
 * strategic calculations used by the agent.
 * 
 * <p>
 * This StrategyDecider is the strategy on which the agent decides its moves. It
 * combines aggressively attacking the visible enemy nodes with expansion by
 * occupying neutral nodes, fallback mechanism and hiding the strongest node
 * form the enemy.
 * <p>
 * This StrategyDecider adapts its behavior mainly based on the current ring
 * size.
 * <ul>
 * <li>the current ring size,</li>
 * <li>the visibility (own strategy for visibility with range {@code <= 2},</li>
 * <li>the enemy's behavior in terms of expansion and defense.</li>
 * </ul>
 * It also checks if the strongest node has reached the maximum capacity per
 * node and.
 * 
 */
public class StrategyDeciderFINALSUBMISSION extends AbstractStrategyDecider {

	/** Collection of attacking moves. */
	private AttackMoves am;

	/** Collection of expanding moves. */
	private ExpansionMoves em;

	/** Collection of moves for the first game step. */
	private StepOneMoves som;

	/** Collection of defensive moves. */
	private DefensiveMoves dm;

	/** Budget tracking for Fernies within a single step. */
	protected Map<Integer, Integer> ferniesBudget;

	/**
	 * Construct a new instance of a {@code StrategyDeciderFINALSUBMISSION} with the
	 * specific {@link GameModel} and {@link StepAnalyzer}.
	 * 
	 * @param gm the current {@link GameModel} representing the game state of the
	 *           current step
	 * @param sa the {@link StepAnalyzer}.
	 */
	public StrategyDeciderFINALSUBMISSION(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);

		this.am = new AttackMoves(gm, sa);
		this.em = new ExpansionMoves(gm, sa);
		this.som = new StepOneMoves(gm, sa);
		this.dm = new DefensiveMoves(gm, sa);
		this.ferniesBudget = new HashMap<Integer, Integer>();
	}

	/**
	 * Decides and composes the move for each step.
	 * <p>
	 * Decisions are mainly based on the ring size of the current game, the
	 * visibility and the enemy's behavior. The method combines multiple moves from
	 * different move categories in one single move String.
	 * 
	 * @return a valid move {@link String} representing the combination of all
	 *         executed moves in the current step. If no move was executed, an empty
	 *         {@link String} will be returned.
	 * 
	 */
	@Override
	public String decide() {

		String move = "";

		// Ensure to get the updated map of the Fernies budget for the current step
		updateBudget(ferniesBudget, sa);

		// Getting the current strongest node for decision making
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		// Getting basic information for decision making
//	     double growth = getGrowthFactor();
		String ring = ringCategory();

		// Get enemy characteristics
		double expandingEnemy = sa.expandingEnemy();
		double defensiveEnemy = sa.defensiveEnemy();

		// ONLY IF VISIBILTY IS RANGE <= 2
		if (gm.getAbsVis() <= 2) {

			// Placing new Fernies if strongest node is full
			if (myMax != null && myMax.getFernieCounts() >= gm.getMaxFernies())
				return move += em.newFerniesNextNeutal(ferniesBudget);

			// Else if strongest node is not full yet
			return move += em.nextNeutralMaxStep(ferniesBudget);
		}

		// ONLY FOR STEP 1: do different expanding opening moves based on the ring size
		if (gm.getStep() == 1 && ring.equals("SMALL")) {
			return som.expandStepOneExtra(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("MID")) {
			return som.expandStepOneOriginal(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("LARGE")) {
			return som.expandStepOneExtra(ferniesBudget);
		}

		// Only for very defensive, stocking opponents
		if (sa.getEnemyNodesCurr().size() <= 3) {

			move += am.putNewFerniesSmart(ferniesBudget);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.moveMax(ferniesBudget);

			if (sa.getEnemyFerniesCurr() * 1.4 < sa.getOwnedFernieSumCurr()) {
				move += am.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.5);
			}
			return move;
		}

		// Distribute the new Fernies if the strongest node has reached the maximum
		// capacity per node
		if (myMax != null && myMax.getFernieCounts() >= gm.getMaxFernies() && gm.getAbsVis() > 1) {
			move += em.putNewFerniesSmart(ferniesBudget);

		}

		// End game and emergency behavior when the agent is very weak:
		// collecting all Fernies and retreat from the enemy,
		// only do border fights to absorb Fernies.
		if (sa.getOwnedFernieSumCurr() <= 10 && sa.getOwnedNodesCurr().size() > 1) {

			if (sa.getOwnedFernieSumCurr() < sa.getEnemyFerniesCurr()) {

				move += dm.collect(ferniesBudget);
				move += dm.runAway(ferniesBudget);

				return move;

			} else {
				move += dm.collect(ferniesBudget);
				move += am.doOnlyBorderFights(ferniesBudget, 0, 1);
				move += dm.runAway(ferniesBudget);

				return move;
			}
		}

		// Do strong, aggressive attacks against really defensive opponents
		if (defensiveEnemy > 0.8) {
			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.25);
		}

		// Strategy for SMALL rings
		if (ring.equals("SMALL")) {

			move += am.fightStrongestEnemiesBestPlacement(ferniesBudget, 0, 1.1);
			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.1);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.collect(ferniesBudget);
			move += dm.runAway(ferniesBudget);

			return move;

		}

		// Strategy for MID rings
		if (ring.equals("MID")) {

			move += am.fightWeakestEnemiesBestPlacement(ferniesBudget, 0, 1.1); // with neighbors
			move += am.fightStrongestEnemiesBestPlacement(ferniesBudget, 0, 1.1);
			move += em.getAsManyNeutralNodes(ferniesBudget, 2);
			move += dm.collect(ferniesBudget);
			move += dm.runAway(ferniesBudget);

			return move;

		}

		// Strategy for LARGE rings
		if (ring.equals("LARGE")) {

			// Fighting stronger if the enemy is defensive and expanding
			if (gm.getStep() >= 2 && defensiveEnemy > 0.7 && expandingEnemy > 0.6) {

				move += am.fightWeakestEnemiesBestPlacement(ferniesBudget, 0, 1.5);
				move += am.doOnlyBorderFights(ferniesBudget, 0, 1.1);
				move += em.getAsManyNeutralNodes(ferniesBudget, 1);
				move += dm.runAway(ferniesBudget);

				return move;

			} else {

				move += am.fightWeakestEnemiesBestPlacement(ferniesBudget, 0, 1.1);
				move += am.fightStrongestEnemiesBestPlacement(ferniesBudget, 0, 1);
				move += em.getAsManyNeutralNodes(ferniesBudget, 1);
				move += dm.runAway(ferniesBudget);
				move += dm.collect(ferniesBudget);

				return move;
			}

		}

		return move;
	}
}
