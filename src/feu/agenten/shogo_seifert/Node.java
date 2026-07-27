package feu.agenten.shogo_seifert;

import java.util.List;

/**
 * Represents a single node on the RINGWars game board. A {@code Node} acts as a
 * lightweight container for one ring position.
 * <p>
 * A {@code Node} instance stores the index on the ring, the number of Fernie
 * placed on it, and the owner.
 * <p>
 * It also provides lightweight helper methods.
 */

public class Node {

	/** The index of the node on the ring. */
	private final int index;

	/** Number of Fernies currently on the node. */
	private final int fernieCount;

	/** Owner identifier for the node. */
	private final String owner;

	/**
	 * Creates a new {@code Node} instance with the specified index, number of
	 * placed Fernies and owner.
	 * 
	 * @param index         the index of the node on the ring
	 * @param countsFernies the number of Fernies on the node
	 * @param owner         the owner of the node (also invisible and neutral)
	 */
	public Node(int index, int countsFernies, String owner) {
		this.index = index;
		this.fernieCount = countsFernies;
		this.owner = owner;
	}

	/**
	 * Checks whether this node is owned by the enemy.
	 * 
	 * @return {@code true} if the owner is the enemy, otherwise {@code false}
	 */
	public boolean isEnemy() {
		if (owner == null)
			return false;
		return !(owner.equals("Y") || owner.equals("U") || owner.equals("N"));
	}

	/**
	 * Checks whether this node is invisible.
	 * 
	 * @return {@code true} if the {@link Node} is invisible, otherwise {@code false}
	 */
	public boolean isInvisible() {
		if (owner == null)
			return false;
		return (owner.equals("U"));
	}

	/**
	 * Checks whether this node is visible.
	 * 
	 * @return {@code true} if the {@link Node} is visible, otherwise {@code false}
	 */
	public boolean isVisible() {
		if (owner == null)
			return false;
		return (!owner.equals("U"));
	}

	/**
	 * Checks whether this node neutral and visible.
	 * 
	 * @return {@code true} if the {@link Node} is neutral; otherwise {@code false}
	 */
	public boolean isNeutral() {
		if (owner == null)
			return false;
		return owner.equals("N") && isVisible();
	}

	/**
	 * Checks whether at least one direct neighbor of this node is an enemy.
	 * 
	 * @param ring a list of {@link Node}s forming the ring
	 * @return {@code true} if one direct neighbor is an enemy, {@code false}
	 *         otherwise
	 */
	public boolean isNeighborEnemy(List<Node> ring) {
		int size = ring.size();
		Node right = ring.get(GameUtils.Ring.correctIndex(index + 1, size));
		Node left = ring.get(GameUtils.Ring.correctIndex(index - 1, size));
		return left.isEnemy() || right.isEnemy();
	}

	/**
	 * Checks whether at least one direct neighbor of this node is neutral.
	 * 
	 * @param ring a list of {@link Node}s forming the ring
	 * @return {@code true} if one direct neighbor is neutral, {@code false}
	 *         otherwise
	 */
	public boolean isNeighborNeutral(List<Node> ring) {
		int size = ring.size();
		Node right = ring.get(GameUtils.Ring.correctIndex(index + 1, size));
		Node left = ring.get(GameUtils.Ring.correctIndex(index - 1, size));
		return left.isNeutral() || right.isNeutral();

	}

	/**
	 * Checks whether both direct neighbors of this node are enemies.
	 * 
	 * @param ring a list of {@link Node}s forming the ring
	 * @return {@code true} if both of the direct neighbors are enemies,
	 *         {@code false} otherwise
	 */
	public boolean areBothNeighborsEnemies(List<Node> ring) {
		int size = ring.size();
		Node right = ring.get(GameUtils.Ring.correctIndex(index + 1, size));
		Node left = ring.get(GameUtils.Ring.correctIndex(index - 1, size));
		return left.isEnemy() && right.isEnemy();
	}

	/**
	 * Checks whether at least one direct neighbor of this node is invisible.
	 * 
	 * @param ring a list of {@link Node}s forming the ring
	 * @return {@code true} if one of the directed neighbor is invisible,
	 *         {@code false} otherwise
	 */
	public boolean isANeighborInvisible(List<Node> ring) {
		int size = ring.size();
		Node right = ring.get(GameUtils.Ring.correctIndex(index + 1, size));
		Node left = ring.get(GameUtils.Ring.correctIndex(index - 1, size));
		return left.isInvisible() || right.isInvisible();
	}

	/**
	 * Gets the index of this node.
	 * 
	 * @return the index of the {@link Node}.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Gets the number of Fernies on this node.
	 * 
	 * @return the Fernie count which is the number of Fernies on this {@link Node}.
	 */
	public int getFernieCounts() {
		return fernieCount;
	}

	/**
	 * Gets the owner of this node.
	 * 
	 * @return the owner of this {@link Node}.
	 */
	public String getOwner() {
		return owner;
	}

}
