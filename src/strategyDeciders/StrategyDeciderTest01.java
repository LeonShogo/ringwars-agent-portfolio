
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
class StrategyDeciderTest01 extends AbstractStrategyDecider {

	private AttackMoves as;
	private ExpansionMoves es;
	private StepOneMoves io;
	private DefensiveMoves ds;
	protected Map<Integer, Integer> ferniesBudget;

	// UNUSED: therefore private.
	private StrategyDeciderTest01(GameModel gm, StepAnalyzer sa) {
		super(gm, sa);
		this.as = new AttackMoves(gm, sa);
		this.es = new ExpansionMoves(gm, sa);
		this.io = new StepOneMoves(gm, sa);
		this.ds = new DefensiveMoves(gm, sa);
		this.ferniesBudget = new HashMap<Integer, Integer>();
	}

	// UNUSED: Experimental strategy, not used in final submission.
	@Override
	public String decide() {

		String move = "";

		// Ensure to get the updated map for the current step
		updateBudget(ferniesBudget, sa);

		// Getting basic information for decision making
//	    double growth = getGrowthFactor();
		String ring = ringCategory();

		// Only for Step 1
		if (gm.getStep() == 1 && ring.equals("SMALL")) {
			return io.expandStepOneMaxEdge(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("MID")) {
			return io.expandStepOneOriginal(ferniesBudget);
		}

		if (gm.getStep() == 1 && ring.equals("LARGE")) {
			return io.expandStepOneMaxEdge(ferniesBudget);
		}

		if (ring.equals("SMALL")) {

			move += as.doOnlyBorderFights(ferniesBudget, 0, 1);
			move += as.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
			move += es.getAsManyNeutralNodes(ferniesBudget, 1);
			move += ds.moveMax(ferniesBudget);

		}

		if (ring.equals("MID")) {

			move += as.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
			move += ds.moveMax(ferniesBudget);

		}

		if (ring.equals("LARGE")) {

			move += as.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1.2);
			move += es.getAsManyNeutralNodes(ferniesBudget, 2);
			move += ds.moveMax(ferniesBudget);

		}

		return move;
	}

}
