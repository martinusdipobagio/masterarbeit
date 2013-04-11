package de.fub.agg2graph.structs.frechet;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.structs.frechet.FrechetDistance.Cell;

public class CellContainer {

	private Cell[][] cells;
	
	public CellContainer(FrechetDistance fd) {
		//i = agg j = tra
		cells = new Cell[fd.getSizeP()][fd.getSizeQ()];
		for(Cell c : fd.cells) {
			cells[c.i][c.j] = c;
		}
		List<Cell> test = new ArrayList<Cell>();
		test.add(cells[7][5]);
		test.add(cells[6][5]);
		test.add(cells[6][4]);
		test.add(cells[5][4]);
		test.add(cells[5][3]);
		test.add(cells[4][3]);
		test.add(cells[4][2]);
		test.add(cells[3][2]);
		test.add(cells[2][2]);
		test.add(cells[2][1]);
		test.add(cells[1][1]);
		test.add(cells[1][0]);
		test.add(cells[0][0]);
		
		Point temp = new Point(0, 0);
		
		for(int i = test.size() - 1; i > 0; i--) {
			if(i == test.size() - 1) {
				test.get(i).from = new Point(0,0);
			} else {
				test.get(i).from = temp;
			}
			
			boolean direction = getDirection(test.get(i), test.get(i-1));
			test.get(i).goTo(direction);
			if(direction) {
				System.out.println("Right");
				temp = new Point(0, test.get(i).to.y);
			} else {
				System.out.println("Top");
				temp = new Point(test.get(i).to.x, 0);
			}
		}
	}
	
	//True = right && False = top
	public boolean getDirection(Cell before, Cell after) {
		if(after.i > before.i && after.j == before.j)
			return false;
		else if(after.j > before.j && after.i == before.i)
			return true;
		//shit happen
		System.out.println("SHIIIIT Happened");
		return false;
	}
	
	public List<List<Cell>> getTrail(int i, int j) {
		//links
		if(i > 0) {
			if(cells[i-1][j].isRelevant) {
//				getTrail(i-1, j, List<Cell>)
			}
		}
		//unten
		if(j > 0) {
			if(cells[i][j-1].isRelevant) {
				
			}
		}
		return null;
	}
	
}
