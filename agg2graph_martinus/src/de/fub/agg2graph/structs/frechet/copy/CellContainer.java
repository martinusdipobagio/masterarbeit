package de.fub.agg2graph.structs.frechet.copy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.copy.FrechetDistance.Cell;

/**
 * This class is used to determined the monotone (and later conformal path) in the
 * free space diagram. All possible path are computed with dynamic programming
 * 
 * @author Martinus
 * 
 */
public class CellContainer {

	// Free space cells
	public Cell[][] cells;

	// Parameter that secure the monotone requirements
	int maxI; // The highest i
	int maxJ; // The highest j
	int leftLimit = -1; // To guarantee monotony path, limit the left after a
						// new start (See also @minJ)
	int minJ = 0; // To guarantee monotony path, limit the left after a new
					// start (See also @leftLimit)

	// List of all possible trails. This will be reseted after a new start
	List<List<Cell>> allTrails;

	// List of all best trails from each start
	public List<List<Cell>> allBestTrails;

	// The Block represent Agg column
	List<Integer> relevancyBlocks = new ArrayList<Integer>();

	/**
	 * Constructor from the @FrechetDistance i = agg ; j = trace
	 * 
	 * @param fd
	 *            = used FrechetDistance
	 */
	public CellContainer(FrechetDistance fd) {
		allTrails  = new ArrayList<List<Cell>>();
		allBestTrails = new ArrayList<List<Cell>>();
		maxI = fd.getSizeP();
		maxJ = fd.getSizeQ();
		cells = new Cell[maxI + 1][maxJ];
		for (Cell c : fd.cells) {
			cells[c.i][c.j] = c;
			// Add all irrelevant cells
			if (!c.isRelevant && !relevancyBlocks.contains(c.i)) {
				relevancyBlocks.add(c.i);
			}
		}
	}

	/**
	 * Get the best trails. It is obligatory to call the function @getTrail() first,
	 * since this function mark and generate the best trails.
	 * @return
	 */
	public List<List<Cell>> getBestTrails() {
		return allBestTrails;
	}
	
	/**
	 * Just test method to get the extracted "FromNode" and "ToNode"
	 */
	public void printProjection() {
		
		for(List<Cell> bestPart : allBestTrails) {
			for(Cell bestCell : bestPart) {
				ILocation aggFrom = bestCell.p.at((double)bestCell.from.x / bestCell.width);
				ILocation aggTo = bestCell.p.at((double)bestCell.to.x / bestCell.width);
				ILocation traFrom = bestCell.q.at((double)bestCell.from.y / bestCell.width);
				ILocation traTo = bestCell.q.at((double)bestCell.to.y / bestCell.width);
				System.out.println(aggFrom);
				System.out.println(aggTo);
				System.out.println(traFrom);
				System.out.println(traTo);
			}

		}				
		
	}
	
	/**
	 * Check the direction True = right = trace && False = top = agg
	 * 
	 * @param before
	 *            = cell before
	 * @param after
	 *            = cell after
	 * @return
	 */
	public boolean getDirection(Cell before, Cell after) {
		if (after.i > before.i && after.j == before.j)
			return false;
		else if (after.j > before.j && after.i == before.i)
			return true;

		// This shall not executed
		System.out.println("something wrong");
		return false;
	}

	/**
	 * This function searches all possible trails from the cell container
	 * 
	 * Each searching are started after 0 and after @relevancyBlock and ended
	 * before @relevancyBlock.
	 */
	public void getTrail() {
		Collections.sort(relevancyBlocks);
		int minOffset = 0;

		for (Integer block : relevancyBlocks) {
			allTrails.clear();
			if (block - minOffset > 1) {
				getTrail(minOffset, (block - 1));
				List<Cell> longest = getLongestTrail();
				allBestTrails.add(longest);
				drawMonotoneTrail(longest);
			}

			minOffset = block + 1;
		}
		// Last part
		if (maxI - minOffset >= 1) {
			getTrail(minOffset, maxI - 1);
			List<Cell> longest = getLongestTrail();
			allBestTrails.add(longest);
			drawMonotoneTrail(longest);
		}
	}

	/**
	 * This function searches all possible trails from a block
	 * 
	 * @param min
	 *            = start (i_th column)
	 * @param max
	 *            = end (i_th column)
	 */
	private void getTrail(int min, int max) {
		int i = max;
		int j = maxJ - 1;
		if (!cells[i][j].isRelevant || cells[i][j].isWhiteEmpty()) {
			Point destination = getDestination(i, j);
			if (destination == null)
				return;
			i = destination.x;
			j = destination.y;
		}

		// Limits are not reached
		if (i > min && j > minJ) {
			// Searching in the direction lower AGG
			if (i > min) {
				if (cells[i - 1][j].isRelevant && !cells[i - 1][j].isWhiteEmpty()) {
					List<Cell> branch = new ArrayList<Cell>();
					allTrails.add(branch);
					branch.add(cells[i][j]);
					getTrail(min, i - 1, j, branch);
				}
			}
			// Searching in the direction lower TRA
			if (j > minJ) {
				if (cells[i][j - 1].isRelevant && !cells[i][j - 1].isWhiteEmpty()) {
					List<Cell> branch = new ArrayList<Cell>();
					allTrails.add(branch);
					branch.add(cells[i][j]);
					getTrail(min, i, j - 1, branch);
				}
			}
		}
		// Limits are reached
		else {
			List<Cell> branch = new ArrayList<Cell>();
			allTrails.add(branch);
			branch.add(cells[i][j]);
			// Searching in the direction lower AGG
			if (i > min) {
				if (cells[i - 1][j].isRelevant && !cells[i - 1][j].isWhiteEmpty()) {
					getTrail(min, i - 1, j, branch);
				}
			}
			// Searching in the direction lower TRA
			else if (j > minJ) {
				if (cells[i][j - 1].isRelevant && !cells[i][j - 1].isWhiteEmpty()) {
					getTrail(min, i, j - 1, branch);
				}
			}
		}
	}

