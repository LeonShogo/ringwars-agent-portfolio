package feu.agenten.shogo_seifert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The {@code GameUtils} is a utility class that provides independent and mostly
 * context free helper methods for information gathering and game logic.
 * <p>
 * They are implemented as {@code static} methods so they can be accessed
 * without initializing an instance of the class.
 * <p>
 * {@code GameUtils} is divided in different nested classes that group the
 * related functionality to improve the overview and readability.
 * <p>
 * The inner classes are:
 * <ul>
 * <li>{@link Ring}, collects methods related to the ring,</li>
 * <li>{@link Analysis}, collects analytical helper methods,</li>
 * <li>{@link Combat}, collects methods related to combat,</li>
 * <li>{@link MathAndRandom}, provides mathematical and random methods.</li>
 * </ul>
 * 
 */
public class GameUtils {

	/**
	 * This class should not be instantiated.
	 */
	private GameUtils() {
	};

	/**
	 * Simple data container representing the result move composition of moves that
	 * take Fernies.
	 * <p>
	 * Stores the move {@link String} and the total number of Fernies taken.
	 */
	public static final class MoveResult {

		/**
		 * The move String representing the move.
		 */
		public final String move;

		/**
		 * The number of Fernies taken.
		 */
		public final int take;

		/**
		 * Constructs a new instance of {@code MoveResult} with the specified move
		 * String and the number of Fernies taken.
		 * 
		 * @param move the move {@link String} representing the action of taking the
		 *             Fernies
		 * @param take the total sum of Fernies taken
		 */
		public MoveResult(String move, int take) {
			this.move = move;
			this.take = take;
		}
	}

	/**
	 * Collection of helper methods related to the ring.
	 * <p>
	 * The collection includes index correction, distance calculation, neighbor
	 * checks and visibility related operations.
	 *
	 */
	public static class Ring {

		/**
		 * This class should not be instantiated.
		 */
		private Ring() {
		};

		/**
		 * Returns the correct index on the ring.
		 * 
		 * @param index    the given index
		 * @param ringSize the size of the ring
		 * @return an index within the ring bounds
		 */
		public static int correctIndex(int index, int ringSize) {
			return (index % ringSize + ringSize) % ringSize;
		}

		/**
		 * Returns the absolute visibility by calculating the amount of neutral nodes
		 * used in the very beginning of the game.
		 * 
		 * @param ring list of {@link Node}s forming the ring
		 * @return value of the absolute visibility
		 */
		public static int getVisibility(List<Node> ring) {
			int count = 0;
			for (Node n : ring) {
				if (n.getOwner().equals("N")) {
					count++;
				}
			}
			// Absolute visibility is only one direction
			return count / 2;
		}

		/**
		 * Returns the right neighbor on the ring of the given node.
		 * 
		 * @param n    the reference {@link Node}
		 * @param ring list of {@link Node}s forming the ring
		 * @return the right neighbor of the reference {@link Node}
		 */
		public static Node neighborR(Node n, List<Node> ring) {
			return ring.get(correctIndex(n.getIndex() + 1, ring.size()));
		}

		/**
		 * Returns the left neighbor on the ring of the given node.
		 * 
		 * @param n    the reference {@link Node}
		 * @param ring list of {@link Node}s forming the ring
		 * @return the left neighbor of the reference {@link Node}
		 */
		public static Node neighborL(Node n, List<Node> ring) {
			return ring.get(correctIndex(n.getIndex() - 1, ring.size()));
		}

		/**
		 * Returns the distance of the nearest enemy of a given node.
		 * 
		 * @param n    the given reference {@link Node}
		 * @param ring list of {@link Node}s forming the ring
		 * @return the distance to the reference {@link Node} to its closest enemy
		 * 
		 */
		public static int distanceNearestEnemy(Node n, List<Node> ring) {

			int nextR = nextEnemyRight(n.getIndex(), ring);
			int nextL = nextEnemyLeft(n.getIndex(), ring);

			if (nextR == -1 && nextL == -1)
				return ring.size();

			return Math.min(distanceOnRing(n.getIndex(), nextR, ring.size()),
					distanceOnRing(n.getIndex(), nextL, ring.size()));
		}

