package strategyDeciders;

import java.util.HashMap;
import java.util.Map;

import feu.agenten.shogo_seifert.GameModel;
import feu.agenten.shogo_seifert.StepAnalyzer;
import moveCategories.AttackMoves;
import moveCategories.DefensiveMoves;
import moveCategories.ExpansionMoves;
import moveCategories.StepOneMoves;

// UNUSED: Experimental StrategyDecider, not used in the final implementation.
class StrategyDeciderTest02 extends AbstractStrategyDecider {

	private AttackMoves am;
	private ExpansionMoves em;
	private StepOneMoves som;
	private DefensiveMoves dm;
	protected Map<Integer, Integer> ferniesBudget;

	// UNUSED: therefore private.
	private StrategyDeciderTest02(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
		this.am = new AttackMoves(gm, sa);
		this.em = new ExpansionMoves(gm, sa);
		this.som = new StepOneMoves(gm, sa);
		this.dm = new DefensiveMoves(gm, sa);
		this.ferniesBudget = new HashMap<Integer, Integer>();

	}

	// UNUSED: Experimental strategy, not used in final submission.
	@Override
	public String decide() {

		String move = "";

		// Ensure to get the updated map for the current step
		updateBudget(ferniesBudget, sa);

		// Getting basic information for decision making
//		    double growth = getGrowthFactor();
		String ring = ringCategory();

		// Only for Step 1
		if (gm.getStep() == 1 && ring.equals("SMALL")) {
			return som.expandStepOneMaxEdge(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("MID")) {
			return som.expandStepOneAggressive(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("LARGE")) {
			return som.expandStepOneMaxEdge(ferniesBudget);
		}

		if (ring.equals("SMALL")) {

			move += am.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
			move += em.getAsManyNodesAndFight(ferniesBudget, 2);
			move += dm.moveMax(ferniesBudget);

		}

		if (ring.equals("MID")) {

			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.1);
			move += am.fightStrongestEnemiesBestPlacement(ferniesBudget, 0, 1.05);
			move += dm.collect(ferniesBudget);
			move += dm.runAway(ferniesBudget);

		}

		if (ring.equals("LARGE")) {

			move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.1);
			move += am.fightStrongestEnemiesBestPlacement(ferniesBudget, 0, 1.05);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.moveMax(ferniesBudget);

		}

		return move;
	}
}
