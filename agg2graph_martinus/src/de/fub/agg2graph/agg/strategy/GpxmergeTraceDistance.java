/*******************************************************************************
 * Copyright (c) 2012 Johannes Mitlmeier.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/agpl-3.0.html
 * 
 * Contributors:
 *     Johannes Mitlmeier - initial API and implementation
 ******************************************************************************/
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

public class GpxmergeTraceDistance implements ITraceDistance {

	public double angleFactor = 0.7;

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
		double gradient = GPSCalc.getSmallGradientFromEdges(near, currentEdge);
		if (gradient > 1000) {
			return Double.MAX_VALUE;
		}
		double distFrom = GPSCalc.getDistancePointToEdgeMeter(near.getFrom(),
				currentEdge);
		double distTo = GPSCalc.getDistancePointToEdgeMeter(near.getTo(),
				currentEdge);
		double length = GPSCalc.getDistanceTwoPointsMeter(currentEdge.getFrom(),
				currentEdge.getTo());
		double dist = gradient * angleFactor + ((distFrom + distTo) / length)
				* (1 - angleFactor);
		return dist;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		return null;
	}
}
