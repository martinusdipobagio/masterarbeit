package de.fub.agg2graph.structs.frechet;

import de.fub.agg2graph.structs.frechet.FrechetDistance.Cell;

public class CellContainer {

	private Cell[][] cells;
	
	public CellContainer(FrechetDistance fd) {
		//i dan j ditukar?
		cells = new Cell[fd.getSizeP()][fd.getSizeQ()];
		for(Cell c : fd.cells) {
			cells[c.i][c.j] = c;
		}
	}
	
	public void getScore(int i, int j) {
		getScore(i-1, j);
		getScore(i, j-1);
	}

	
}
