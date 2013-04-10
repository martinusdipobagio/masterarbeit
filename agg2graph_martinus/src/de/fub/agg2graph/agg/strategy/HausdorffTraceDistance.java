package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;

@SuppressWarnings("unused")
public class HausdorffTraceDistance implements ITraceDistance {
	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.default.dist");
	public double aggReflectionFactor = 4;
	public int maxOutliners = 10;
	public double maxDistance = 60;
	public int maxLookahead = 4;
	public double maxPathDifference = 100;
	public int minLengthFirstSegment = 1;
	public double maxAngle = 37;

	/**
	 * Compute the difference of a path to the aggregation. This measure only
	 * guarantees relative correctness when questioned repeatedly for different
	 * paths. A difference of 0 indicates equality while larger values indicate
	 * increasingly different paths.
	 * 
	 * @param aggPath
	 * @param tracePoints
	 * @param startIndex
	 * @param dmh
	 * @return Object[] { double bestValue, int bestValueLength }
	 */
	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
		double bestValue = Double.MIN_VALUE;
		double bestValueLength = 0;
		// List<AggNode> internalPath = new ArrayList<AggNode>();
		// internalPath.addAll(aggPath);
		List<AggNode> aggResult = new ArrayList<AggNode>();
		List<GPSPoint> traceResult = new ArrayList<GPSPoint>();
		/**
		 * START
		 */
		List<AggNode> aggLocations = aggPath;
		List<GPSPoint> tempAggLocations = new ArrayList<GPSPoint>();
		for (AggNode loc : aggLocations)
			tempAggLocations.add(loc);
		
		/* TEST */
//		List<GPSPoint> traceLocations = tracePoints.subList(startIndex,
//				tracePoints.size());
		/* UNSAFE */
		AggNode lastAgg = aggLocations.get(aggLocations.size() - 1);
		double bestLastDistance = Double.MAX_VALUE;
		int bestI = -1;
		for(int i = startIndex; i < tracePoints.size(); i++) {
			if(bestLastDistance > GPSCalc.getDistanceTwoPointsMeter(lastAgg, tracePoints.get(i))) {
				bestI = i;
				bestLastDistance = GPSCalc.getDistanceTwoPointsMeter(lastAgg, tracePoints.get(i));
			}
		}
		List<GPSPoint> traceLocations = (bestI > -1) ? tracePoints.subList(startIndex, bestI+1)
				: tracePoints.subList(startIndex, tracePoints.size());
		int j = 0;
		int bestK = 0;
		double distance = 0;
		double bestDistance = Double.MAX_VALUE;

		// step 1a: get nearest point in agg from trace and create new temporary
		// point if, the projection is not in agg
		// maxInitDistance is ignored in this phase. Relevancy is calculated
		// though
		ILocation currentNode, projection;
		while (j < traceLocations.size()) {

			currentNode = traceLocations.get(j);
			for (int k = 0; k < aggLocations.size() - 1; k++) {
				// if(!aggLocations.get(k).isRelevant())
				// continue;

				distance = GPSCalc.getDistancePointToEdgeMeter(currentNode,
						aggLocations.get(k), aggLocations.get(k + 1));

				if (bestDistance > distance && distance < maxDistance) {
					bestDistance = distance;
					bestK = k;
				}
			}

			// If the projection is not in data, then create new node
			projection = GPSCalc.getProjectionPoint(currentNode,
					aggLocations.get(bestK), aggLocations.get(bestK + 1));
			if (projection != null) {
				GPSPoint node = new GPSPoint(projection);
				// node.setK(aggLocations.get(bestK).getK());
				// node.setRelevant(aggLocations.get(bestK).isRelevant());
				// node.setID("A-" + j);
				tempAggLocations.add(bestK + 1, node);
			}
			bestDistance = Double.MAX_VALUE;
			j++;
		}

