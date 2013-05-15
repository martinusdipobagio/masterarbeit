package de.fub.agg2graph.agg.strategy;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.CheckPath;
import de.fub.agg2graph.structs.frechet.FrechetDistance;
import de.fub.agg2graph.structs.frechet.IAggregatedMap;
import de.fub.agg2graph.structs.frechet.ITrace;
import de.fub.agg2graph.structs.frechet.FrechetDistance.Cell;

public class ConformalPathDistance implements ITraceDistance {

//	public double maxDistance = 25;
//
//	public static AggContainer aggContainer;
//	public IAggregatedMap map;
//	public ITrace trace;
//	public ILocation start;
	
	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
//		double bestValue = 0;
//		double bestValueLength = 0;
//		int segmentLength = 0;
//		
//		//1. Input: Converting List<GPSPoint> to List<GPSEdge>
//		List<GPSEdge> path1 = new ArrayList<GPSEdge>();
//		for(int i = 0; i < aggPath.size() - 1; i++) {
//			path1.add(new GPSEdge(aggPath.get(i), aggPath.get(i+1)));
//		}
//
//		//2. Input: Converting List<GPSPoint> to List<GPSEdge>
//		List<GPSEdge> path2 = new ArrayList<GPSEdge>();
//		for(int i = startIndex; i < tracePoints.size() - 1; i++) {
//			path2.add(new GPSEdge(tracePoints.get(i), tracePoints.get(i+1)));
//		}
//		
//		//Frechet Distance and generate FreeSpaceDiagram
//		FrechetDistance fd = new FrechetDistance(path1, path2, maxDistance/92500);
//		drawImaginaryFreeSpace(fd);
//		
//		
//		//Generate Best Trail
//		CellContainer container = new CellContainer(fd);
//		container.getTrail();
//		List<List<Cell>> ret = container.getBestTrails();
//
//		//	To determine the score of a trail. L1 Distances in white regions are calculated
//		for(List<Cell> cells : ret) {
//			for(Cell c : cells) {
//				bestValueLength += ((c.to.x - c.from.x) + (c.to.y + c.from.y));
//			}
//		}
//		
//		// TODO Best Value
//		
//		//
//		segmentLength = fd.getSizeQ();
//		
//		return new Object[] { bestValue, bestValueLength, ret, segmentLength };
		return null;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}
	
	/**
	 * This class force @FrechetDistance to generate FreeSpaceDiagram.
	 * For the optimization of calculation, there will be no real image.
	 * @param f
	 */
//	private void drawImaginaryFreeSpace(FrechetDistance f) {
//		int tileSize = 80;
//		int startx = 0;
//		int starty = 0;
//		int endx = f.getSizeQ() - 1;
//		int endy = f.getSizeP() - 1;
//		BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
//
//		//For Every Cell
//		for(int y =  starty ; y <= endy; ++y) {
//			for(int x = startx; x <= endx; ++x) {
//				Cell cell = f.getCell((f.getSizeP() - y - 1 ), x);
//				if(cell == null)
//					continue;
//
//				cell.getFreeSpace(tile, tileSize);
//				cell.getParameterMarks(tile, tileSize);
//				tile = cell.getReachableMarks(tile, tileSize);
//
//			}
//		}
//	}	
}