		/**
		 * Returns the shortest distance of to given indices of nodes on the ring.
		 * 
		 * @param a        the first index
		 * @param b        the second index
		 * @param ringSize the size of the ring
		 * @return the shorter distance between the two indices on the ring as an int
		 */
		public static int distanceOnRing(int a, int b, int ringSize) {
			int diff = Math.abs(a - b);
			// Shorter path right or left way
			return Math.min(diff, ringSize - diff);
		}

		/**
		 * Returns the index of the next enemy in the right direction starting from a
		 * given index.
		 * 
		 * @param startIndex the start index
		 * @param ring       a list of {@link Node}s that is forming the ring
		 * @return the index of the next enemy to the right of the given start index or
		 *         {@code -1} if no enemy was found
		 */
		public static int nextEnemyRight(int startIndex, List<Node> ring) {
			int size = ring.size();
			for (int i = 1; i < size; i++) {
				int index = correctIndex(startIndex + i, size);
				Node n = ring.get(correctIndex(index, size));
				if (n.isEnemy()) {
					return index;
				}
			}
			return -1;
		}

		/**
		 * Returns the index of the next enemy of the left direction starting from a
		 * given index.
		 * 
		 * @param startIndex the start index
		 * @param ring       a list of {@link Node}s that is forming the ring
		 * @return the index of the next enemy to the left of the given start index or
		 *         {@code -1} if no enemy was found
		 */
		public static int nextEnemyLeft(int startIndex, List<Node> ring) {
			int size = ring.size();
			for (int i = 1; i < size; i++) {
				int index = correctIndex(startIndex - i, size);
				Node n = ring.get(correctIndex(index, size));
				if (n.isEnemy()) {
					return index;
				}
			}
			return -1; // No enemy was found
		}

		/**
		 * Returns the index of the next neutral node of the left direction starting
		 * from a given index.
		 * 
		 * @param startIndex the start index
		 * @param ring       a list of {@link Node}s that is forming the ring
		 * @return the index of the next neutral {@link Node} to the left of the given
		 *         start index or {@code -1} if no neutral {@link Node} exists
		 */
		public static int neutralNodeLeft(int startIndex, List<Node> ring) {

			int size = ring.size();
			for (int i = 0; i < size; i++) {
				int Nindex = correctIndex(startIndex - i, size);
				if ("N".equals(ring.get(Nindex).getOwner())) {
					return Nindex;
				}
			}
			return -1;
		}

		/**
		 * Returns the index of the next neutral node of the right direction starting
		 * from a given index.
		 * 
		 * @param startIndex the start index
		 * @param ring       a list of {@link Node}s that is forming the ring
		 * @return the index of the next neutral {@link Node} to the right of the given
		 *         start index or {@code -1} if no neutral {@link Node} exists
		 */
		public static int neutralNodeRight(int startIndex, List<Node> ring) {
			int size = ring.size();
			for (int i = 0; i < size; i++) {
				int Nindex = correctIndex(startIndex + i, size);
				if ("N".equals(ring.get(Nindex).getOwner())) {
					return Nindex;
				}
			}
			return -1;
		}

	}

	/**
	 * Collection of helper methods related to analytical operations on the ring.
	 * <p>
	 * The collection includes getting a list of a specific type of nodes, colony
	 * related helper methods, or getting the strongest node out of a list of nodes.
	 * 
	 */
	public static class Analysis {

		/**
		 * This class should not be instantiated.
		 */
		private Analysis() {
		};

		/**
		 * Returns the strongest colony in terms of the number of Fernies of a given
		 * list of colonies.
		 * 
		 * @param colonies a list of colonies
		 * @return the strongest colony out of the given list in terms of the number of
		 *         Fernies
		 */
		public static List<Node> getStrongestColony(List<List<Node>> colonies) {
			// sorts the given list in ascending order in terms of the amount of Fernies in
			// the colony
			sortedColoniesFernies(colonies);
			return colonies.getLast();
		}

