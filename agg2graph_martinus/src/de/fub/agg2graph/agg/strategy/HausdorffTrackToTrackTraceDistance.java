package de.fub.agg2graph.agg.strategy;

import java.util.List;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;

public class HausdorffTrackToTrackTraceDistance implements ITraceDistance {
	// private static final Logger logger = Logger
	// .getLogger("agg2graph.agg.hausdorff.dist");
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
		if (aggPath.size() > 1 && tracePoints.size() > startIndex + 1) {
			return new Object[] {
					getDistanceViaGradient(
							aggPath.get(0).getConnectionTo(aggPath.get(1)),
							new GPSEdge(tracePoints.get(startIndex),
									tracePoints.get(startIndex + 1))), 1 };
		}
		return new Object[] { Double.MAX_VALUE, 0 };
	}

	private double getDistanceViaGradient(AggConnection near,
			GPSEdge currentEdge) {
		double distFrom = GPSCalc.getDistancePointToEdgeMeter(near.getFrom(),
				currentEdge);
		double distTo = GPSCalc.getDistancePointToEdgeMeter(near.getTo(),
				currentEdge);
		//TODO
		double dist = Math.max(distFrom, distTo);
		return dist;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		// TODO Auto-generated method stub
		return null;
	}

}
