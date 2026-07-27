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
class StrategyDeciderTestSimple extends AbstractStrategyDecider {

	private AttackMoves am;
	private ExpansionMoves em;
	private StepOneMoves som;
	private DefensiveMoves dm;
	private Map<Integer, Integer> ferniesBudget;

	// UNUSED: therefore private.
	private StrategyDeciderTestSimple(GameModel gm, StepAnalyzer sa) {
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

		// Ensure to get the updated map for the current step
		updateBudget(ferniesBudget, sa);

		String move = "";

		// STEP 1
		if (gm.getStep() == 1) {
			return move += som.expandStepOneMaxEdge(ferniesBudget);
		}

		// Only for very defensive opponents
		if (sa.wholeRingVisible(gm) && sa.getEnemyNodesCurr().size() <= 3) {

			if (sa.getOwnedFernieSumCurr() < sa.getEnemyFerniesCurr() * 1.2) {
				move += em.getAsManyNeutralNodes(ferniesBudget, 1);
				return move += am.putNewFerniesSmart(ferniesBudget);
			}
			if (sa.getOwnedFernieSumCurr() > sa.getEnemyFerniesCurr() * 1.3)
				return move += am.fightStrongestEnemiesWithNeighbors(ferniesBudget, 0, 1.3);

			move += am.putNewFerniesSmart(ferniesBudget);
			move += em.getAsManyNeutralNodes(ferniesBudget, 1);
			move += dm.moveMax(ferniesBudget);

			return move;
		}

		// Only for a visibility with a range 1
		if (gm.getAbsVis() == 1) {

			move += em.getAsManyNodesAndFight(ferniesBudget, 1);
			move += som.expandStepOneMaxEdge(ferniesBudget);
			return move;
		}

		// Default moves
		move += am.fightWeakestEnemiesWithNeighbors(ferniesBudget, 0, 1);
		move += em.getAsManyNodesAndFight(ferniesBudget, 0.5);
		move += dm.moveMax(ferniesBudget);

		return move;
	}

}