		/**
		 * Sorts a given list of colonies in ascending order in terms of the number of
		 * Fernies it contains.
		 * 
		 * @param colonies a list of colonies
		 * @return the same given list, sorted in ascending order by number of Fernies
		 */
		public static List<List<Node>> sortedColoniesFernies(List<List<Node>> colonies) {
			Collections.sort(colonies, (list1, list2) -> Analysis.sumFernies(list1) - Analysis.sumFernies(list2));
			return colonies;
		}

		/**
		 * Returns a list of "colonies". <em>Colonies</em> are connected nodes owned by
		 * the given owner.
		 * <p>
		 * If the first and last colonies belong to the same owner, they are merged into
		 * a single colony.
		 * <p>
		 * The list of colonies is sorted in ascending order base on the colony size.
		 * 
		 * @param ring  a list of {@link Node}s that is forming the ring
		 * @param owner the owner of the colonies
		 * @return a list of colonies, represented as a list of {@link Node}s, sorted by
		 *         ascending colony size
		 */
		public static List<List<Node>> getColonies(List<Node> ring, String owner) {

			List<List<Node>> listOfColonies = new ArrayList<List<Node>>();
			List<Node> colony = new ArrayList<Node>();

			// Build colonies of the same given owner
			for (int i = 0; i < ring.size(); i++) {
				if (ring.get(i).getOwner().equals(owner)) {
					colony.add(ring.get(i));

				} else if (!colony.isEmpty()) {
					listOfColonies.add(new ArrayList<Node>(colony));
					colony.clear();
				}
			}

			// Add the last colony
			if (!colony.isEmpty()) {
				listOfColonies.add(new ArrayList<Node>(colony));
			}
			// Merge first and last colony if they are connected
			if (listOfColonies.size() > 1) {
				List<Node> first = listOfColonies.get(0);
				List<Node> last = listOfColonies.get(listOfColonies.size() - 1);

				if (!first.isEmpty() && !last.isEmpty() && first.get(0).getOwner().equals(owner)
						&& last.get(0).getOwner().equals(owner)) {
					last.addAll(first);
					listOfColonies.remove(0);
				}
			}
			// Sort in ascending order based on the size
			Collections.sort(listOfColonies, (list1, list2) -> list1.size() - list2.size());
			return listOfColonies;
		}

		/**
		 * Returns a list of all currently visible nodes Merges three given lists that
		 * contain neutral nodes, the enemy nodes and the owned nodes.
		 * 
		 * @param myNodes      a list of owned {@link Node}s
		 * @param neutralNodes a list of neutral {@link Node}s
		 * @param enemyNodes   a list of enemy {@link Node}s
		 * @return a list, merged out of the three given lists (result is a list
		 *         containing all visible {@link Node}s)
		 */
		public static List<Node> allVisibleNodes(List<Node> myNodes, List<Node> neutralNodes, List<Node> enemyNodes) {

			List<Node> allNodes = new ArrayList<Node>();

			if (myNodes != null)
				allNodes.addAll(myNodes);

			if (neutralNodes != null)
				allNodes.addAll(neutralNodes);

			if (enemyNodes != null)
				allNodes.addAll(enemyNodes);

			sortListIndizes(allNodes);

			return allNodes;
		}

		/**
		 * Returns whether at least one enemy node is visibly on the current ring.
		 * 
		 * @param ring list of {@link Node}s forming the ring
		 * @return {@code true} when an enemy is visible on the ring, otherwise
		 *         {@code false}
		 */
		public static boolean enemyVisible(List<Node> ring) {
			for (Node n : ring) {
				if (n.isEnemy()) {
					return true;
				}
			}
			return false;
		}

		/**
		 * Returns a list of nodes that contains all current nodes owned by the agent.
		 * 
		 * @param gm {@link GameModel} that provides the current representation of the
		 *           ring
		 * @return a new list of {@link Node}s of all nodes owned by the agent
		 */
		public static List<Node> getMyNodes(GameModel gm) {

			List<Node> myNodes = new ArrayList<Node>();
			
			for (Node n : gm.getRing()) {
				
				if (n.getOwner().equals("Y") && n.getFernieCounts() >= 0) {
					myNodes.add(n);
				}
			}
			
			return myNodes;
		}

