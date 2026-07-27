package feu.agenten.shogo_seifert;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of a {@link GameModel} represents the game state of a single
 * step.
 * <p>
 * A {@code GameModel} is a passive data container which stores the name of the
 * agent, the step, the ring of nodes as a list, the maximum number of Fernies
 * per node and the new Fernies of the current step.
 * <p>
 * It contains no logic for strategic decisions.
 */

public class GameModel {

	/** The agents name. */
	private final String name;

	/** The current step number. */
	private final int step;

	/** List of nodes forming the ring. */
	private List<Node> ring = new ArrayList<Node>();

	/** New available Fernies of the current step. */
	private int newFernies;

	/** Max number of Fernies per node. */
	private int maxFernies;

	/** Absolute visibility of the current game. */
	private int absVis;

	/**
	 * Constructs a new instance of a {@code GameModel} for one step with the
	 * specified agent name, step number, ring, new Fernies, maximum number of
	 * Fernies per node, and absolute visibility of the current step.
	 * 
	 * @param name       the agent name
	 * @param step       the step number
	 * @param ring       a list of nodes forming the ring
	 * @param newFernies the new Fernies of the current step
	 * @param maxFernies the maximum number of Fernies per node
	 * @param absVis     the absolute visibility of the game
	 */
	public GameModel(String name, int step, List<Node> ring, int newFernies, int maxFernies, int absVis) {
		super();
		this.name = name;
		this.step = step;
		this.ring = ring;
		this.newFernies = newFernies;
		this.maxFernies = maxFernies;
		this.absVis = absVis;

	}

	/**
	 * Constructs a new instance of a {@code GameModel} with the specified agent
	 * name, step and default values.
	 *
	 * @param name agent name
	 * @param step step number
	 */
	public GameModel(String name, int step) {
		this(name, step, new ArrayList<Node>(), 0, 0, 0);
	}

	/**
	 * Gets the name of the agent.
	 * 
	 * @return the name of the agent
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the ring size of the current game.
	 * 
	 * @return the ring size of the current game
	 */
	public int getRingSize() {
		return this.ring.size();
	}

	/**
	 * Gets the ring representing the current game state.
	 * 
	 * @return the ring as a list of nodes
	 */
	public List<Node> getRing() {
		return ring;
	}

	/**
	 * Gets the new available Fernies of the current step.
	 * 
	 * @return the new available new Fernies
	 */
	public int getNewFernies() {
		return newFernies;
	}

	/**
	 * Sets the new Fernies of the current step.
	 * 
	 * @param newFernies the number of the new Fernies of the current step
	 */
	public void setNewFernies(int newFernies) {
		this.newFernies = newFernies;
	}

	/**
	 * Sets the maximum number of Fernies per node.
	 * 
	 * @param maxFernies the number of the maximum capacity per node of the current game
	 */
	public void setMaxFernies(int maxFernies) {
		this.maxFernies = maxFernies;
	}

	/**
	 * Uses a given number of the new Fernies and updates it.
	 * 
	 * @param used the number of Fernies taken
	 * @throws IllegalArgumentException if {@code used < 0}
	 */

	public void useNewFernies(int used) {
		if (used < 0) {
			throw new IllegalArgumentException("used must be >= 0");
		}
		this.newFernies = Math.max(0, this.newFernies - used);
	}

	/**
	 * Gets the maximum number of Fernies per node.
	 * 
	 * @return the maximum Fernies per node
	 */
	public int getMaxFernies() {
		return maxFernies;
	}

	/**
	 * Gets the absolute visibility of the game.
	 * 
	 * @return the absolute visibility of the game
	 */
	public int getAbsVis() {
		return absVis;
	}

	/**
	 * Gets the current step number.
	 * 
	 * @return the step number
	 */
	public int getStep() {
		return step;
	}

	/**
	 * Sets the absolute visibility of the current game.
	 * 
	 * @param absVis the absolute visibility of the current game
	 */
	public void setAbsVis(int absVis) {
		this.absVis = absVis;
	}

}
