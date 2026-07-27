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

// UNUSED: Experimental StrategyDecider, not used in the final implementation.
class StrategyDeciderTestKomplex extends AbstractStrategyDecider {

	private AttackMoves am;
	private ExpansionMoves em;
	private StepOneMoves som;
	private DefensiveMoves dm;
	protected Map<Integer, Integer> ferniesBudget;

	// UNUSED: therefore private.
	private StrategyDeciderTestKomplex(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
		this.som = new StepOneMoves(gm, sa);
		this.am = new AttackMoves(gm, sa);
		this.em = new ExpansionMoves(gm, sa);
		this.dm = new DefensiveMoves(gm, sa);
		this.ferniesBudget = new HashMap<Integer, Integer>();
	}

	// UNUSED: Experimental strategy, not used in final submission.
	@Override
	public String decide() {

		String move = "";

		// Get the strongest node for further decision making
		Node myMax = GameUtils.Analysis.nodeWithMaxFernies(sa.getOwnedNodesCurr());

		// Ensure to get the updated map for the current step
		updateBudget(ferniesBudget, sa);

		// Getting basic information for decision making
//		double growth = getGrowthFactor();
		String ring = ringCategory();

		double expandingEnemy = sa.expandingEnemy();
		double defensiveEnemy = sa.defensiveEnemy();

		// ONLY IF VISIBILTY IS RANGE 1
		if (gm.getAbsVis() == 1) {
			// Place new Fernies if strongest node is full
			if (myMax != null && myMax.getFernieCounts() >= gm.getMaxFernies())
				return move += em.newFerniesNextNeutal(ferniesBudget);
			// Else if strongest node is not full yet
			return move += em.nextNeutralMaxStep(ferniesBudget);
		}

		// Only for Step 1
		if (gm.getStep() == 1) {

			if (ring.equals("SMALL"))
				return som.expandStepOneMaxEdge(ferniesBudget);

			if (ring.equals("MID"))
				return som.expandStepOneExtra(ferniesBudget);

			if (ring.equals("LARGE"))
				return som.expandStepOneAggressive(ferniesBudget);
		}

		// If the strongest node is full
		if (myMax != null && myMax.getFernieCounts() >= gm.getMaxFernies()) {
			move += som.putNewFerniesSmart(ferniesBudget);

		}

		// Collecting if agent has only a few weak nodes left
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

		// Only for very defensive opponents
		if (sa.getEnemyNodesCurr().size() <= 3) {

			move += am.putNewFerniesSmart(ferniesBudget);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.moveMax(ferniesBudget);

			if (sa.getEnemyFerniesCurr() * 1.4 < sa.getOwnedFernieSumCurr()) {
				move += am.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.5);
			}

			return move;
		}

		// MOVES FOR EACH RING CATEGORY
		// ----------------------------------------------------

		// SMALL
		if ((ring.equals("SMALL"))) {

			// Defensive and not expanding
			if (defensiveEnemy > 0.6 && expandingEnemy < 0.5) {

				move += am.doOnlyBorderFights(ferniesBudget, 0, 1);
				move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1);
				move += dm.collect(ferniesBudget);
				move += dm.runAway(ferniesBudget);

			}

			move += am.doOnlyBorderFights(ferniesBudget, 0, 1);
			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1);
			move += dm.collect(ferniesBudget);
			move += dm.runAway(ferniesBudget);

		}

		// MID
		if ((ring.equals("MID"))) {

			// Expanding Enemy
			if (expandingEnemy > 0.5) {

				move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.3);
				move += em.getAsManyNodesAndFight(ferniesBudget, 1);
				move += dm.moveMax(ferniesBudget);

			}
			// Defensive Enemy
			if (defensiveEnemy > 0.6) {

				move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.3);
				move += am.doOnlyBorderFights(ferniesBudget, 0, 1.0);
				move += em.getAsManyNeutralNodes(ferniesBudget, 1);
				move += dm.runAway(ferniesBudget);

			}

			// Default moves
			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
			move += em.getAsManyNeutralNodes(ferniesBudget, 2);
			move += dm.moveMax(ferniesBudget);

		}

		// LARGE
		if ((ring.equals("LARGE"))) {

			// Defensive Enemy
			if (defensiveEnemy > 0.6) {

				move += am.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.3);
				move += em.getAsManyNodesAndFight(ferniesBudget, 1);
				move += am.doOnlyBorderFights(ferniesBudget, 0, 1.05);
				move += dm.runAway(ferniesBudget);

			}
			// Expanding Enemy
			if (expandingEnemy > 0.6) {

				move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
				move += em.getAsManyNeutralNodes(ferniesBudget, 2);
				move += em.getAsManyNodesAndFight(ferniesBudget, 1);
				move += dm.runAway(ferniesBudget);
			}

			// Default moves
			move += am.fightWeakestEnemiesBestPlacement(ferniesBudget, 0, 1.05);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.runAway(ferniesBudget);

			if (!sa.wholeRingVisible(gm)) {
				move += em.expandToHiddenNodes(ferniesBudget, 0);
			}
		}

		return move;
	}

}