		/**
		 * Returns a list of nodes that contains all current nodes owned by the enemy.
		 * 
		 * @param gm {@link GameModel} that provides the current representation of the
		 *           ring
		 * @return a new list of {@link Node}s of all nodes owned by the enemy
		 */
		public static List<Node> getEnemyNodes(GameModel gm) {
			
			List<Node> enemyNodes = new ArrayList<Node>();
			
			for (Node n : gm.getRing()) {
				
				if (n.isEnemy() && n.getFernieCounts() >= 0) {
					enemyNodes.add(n);
				}
			}
			
			return enemyNodes;
		}

		/**
		 * Returns a list of nodes that contains all current visible neutral nodes.
		 * 
		 * @param gm {@link GameModel} that provides the current representation of the
		 *           ring
		 * @return a new list of {@link Node}s of all currently neutral nodes
		 */
		public static List<Node> getNeutralNodes(GameModel gm) {

			List<Node> neutralNodes = new ArrayList<Node>();
			
			for (Node n : gm.getRing()) {
				
				if (n.getOwner().equals("N") && n.getFernieCounts() >= 0) {
					neutralNodes.add(n);
				}
			}
			return neutralNodes;
		}

		/**
		 * Returns the distance of the closest enemy next to a given node.
		 * 
		 * @param n    {@link Node} for that the distance of the closest enemy should be
		 *             calculated
		 * @param ring list with nodes representing the current ring
		 * @return the distance of the closest enemy {@link Node} or {@code ring.size()}
		 *         if no enemy was found
		 */
		public static int distanceNearestEnemy(Node n, List<Node> ring) {

			int nextR = Ring.nextEnemyRight(n.getIndex(), ring);
			int nextL = Ring.nextEnemyLeft(n.getIndex(), ring);

			if (nextR == -1 && nextL == -1)
				return ring.size();

			if (nextR == -1 && nextL != -1)
				return Ring.distanceOnRing(n.getIndex(), nextL, ring.size());

			if (nextL == -1 && nextR != -1)
				return Ring.distanceOnRing(n.getIndex(), nextR, ring.size());

			return Math.min(Ring.distanceOnRing(n.getIndex(), nextR, ring.size()),
					Ring.distanceOnRing(n.getIndex(), nextL, ring.size()));
		}

		/**
		 * Returns the average number of Fernies in the given list.
		 * 
		 * @param nodes a list of {@link Node}s
		 * @return the average number of Fernies of the {@link Node}s of {@code 0} if
		 *         the list list is empty or {@code null}
		 */
		public static double averageFernies(List<Node> nodes) {
			int sum = 0;
			int counts = 0;

			if (nodes == null || nodes.isEmpty())
				return 0;

			for (Node n : nodes) {
				sum = sum + n.getFernieCounts();
				counts++;
			}
			return (double) sum / (double) counts;
		}

		/**
		 * Returns the total number of the Fernies contained by the nodes of the given
		 * list.
		 * 
		 * @param nodes a list of {@link Node}s
		 * @return the sum of the Fernies contained by the {@link Node}s in the given
		 *         list
		 */
		public static int sumFernies(List<Node> nodes) {
			int sum = 0;
			for (Node n : nodes) {
				sum = sum + n.getFernieCounts();
			}
			return sum;
		}

		/**
		 * Returns the node with the highest number of Fernies out of a given list.
		 * 
		 * @param nodes a list of {@link Node}s
		 * @return the {@link Node} with the highest number of Fernies or {@code null}
		 *         if the given list is empty or {@code null}
		 */
		public static Node nodeWithMaxFernies(List<Node> nodes) {

			if (nodes == null || nodes.isEmpty())
				return null;

			Node maxNode = nodes.get(0);
			for (Node n : nodes) {
				if (n.getFernieCounts() > maxNode.getFernieCounts()) {
					maxNode = n;
				}
			}
			return maxNode;
		}

