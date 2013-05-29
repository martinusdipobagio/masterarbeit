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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.AggregationStrategyFactory;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.management.MyStatistic;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class GpxmergeMatchIterativeMergeStrategy extends
		AbstractAggregationStrategy {
	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.gpxmerge.strategy");

	public int maxLookahead = 5;
	public double maxPathDifference = 35;
	public double maxInitDistance = 15;

	public enum State {
		NO_MATCH, IN_MATCH
	}

	// Statistics variable
//	@SuppressWarnings("unused")
	private double aggLength = 0;
//	@SuppressWarnings("unused")
	private double traceLength = 0;
//	@SuppressWarnings("unused")
	private double matchedAggLength = 0;
//	@SuppressWarnings("unused")
	private double matchedTraceLength = 0;

	private State state = State.NO_MATCH;

	/**
	 * Preferably use the {@link AggregationStrategyFactory} for creating
	 * instances of this class.
	 */
	public GpxmergeMatchIterativeMergeStrategy() {
		TraceDistanceFactory.setClass(GpxmergeTraceDistance.class);
		traceDistance = TraceDistanceFactory.getObject();
		MergeHandlerFactory.setClass(IterativeClosestPointsMerge.class);
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	@Override
	public void aggregate(GPSSegment segment, boolean isAgg) {
		logger.setLevel(Level.OFF); // Level.ALL);

		// reset all attributes
		lastNode = null;
		mergeHandler = null;
		matches = new ArrayList<IMergeHandler>();
		state = State.NO_MATCH;

		// insert first segment without changes (assuming somewhat cleaned
		// data!)
		// attention: node counter is not necessarily accurate!
		if (aggContainer.getCachingStrategy() == null
				|| aggContainer.getCachingStrategy().getNodeCount() == 0
				|| isAgg) {
			int i = 0;
			while (i < segment.size()) {
				GPSPoint pointI = segment.get(i);
				AggNode node = new AggNode(pointI, aggContainer);
				node.setK(pointI.getK());
				node.setRelevant(pointI.isRelevant());
				node.setID("A-" + pointI.getID());
				addNodeToAgg(aggContainer, node);
				lastNode = node;
				i++;
			}
			aggLength = GPSCalc.traceLengthMeter(segment);
			return;
		}

		int i = 0;
		Set<AggConnection> nearEdges = null;
		traceLength = GPSCalc.traceLengthMeter(segment);

		while (i < segment.size()) {
			State lastState = state;

			if ((i == segment.size() - 1)) {
				if (lastState == State.IN_MATCH)
					finishMatch();
				break;
			}
			// step 1: find starting point
			// get close points, within 10 meters (merge candidates)
			// START
			GPSPoint firstPoint = segment.get(i);
			GPSPoint secondPoint = segment.get(i + 1);
			GPSEdge currentEdge = new GPSEdge(firstPoint, secondPoint);

			nearEdges = aggContainer.getCachingStrategy().getCloseConnections(
					currentEdge, maxInitDistance);
			// END

			boolean isMatch = true;
			if (nearEdges.size() == 0) {
				isMatch = false;
				state = State.NO_MATCH;
			} else {
				Iterator<AggConnection> itNear = nearEdges.iterator();
				Double grade = Double.MAX_VALUE;
				AggConnection bestConn = null;
				double dist = Double.MAX_VALUE;
				while (itNear.hasNext()) {
					AggConnection near = itNear.next();
					Object[] distReturn = traceDistance.getPathDifference(
							near.toPointList(), segment, i, mergeHandler);
					dist = (Double) distReturn[0];
					if (dist < maxPathDifference && dist < grade) {
						grade = dist;
						bestConn = near;
					}
				}

				// do we have a successful match?
				if (bestConn == null) {
					isMatch = false;
				}
				// else if (bestPath.size() <= 1 && bestPathLength <= 1) {
				//
				// }

				state = isMatch ? State.IN_MATCH : State.NO_MATCH;
				if (isMatch) {
					// System.out.println("I = " + i);
					// System.out.println(bestConn.getFrom() + " : " +
					// bestConn.getTo());
					// System.out.println(currentEdge.getFrom() + " : " +
					// currentEdge.getTo());
					// make a merge handler if the match would start here
					if (lastState == State.NO_MATCH) {
						mergeHandler = baseMergeHandler.getCopy();
						mergeHandler.setAggContainer(aggContainer);
					}
					// isMatch = false;
					if (!mergeHandler.getAggNodes()
							.contains(bestConn.getFrom()))
						mergeHandler.addAggNode(bestConn.getFrom());
					if (!mergeHandler.getAggNodes().contains(bestConn.getTo()))
						mergeHandler.addAggNode(bestConn.getTo());
					if (!mergeHandler.getGpsPoints().contains(
							currentEdge.getFrom()))
						;
					mergeHandler.addGPSPoint(currentEdge.getFrom());
					if (!mergeHandler.getGpsPoints().contains(
							currentEdge.getTo()))
						;
					mergeHandler.addGPSPoint(currentEdge.getTo());

					mergeHandler.setDistance(grade);
					i++;
				}
			}

			if (!isMatch
					&& (lastState == State.IN_MATCH && (state == State.NO_MATCH || i == segment
							.size() - 1))) {
				finishMatch();
				i++;
			} else if (!isMatch && lastState == State.NO_MATCH) {
				// if there is no close points or no valid match, add it to the
				// aggregation
				// Dibutuhkan kalau butuh cabang baru
				// AggNode node = new AggNode(currentPoint, aggContainer);
				// node.setID("A-" + currentPoint.getID());
				// addNodeToAgg(aggContainer, node);
				// lastNode = node;
				i++;
			} 
//			else {
//				i++;
//			}
			// System.out.println(isMatch + " : " + lastState + " " + state);
		}
		// step 2 and 3 of 3: ghost points, merge everything
		// System.out.println("MATCHES : " + matches.size());
		// int locCounter = 0;
		matchedAggLength = 0;
		matchedTraceLength = 0;
		for (IMergeHandler match : matches) {
			// System.out.println(++locCounter + ". Match");
			 System.out.println(match.getAggNodes());
			// System.out.println(match.getGpsPoints());
			matchedAggLength += GPSCalc.traceLengthMeter(match.getAggNodes());
			matchedTraceLength += GPSCalc
					.traceLengthMeter(match.getGpsPoints());
			if (!match.isEmpty()) {
				match.mergePoints();
			}
		}

		// try {
		// new File("test/input/output-test").mkdirs();
		// AggNode source = getLastNode();
		//
		// List<GPSSegment> segments = SerializeAgg.getSerialize(source);
		// GPXWriter.writeSegments(new File(
		// new String("test/input/output-test/" + toString() + counter++ +
		// ".gpx")), segments);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// TODO Statistik-Zeug
		 System.out.println(this.aggLength);
		 System.out.println(this.matchedAggLength);
		 System.out.println(this.traceLength);
		 System.out.println(this.matchedTraceLength);
//		List<Double> value = new ArrayList<Double>();
//		value.add(this.aggLength);
//		value.add(this.matchedAggLength);
//		value.add(this.traceLength);
//		value.add(this.matchedTraceLength);
//		try {
//			MyStatistic.writefile("test/exp/GpxMatch-AttractionMerge.txt",
//					value);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	protected void finishMatch() {
		// last match is over now
		matches.add(mergeHandler);
		mergeHandler.processSubmatch();
		/*
		 * connect to previous node lastNode is the last non-matched node or the
		 * outNode of the last match
		 */
		aggContainer.connect(lastNode, mergeHandler.getInNode());
		mergeHandler.setBeforeNode(lastNode);
		// remember outgoing node (for later connection)
		lastNode = mergeHandler.getOutNode();
	}

	@Override
	public String toString() {
		return "DefaultMatch-DefaultMerge";
	}
}
