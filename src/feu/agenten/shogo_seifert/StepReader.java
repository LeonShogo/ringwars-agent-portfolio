package feu.agenten.shogo_seifert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of {@link IReader}.
 * <p>
 * Reads {@code step.txt} files provided by the RINGWars game environment.
 * Constructs {@link GameModel} instances for the current and previous step.
 */

public class StepReader implements IReader {

	/** GameModel that provides the agent's name and step number. */
	private final GameModel gm;

	/**
	 * Constructs a new {@code StepReader} with the specified {@link GameModel},
	 * providing the specified agent name and step number.
	 *
	 * @param name the specific name of the agent
	 * @param step the step of the game
	 */
	public StepReader(String name, int step) {
		this.gm = new GameModel(name, step);
	}

	/**
	 * Reads and constructs the {@link GameModel} of the current step.
	 * 
	 * @return {@link GameModel} that contains the game state of current step
	 */
	@Override
	public GameModel readGameStatus() {

		try (BufferedReader bf = new BufferedReader(new FileReader(gm.getName() + "/" + gm.getStep() + ".txt"))) {

			// Read node Fernie counts and ownership information

			// Split the first line into an array of the Fernies per node
			String[] counts = bf.readLine().split(",");

			// Split the first line into an array of the owner per node
			String[] owners = bf.readLine().split(",");

			// Iterate over the arrays of Fernie counts and owner
			// to initialize the nodes of the current ring
			for (int i = 0; i < counts.length; i++) {
				int fernies = Integer.parseInt(counts[i]); // Get the number of Fernies of the current node
				String owner = owners[i]; // Get the owner of the current node
				gm.getRing().add(new Node(i, fernies, owner)); // Add the node to the ring
			}

			// Set the newFernies variable with the third line
			gm.setNewFernies(Integer.parseInt(bf.readLine()));

			// Set the maxFernies variable with the fourth line
			gm.setMaxFernies(Integer.parseInt(bf.readLine()));

			// Get the absolute visibility of the current game
			//
			if (gm.getStep() == 0) {
				gm.setAbsVis((int) GameUtils.Ring.getVisibility(gm.getRing()));
			} else {

				// Get absolute visibility based on the first steps and the number of neutral
				// nodes
				try (BufferedReader bf2 = new BufferedReader(new FileReader(gm.getName() + "/1.txt"))) {

					// Rebuild the ring of the first step out of the text file of the first step
					String[] counts2 = bf2.readLine().split(",");
					String[] owners2 = bf2.readLine().split(",");

					List<Node> ringStep1 = new ArrayList<>();
					for (int i = 0; i < counts2.length; i++) {
						int fernies = Integer.parseInt(counts2[i]);
						String owner = owners2[i];
						ringStep1.add(new Node(i, fernies, owner));
					}
					// Set the absolute visibility of the current game
					gm.setAbsVis((int) GameUtils.Ring.getVisibility(ringStep1));

				} catch (IOException e) {

					// Fallback if step one file is not available
					gm.setAbsVis((int) GameUtils.Ring.getVisibility(gm.getRing()));
					System.out.println("Fallback visibility: " + gm.getAbsVis());
				}
			}

		} catch (IOException e) {
			// Throw an exception if the file cannot be read
			throw new IllegalStateException("Failed to read current game step" + gm.getStep(), e);
		}

		// Return the game model object representing the current game state
		return gm;

	}

	/**
	 * Reads and constructs the {@link GameModel} of the previous step.
	 * 
	 * @return {@link GameModel} that represents the game state of previous step
	 */
	public GameModel readPreviousGameStatus() {

		// The GameModel object representing the previous step
		GameModel prevGM = new GameModel(gm.getName(), gm.getStep() - 1);

		try (BufferedReader bf = new BufferedReader(
				new FileReader(prevGM.getName() + "/" + (gm.getStep() - 1) + ".txt"))) {

			// Reading node Fernie counts and ownership information
			String[] counts = bf.readLine().split(",");
			String[] owners = bf.readLine().split(",");

			// Iterate over the arrays of Fernie counts and owner
			// to initialize the nodes of the current ring
			for (int i = 0; i < counts.length; i++) {
				int fernies = Integer.parseInt(counts[i]); // Get the number of Fernies of the node
				String owner = owners[i]; // Get the owner of the node
				prevGM.getRing().add(new Node(i, fernies, owner)); // Add the node to the ring
			}

			// Setting the newFernies variable with the third line
			prevGM.setNewFernies(Integer.parseInt(bf.readLine()));

			// Setting the maxFernies variable with the fourth line
			prevGM.setMaxFernies(Integer.parseInt(bf.readLine()));

		} catch (IOException e) {
			// Previous step not available - continue without full information
			return prevGM;
		}
		// Return the game model object representing the previous game state
		return prevGM;

	}

}