		/**
		 * Returns the node with the lowest number of Fernies out of a given list.
		 * 
		 * @param nodes a list of {@link Node}s
		 * @return the {@link Node} with the lowest number of Fernies or {@code null} if
		 *         the given list is empty or {@code null}
		 */
		public static Node nodeWithMinFernies(List<Node> nodes) {

			if (nodes == null || nodes.isEmpty())
				return null;

			Node minNode = nodes.get(0);
			for (Node n : nodes) {
				if (n.getFernieCounts() < minNode.getFernieCounts()) {
					minNode = n;
				}
			}
			return minNode;
		}

		/**
		 * Returns the ratio of the nodes contained in the given list relative to the
		 * ring size.
		 * 
		 * @param nodes    list of {@link Node}s
		 * @param ringSize size of the ring
		 * @return the ratio between the list of {@link Node} and the ring size as a
		 *         {@code double} value
		 */
		public static double ownedRatio(List<Node> nodes, int ringSize) {
			return (double) nodes.size() / ringSize;
		}

		/**
		 * Sorts a given list based on their number of Fernies.
		 * 
		 * @param nodes a list of {@link Node}s that gets sorted
		 */
		public static void sortListFernies(List<Node> nodes) {
			Collections.sort(nodes, (n1, n2) -> n1.getFernieCounts() - n2.getFernieCounts());
		}

		/**
		 * Sorts a given list based on their indices.
		 * 
		 * @param nodes a list of {@link Node}s that gets sorted
		 */
		public static void sortListIndizes(List<Node> nodes) {
			Collections.sort(nodes, (n1, n2) -> n1.getIndex() - n2.getIndex());
		}

		/**
		 * Returns a new list containing the nodes of the given list sorted in ascending
		 * order based on their number of Fernie.
		 * 
		 * @param nodes the list of {@link Node}s that gets sorted
		 * @return a new list of {@link Node}s sorted based on their number of Fernies
		 */
		public static List<Node> getSortedListFernies(List<Node> nodes) {
			List<Node> sortedList = new ArrayList<Node>(nodes);

			Collections.sort(sortedList, (n1, n2) -> n1.getFernieCounts() - n2.getFernieCounts());
			return sortedList;
		}

		/**
		 * Returns a new list containing the nodes of the given list sorted in ascending
		 * order based on their indices.
		 * 
		 * @param nodes the list of {@link Node}s that gets sorted
		 * @return a new list of {@link Node}s sorted based on their indices
		 */
		public static List<Node> getSortedListbyIndizes(List<Node> nodes) {
			List<Node> sortedList = new ArrayList<Node>(nodes);

			Collections.sort(sortedList, (n1, n2) -> n1.getIndex() - n2.getIndex());
			return sortedList;
		}

	}

	/**
	 * Collection of helper methods related to combats.
	 * <p>
	 * This collections includes to get the best placement for an attack, to
	 * calculate the strength of an enemy {@link Node}s or to get the best
	 * {@link Node}s to retreat from the enemy.
	 */
	public static class Combat {

		/**
		 * This class should not be instantiated.
		 */
		private Combat() {
		};

