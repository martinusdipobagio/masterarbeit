package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.Projection;
import de.fub.agg2graph.structs.Projection.Mode;

public class HausdorffPointToPointTraceDistance implements ITraceDistance {
//	private static final Logger logger = Logger
//			.getLogger("agg2graph.agg.hausdorff.dist");
	public double aggReflectionFactor = 4;
	public int maxOutliners = 10;
	public double maxDistance = 60;
	public int maxLookahead = 10;
	public double maxPathDifference = 100;
	public int minLengthFirstSegment = 1;
	public double maxAngle = 37;
	
	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
		Projection[] ret = new Projection[1];
		GPSPoint currentPoint = tracePoints.get(0);
		
		if(aggPath.size() == 0)
			return null;
		else if(aggPath.size() == 1) {
			ret[0] = new Projection(GPSCalc.getDistanceTwoPointsMeter(currentPoint, aggPath.get(0)), 
					aggPath.get(0), Mode.METER);
		}
		
		double bestDist = Double.MAX_VALUE;
		AggNode currentNeighbour, nearestNeighbour = null;
		Iterator<AggNode> itAggPath = aggPath.iterator();
		while(itAggPath.hasNext()) {
			currentNeighbour = itAggPath.next();
			if(GPSCalc.getDistanceTwoPointsMeter(currentPoint, currentNeighbour) < bestDist) {
				nearestNeighbour = currentNeighbour;
				bestDist = GPSCalc.getDistanceTwoPointsMeter(currentPoint, nearestNeighbour);
			}
		}			
		ret[0] = new Projection(bestDist, nearestNeighbour, Mode.METER);
		return ret;
	}
	
	public double hausdorffDistance(List<AggNode> aggPath,
			List<GPSPoint> tracePoints) {
		/**
		 * [Agg -> Trace] + [-1] + [Trace -> Agg]
		 */
		double[] dist = new double[aggPath.size() + 1 + tracePoints.size()];
		double[] traceToAgg = getPointToLineDistances(aggPath, tracePoints);
		double[] aggToTrace = getPointToLineDistances(tracePoints, aggPath);
		double max1 = 0;
		double max2 = 0;
		double max = 0;
		for(int i = 0; i < traceToAgg.length; i++) {
			dist[i] = traceToAgg[i];
			if(max1 < dist[i] && dist[i] < Double.MAX_VALUE)
				max1 = dist[i];
		}
		
		for(int j = aggToTrace.length + 1; j < dist.length - 1; j++) {
			dist[j] = aggToTrace[j - (aggToTrace.length + 1)];
			if(max2 < dist[j] && dist[j] < Double.MAX_VALUE)
				max2 = dist[j];
		}
		
		max = Math.max(max1, max2);
		
		return max;
	}

	private double[] getPointToLineDistances(List<? extends ILocation> from,
			List<? extends ILocation> to) {
		double[] result = new double[from.size()];
		ILocation loc;
		for (int i = 0; i < from.size(); i++) {
			loc = from.get(i);			
				
			result[i] = GPSCalc.getDistancePointToTraceMeter(loc, to)[0];
		}
		return result;
	}
	
	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}

}
