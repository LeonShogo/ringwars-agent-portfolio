package feu.agenten.shogo_seifert;

/**
 * Defines the input interface for reading the {@code step.txt} files provided
 * by the game environment and builind a game state of the current step.
 * <p>
 * Implementations are responsible for parsing the input files provided by the
 * game environment and initializing a {@link GameModel} by parsing the
 * information.
 */
public interface IReader {

	/**
	 * Reads the game state of the current step.
	 * 
	 * @return a {@link GameModel}, representing the current game state
	 */
	public GameModel readGameStatus();

	/**
	 * Reads the game state of the previous step.
	 * 
	 * @return {@link GameModel}, representing the previous game state
	 */
	public GameModel readPreviousGameStatus();

}