		/**
		 * Returns the best {@link Node} to run away with.
		 * <p>
		 * The method prefers neutral nodes not visible to the enemy with the greatest
		 * distance to an enemy node.
		 * <p>
		 * If no neutral invisible node exists, it falls back to visible neutral nodes
		 * with the greatest distance to an enemy.
		 * 
		 * @param ring a list of {@link Node}s forming the ring
		 * @param gm   the current {@link GameModel}
		 * @return a {@link Node} that is considered the best {@link Node}s to run away
		 *         or {@code null} if no {@link Node}s was found
		 */
		public static Node getBestNodeToRunAway(List<Node> ring, GameModel gm) {

			List<Node> invisNodes = new ArrayList<Node>();
			List<Node> neutralNodes = new ArrayList<Node>();

			Node bestOption = null;
			int dist = 0;

			// Neutral or invisible nodes without an enemy neighbor
			for (Node n : ring) {
				if (n.isNeutral() && !enemySeesNode(n, ring, gm.getAbsVis()) && !n.isNeighborEnemy(ring)) {
					invisNodes.add(n);
				}

				if (n.isNeutral() && !n.isNeighborEnemy(ring)) {
					neutralNodes.add(n);
				}
			}

			// Invisible neutral nodes with greatest distance to an enemy
			for (Node inv : invisNodes) {
				if (Ring.distanceNearestEnemy(inv, ring) > dist) {
					bestOption = inv;
					dist = Ring.distanceNearestEnemy(inv, ring);
				}
			}

			// Fallback: visible neutral nodes
			if (bestOption == null) {
				for (Node neutr : neutralNodes) {
					if (Ring.distanceNearestEnemy(neutr, ring) > dist) {
						bestOption = neutr;
						dist = Ring.distanceNearestEnemy(neutr, ring);
					}
				}
			}
			return bestOption;
		}

