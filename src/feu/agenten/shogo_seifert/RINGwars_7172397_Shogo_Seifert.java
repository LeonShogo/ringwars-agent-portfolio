package feu.agenten.shogo_seifert;

import strategyDeciders.AbstractStrategyDecider;
import strategyDeciders.StrategyDeciderFINALSUBMISSION;

/**
 * Entry point with the main method and main orchestration of the agent program.
 * <p>
 * The {@code RINGwars_7172397_Shogo_Seifert} coordinates the execution of one
 * game step. It controls flow and input/output handling on a high-level but
 * does not contain any strategic or logic decision making.
 * 
 * <p>
 * For each step, the {@code AgentController} executes these actions:
 * <ul>
 * <li>parses the command line arguments {@code step} and {@code name},</li>
 * <li>initializes a {@link StepReader},</li>
 * <li>reads and initializes the current and previous {@link GameModel} (if
 * available),</li>
 * <li>initializes the {@link StepAnalyzer} and {@link AbstractStrategyDecider}
 * as analysis and strategy components,</li>
 * <li>initializes a {@link MoveWriter} and writes the generated move in the
 * {@code move.txt} file.</li>
 * </ul>
 * The agent controller remains a strict orchestration layer, all analyitcal and
 * strategic responsibilities are delegated to other components.
 * 
 * @author Shogo Seifert, 7172397
 */

public class RINGwars_7172397_Shogo_Seifert {

	/**
	 * Executes and orchestrates one step of the agent. Initializes and composes
	 * instances of readers, writers, analyzers and strategy classes.
	 * 
	 * @param args the command line arguments {@code args[0]} = step number,
	 *             {@code args[1]} = agent name
	 * 
	 *             If the number of arguments if not two, the method terminates
	 *             immediately.
	 * 
	 */
	public static void main(String[] args) {

		if (args.length != 2) {
			return;
		}

		String name = args[1];
		int step = Integer.parseInt(args[0]);

		// Optional console output to indicate the start of a new game
		if (step == 1)
			System.out.println(
					"---------------------------------" +
			"NEW GAME STARTED" + "-------------------------------");

		IReader sr = new StepReader(name, step);

		GameModel gmPrev = null;

		// Case step > 1 : no previous game State available
		if (step > 1)
			gmPrev = sr.readPreviousGameStatus();

		GameModel gmCurr = sr.readGameStatus();

		StepAnalyzer sa = new StepAnalyzer(gmPrev, gmCurr);

		AbstractStrategyDecider sd = new StrategyDeciderFINALSUBMISSION(gmCurr, sa);

		IMoveWriter mw = new MoveWriter();

		// Write the move to the move.txt file
		mw.writeMove(sd.decide(), gmCurr);

	}

}
