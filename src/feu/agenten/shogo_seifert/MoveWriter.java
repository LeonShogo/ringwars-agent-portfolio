package feu.agenten.shogo_seifert;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Concrete implementation of {@link IMoveWriter}.
 * <p>
 * Writes the decided move to the {@code move.txt} file in the directory of the
 * current agent.
 * Instances are created with the default constructor.
 */
public class MoveWriter implements IMoveWriter {

	/**
	 * Writes the given move to the current {@code move.txt} file.
	 * 
	 * @param move      the given move that has to be written
	 * @param gm        the current {@code GameModel} providing the agent's game
	 *                  context
	 * 
	 * @throws IllegalStateException if the move could not be written
	 */
	@Override
	public void writeMove(String move, GameModel gm) {

		if (gm == null || move == null) {
			return;
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(gm.getName() + "/move.txt"))) {

			writer.write(move);

		} catch (IOException e) {

			throw new IllegalStateException("Failed to write move", e);

		}
	}
}