	/**
	 * This function searches all possible trails from a block
	 * 
	 * @param min
	 *            = start (i_th column)
	 * @param max
	 *            = end (i_th column)
	 */
	private void getTrail(int min, int i, int j, List<Cell> branch) {
		// Limits are not reached
		if (i > min && j > minJ) {
			// Searching in the direction lower AGG
			if (i > min) {
				if (cells[i - 1][j].isRelevant && !cells[i - 1][j].isWhiteEmpty()) {
					branch.add(cells[i][j]);
					System.out.println("i = " + i + " : j = " + j);
					getTrail(min, i - 1, j, branch);
				}
			}
			// Searching in the direction lower TRA
			if (j > 0) {
				if (cells[i][j - 1].isRelevant && !cells[i][j - 1].isWhiteEmpty()) {
					List<Cell> newBranch = new ArrayList<Cell>();
					allTrails.add(newBranch);
					newBranch.addAll(branch);
					newBranch.add(cells[i][j]);
					System.out.println("i = " + i + " : j = " + j);
					getTrail(min, i, j - 1, newBranch);
				}
			}
		}
		// Limits are not reached
		else {
			branch.add(cells[i][j]);
			// Searching in the direction lower AGG
			if (i > min) {
				if (cells[i - 1][j].isRelevant && !cells[i - 1][j].isWhiteEmpty()) {
					getTrail(min, i - 1, j, branch);
				}
			}

			// Searching in the direction lower AGG
			else if (j > minJ) {
				if (cells[i][j - 1].isRelevant && !cells[i][j - 1].isWhiteEmpty()) {
					getTrail(min, i, j - 1, branch);
				}
			}
		}

	}

	/**
	 * Get the destination cell of a block. The destination should very right,
	 * top and have white region.
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	private Point getDestination(int i, int j) {
		Cell best = null;
		Point ret = null;

		for (int it = i; it >= 0; it--) {
			for (int jt = j; jt >= 0; jt--) {
				if (cells[it][jt].isRelevant && !cells[it][jt].isWhiteEmpty()) {
					if (best == null || (best.i + best.j) < (it + jt))
						best = cells[it][jt];
				}
			}
		}

		if (best != null) {
			ret = new Point(best.i, best.j);
		}

		return ret;
	}

	/**
	 * Get the longest path from the all possible trail. The longest path has
	 * the longest L1 distance
	 */
	private List<Cell> getLongestTrail() {
		// if (allTrails.size() == 0)
		// getTrail();
		int current;
		int best = 0;
		int index = 0;
		for (int k = 0; k < allTrails.size(); k++) {
			current = allTrails.get(k).get(0).i
					- allTrails.get(k).get(allTrails.get(k).size() - 1).i
					+ allTrails.get(k).get(0).j
					- allTrails.get(k).get(allTrails.get(k).size() - 1).j;
			if (current > best) {
				best = current;
				index = k;
			}
		}

		if (allTrails.size() == 0)
			return new ArrayList<Cell>();

		return allTrails.get(index);
	}

	/**
	 * After the longestPath is found, draw the path
	 * 
	 * @param trail
	 *            = the longest path
	 */
	private void drawMonotoneTrail(List<Cell> trail) {
		if (trail.size() <= 1)
			return;

		Point temp = new Point(-1, -1);

		for (int k = trail.size() - 1; k >= 0; k--) {

			// Start cell
			if (k == trail.size() - 1 && trail.size() > 1) {
				// if current start cell.j == previous destination cell.j
				if (minJ == trail.get(k).j) {
					trail.get(k).from = trail.get(k).setLowestFrom(leftLimit);
				} else {
					trail.get(k).from = trail.get(k).setLowestFrom(0);
				}
			}
			// Destination cell
			else if (k == 0) {
				if(temp.x > -1 && temp.y > -1)
					trail.get(k).from = temp;
				else
					trail.get(k).from = trail.get(k).setLowestFrom(0);
				leftLimit = trail.get(k).getLongestPath();
				minJ = trail.get(k).j;
				break;
			}
			// Other cell
			else {
				trail.get(k).from = temp;
			}

			// True = right = trace && False = top = agg
			boolean direction = getDirection(trail.get(k), trail.get(k - 1));
			trail.get(k).getPath(direction);
			if (direction) {
				temp = new Point(trail.get(k).to.x, 0);

			} else {
				temp = new Point(0, trail.get(k).to.y);
			}
		}
	}
	
}