		/**
		 * Calculates the best index to place Fernies to attack a given enemy
		 * {@link Node}.
		 * <p>
		 * The method considers the neighborhood of the given enemy node. It applies a
		 * heuristic decision process based on:
		 * <ul>
		 * <li>whether neighboring nodes are neutral or owned by the enemy,</li>
		 * <li>the combined strength of the enemy and its neighbors,</li>
		 * <li>the visibility,</li>
		 * <li>the distance to other enemy nodes.</li>
		 * </ul>
		 * Border nodes are preferred, since they absorb the enemy's Fernies.
		 * <p>
		 * If no good placement was found, as a fallback action, the given enemy node
		 * gets attacked directly.
		 * 
		 * @param enemyNode the enemy {@link Node} that gets attacked
		 * @param gm        the current {@link GameModel} representing the game state
		 * @return the index of the {@link Node} where the Fernies to attack the enemy
		 *         node should be placed
		 */
		public static int getBestPlacement(Node enemyNode, GameModel gm) {

			// Strength of the given target enemy node itself
			int enemyStrength = enemyNode.getFernieCounts();

			// Fernies of the neighbor enemy nodes
			int sR = 0;
			int sL = 0;
			int sRR = 0;
			int sLL = 0;

			int index = enemyNode.getIndex();

			// Direct neighbors of the given enemy node,
			// getting their strength if they are enemy nodes as well
			Node neighbourR = Ring.neighborR(enemyNode, gm.getRing());
			if (neighbourR.isEnemy())
				sR = neighbourR.getFernieCounts();

			Node neighbourL = Ring.neighborL(enemyNode, gm.getRing());
			if (neighbourL.isEnemy())
				sL = neighbourL.getFernieCounts();

			// Second neighbors of the given enemy node,
			// getting their strength if they are enemy nodes as well
			Node neighbourRR = Ring.neighborR(neighbourR, gm.getRing());
			if (neighbourRR.isEnemy())
				sRR = neighbourRR.getFernieCounts();

			Node neighbourLL = Ring.neighborL(neighbourL, gm.getRing());
			if (neighbourLL.isEnemy())
				sLL = neighbourLL.getFernieCounts();

			// Checks if the neighboring nodes are neutral
			boolean Rfree = neighbourR.isNeutral();
			boolean Lfree = neighbourL.isNeutral();

			// Checks if the second neighbor nodes are neutral
			boolean RRfree = neighbourR.isNeutral() && neighbourRR.isNeutral();
			boolean LLfree = neighbourL.isNeutral() && neighbourLL.isNeutral();

			// 1. Case: RR and LL are both free free
			if (RRfree && LLfree) {

				// Place on the node with the larger distance to an enemy node
				int disR = Analysis.distanceNearestEnemy(neighbourR, gm.getRing());
				int disL = Analysis.distanceNearestEnemy(neighbourL, gm.getRing());

				if (disR > disL) {
					if (neighbourR.isVisible())
						return neighbourR.getIndex();
				}
				if (neighbourL.isVisible())
					return neighbourL.getIndex();

				else
					return enemyNode.getIndex();
			}

			// 2. Case: only RR is free
			if (RRfree && !LLfree) {
				if (neighbourR.isVisible())
					return neighbourR.getIndex();
				else
					return enemyNode.getIndex();
			}
			// 2. Case: only LL is free
			if (LLfree && !RRfree) {
				if (neighbourL.isVisible())
					return neighbourL.getIndex();
				else
					return enemyNode.getIndex();
			}
			// 4. Case: direct neighbors R AND L are free, but second neighbors are blocked
			if (Rfree && Lfree) {
				int strenghtR = enemyStrength + sRR;
				int strengthL = enemyStrength + sLL;
				if (strenghtR < strengthL && neighbourR.isVisible())
					return neighbourR.getIndex();
				if (neighbourL.isVisible())
					return neighbourL.getIndex();
				else
					return enemyNode.getIndex();
			}
			// 5. Case: right is free, but left is blocked
			if (Rfree && !Lfree) {
				int strengthL = enemyStrength + sL + sLL;
				int strengthR = enemyNode.getFernieCounts() + sRR;
				int strengthEnemy = getEnemyStrengthWithNeighbours(enemyNode, gm);
				if (neighbourLL.isNeutral() && strengthL < strengthR) {
					return neighbourL.getIndex();
				}
				int minStrength = MathAndRandom.getMinimum(strengthEnemy, strengthL, strengthR);

				if (minStrength == strengthEnemy)
					return enemyNode.getIndex();
				if (minStrength == strengthR && neighbourR.isVisible())
					return neighbourR.getIndex();
				if (neighbourL.isVisible())
					return neighbourL.getIndex();
				else
					return enemyNode.getIndex();

			}
			// 6. Case: left is free, but right is blocked
			if (Lfree && !Rfree) {
				int strenghtL = enemyStrength + sLL;
				int strenghtR = enemyStrength + sR + sRR;
				int strengthEnemy = getEnemyStrengthWithNeighbours(enemyNode, gm);

				if (neighbourRR.isNeutral() && strenghtR < strenghtL) {
					return neighbourR.getIndex();
				}
				int minStrength = MathAndRandom.getMinimum(strengthEnemy, strenghtL, strenghtR);

				if (minStrength == strengthEnemy)
					return enemyNode.getIndex();
				if (minStrength == strenghtR && neighbourR.isVisible())
					return neighbourR.getIndex();
				if (neighbourL.isVisible())
					return neighbourL.getIndex();
				else
					return enemyNode.getIndex();
			}
			// 7. Case: both sides are enemies
			if (!Lfree && !Rfree) {

				int strenghtEnemy = getEnemyStrengthWithNeighbours(enemyNode, gm);
				int strenghtL = getEnemyStrengthWithNeighbours(neighbourL, gm);
				int strengthR = getEnemyStrengthWithNeighbours(neighbourR, gm);

				int minStrength = MathAndRandom.getMinimum(strenghtEnemy, strenghtL, strengthR);

				if (minStrength == strenghtEnemy)
					return enemyNode.getIndex();
				if (minStrength == strengthR && neighbourR.isVisible())
					return neighbourR.getIndex();
				if (neighbourL.isVisible())
					return neighbourL.getIndex();
				else
					return enemyNode.getIndex();

			}
			// Fallback: the given enemy node's index
			return index;
		}

		/**
		 * Returns the strength of the given node calculated as a sum of its own Fernies
		 * amount plus the number of Fernies of its direct neighbors.
		 * 
		 * @param enemyNode given enemy {@link Node}
		 * @param gm        {@link GameModel} which provides the ring as list of nodes
		 * @return the total amount of the Fernies as the strength including the Fernies
		 *         of both direct neighbors
		 */
		public static int getEnemyStrengthWithNeighbours(Node enemyNode, GameModel gm) {

			int strength = enemyNode.getFernieCounts();

			Node neighourR = Ring.neighborR(enemyNode, gm.getRing());
			Node neighbourL = Ring.neighborL(enemyNode, gm.getRing());

			if (neighourR.isEnemy())
				strength += neighourR.getFernieCounts();
			if (neighbourL.isEnemy())
				strength += neighbourL.getFernieCounts();

			return strength;
		}

