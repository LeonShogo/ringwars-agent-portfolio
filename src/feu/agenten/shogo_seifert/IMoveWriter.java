package feu.agenten.shogo_seifert;

/**
 * Defines the output interface for the agents move in one step.
 * 
 * Implementations are responsible for writing the final move of the agent to
 * the write move.txt file with the correct name, required by the game
 * environment.
 * 
 */

public interface IMoveWriter {
	/**
	 * Writes the move from the current step.
	 * 
	 * @param move the move decided by the strategy layer
	 * @param gm   the current the {@link GameModel}, providing the agent's name
	 */
	public void writeMove(String move, GameModel gm);

}