		// step 1b:
		j = 0;
		while (j < aggLocations.size() - 1) {
			AggNode currentFrom = aggLocations.get(j);
			AggNode currentTo = aggLocations.get(j + 1);

			// Distance check
			//
			// get the nearest points
			double bestDistFrom = Double.MAX_VALUE;
			double bestDistTo = Double.MAX_VALUE;
			int bestKFrom = -1, bestKTo = -1;
			for (int k = 0; k < traceLocations.size() - 1; k++) {
				double distFrom = GPSCalc.getDistancePointToEdgeMeter(
						currentFrom, traceLocations.get(k),
						traceLocations.get(k + 1));
				double distTo = GPSCalc.getDistancePointToEdgeMeter(currentTo,
						traceLocations.get(k), traceLocations.get(k + 1));
				if (bestDistFrom > distFrom) {
					bestDistFrom = distFrom;
					bestKFrom = k;
				}
				if (bestDistTo > distTo) {
					bestDistTo = distTo;
					bestKTo = k;
				}
			}

			if (bestDistFrom > maxDistance || bestDistTo > maxDistance) {
				break;
			}

			int tPointerFrom = getTempFromOriginal(currentFrom,
					tempAggLocations);
			int tPointerTo = getTempFromOriginal(currentTo, tempAggLocations);
			// should be impossible
			if (tPointerTo == -1) {
				break;
			}
			// Distance between two nodes
			boolean temp = true;
			double distTemp;
			while (tPointerTo - tPointerFrom > 1) {
				temp = false;
				// D
				for (int k = 0; k < traceLocations.size() - 1; k++) {
					distTemp = GPSCalc.getDistancePointToEdgeMeter(
							tempAggLocations.get(tPointerFrom),
							traceLocations.get(k), traceLocations.get(k + 1));
					// at least one distance < max
					if (distTemp < maxDistance) {
						temp = true;
						break;
					}
				}
				if (!temp) {
					break;
				}
				tPointerFrom++;
			}

			if (!temp) {
				break;
			}

			ILocation projFrom = GPSCalc.getProjectionPoint(currentFrom,
					traceLocations.get(bestKFrom),
					traceLocations.get(bestKFrom + 1));
			if (projFrom == null) {
				if (GPSCalc.getDistanceTwoPointsMeter(currentFrom,
						traceLocations.get(bestKFrom)) < GPSCalc
						.getDistanceTwoPointsMeter(currentFrom,
								traceLocations.get(bestKFrom + 1)))
					projFrom = traceLocations.get(bestKFrom);
				else
					projFrom = traceLocations.get(++bestKFrom);
			}

			ILocation projTo = GPSCalc.getProjectionPoint(currentTo,
					traceLocations.get(bestKTo),
					traceLocations.get(bestKTo + 1));
			if (projTo == null) {
				if (GPSCalc.getDistanceTwoPointsMeter(currentTo,
						traceLocations.get(bestKTo)) < GPSCalc
						.getDistanceTwoPointsMeter(currentTo,
								traceLocations.get(bestKTo + 1)))
					projTo = traceLocations.get(bestKTo);
				else
					projTo = traceLocations.get(++bestKTo);
			}

			if (projFrom != null && projTo != null) {
				if (j == 0) {
					aggResult.add(currentFrom);
					aggResult.add(currentTo);
					traceResult.add(new GPSPoint(projFrom));
					traceResult.add(new GPSPoint(projTo));
				} else {
					aggResult.add(currentTo);
					traceResult.add(new GPSPoint(projTo));
				}

				if (bestValue < Math.max(bestDistFrom, bestDistTo))
					bestValue = Math.max(bestDistFrom, bestDistTo);
			} else {
				break;
			}
			j++;
		}

		if (aggResult.size() != traceResult.size())
			System.err.println("Irgendwas stimmt nicht");

		bestValueLength = aggResult.size();

		if (aggResult.size() <= 1)
			return null;
		else
			return new Object[] { bestValue, bestValueLength, aggResult,
					traceResult };
		/**
		 * END
		 */
	}

	private int getTempFromOriginal(AggNode current, List<GPSPoint> temp) {
		for (int j = 0; j < temp.size(); j++) {
			if (current.getLat() == temp.get(j).getLat()
					&& current.getLon() == temp.get(j).getLon())
				return j;
		}
		return -1;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}

}