		/**
		 * Returns whether a given node is visible for the enemy.
		 * 
		 * @param node   given reference {@link Node} to check if it is visible for the
		 *               enemy
		 * @param ring   list of {@link Node}s forming the ring
		 * @param absVis absolute visibility of the match
		 * @return {@code true} if a given {@link Node} is visible for at least on enemy
		 *         node, otherwise returns {@code false}
		 * 
		 */
		public static boolean enemySeesNode(Node node, List<Node> ring, int absVis) {

			for (Node n : ring) {
				if (n.isEnemy()) {
					int dist = Ring.distanceOnRing(n.getIndex(), node.getIndex(), ring.size());
					if (dist <= absVis) {
						return true;
					}
				}
			}
			return false;

		}

		/**
		 * Returns the nearest enemy in reference to a given node.
		 * 
		 * @param n    reference {@link Node} for which the nearest enemy should be
		 *             found
		 * @param ring list of {@link Node}s thats representing the current ring
		 * @return the enemy node with the closest distance to the given node or
		 *         {@code null} if no enemy is found
		 */
		public static Node getNearestEnemy(Node n, List<Node> ring) {

			// Getting the indices of the closest enemy in both directions
			int right = Ring.nextEnemyRight(n.getIndex(), ring);
			int left = Ring.nextEnemyLeft(n.getIndex(), ring);

			// If no enemy was found in both directions, return null
			if (right == -1 && left == -1)
				return null;

			// Safety: if no enemy was found in the left direction
			if (left == -1 ||

					Ring.distanceOnRing(n.getIndex(), right, ring.size()) < Ring.distanceOnRing(n.getIndex(), left,
							ring.size()))

				return ring.get(right);

			// Safety: if no enemy was found in the right direction
			if (right == -1 ||

					Ring.distanceOnRing(n.getIndex(), right, ring.size()) > Ring.distanceOnRing(n.getIndex(), left,
							ring.size()))

				return ring.get(left);

			else
				return ring.get(right);
		}

		/**
		 * Returns the name of the enemy in the current game state.
		 * 
		 * @param ring a list of {@link Node}s that are forming the ring
		 * @return the name of the enemy as a {@link String} , or an empty
		 *         {@link String} if no enemy is visible
		 */
		public static String getEnemiesName(List<Node> ring) {
			if (!Analysis.enemyVisible(ring))
				return "";
			for (Node n : ring) {
				if (n.isEnemy()) {
					return n.getOwner();
				}
			}
			// Safety: if no enemy is found, return empty String
			return "";
		}

	}

	/**
	 * Collection of helper methods for calculating and getting useful random
	 * values.
	 * <p>
	 * The collection includes calculating a random index on a ring or to get the
	 * minimum of three integer values.
	 */
	public static class MathAndRandom {
		/**
		 * This class should not be instantiated.
		 */
		private MathAndRandom() {
		};

		/**
		 * Returns a random index within the bounds of a ring.
		 * 
		 * @param ringSize the size of the ring
		 * @return a random index in the range of {@code 0} (inclusive) to {@code
		 *         ringSize} (exclusive)
		 * @throws IllegalArgumentException if {@code ringSize <= 0 }
		 */
		public static int randomIndex(int ringSize) {
			if (ringSize <= 0)
				throw new IllegalArgumentException("ringSize must be >=0");
			return (int) (Math.random() * ringSize);
		}

		/**
		 * Returns the minimum of three given integer values.
		 * 
		 * @param a the first value
		 * @param b the second value
		 * @param c the third value
		 * @return the minimum the three values
		 */
		public static int getMinimum(int a, int b, int c) {
			int min = a;
			if (b < min)
				min = b;
			if (c < min)
				min = c;
			return min;

		}

	}
}
