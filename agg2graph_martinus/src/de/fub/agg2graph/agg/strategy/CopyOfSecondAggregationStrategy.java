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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.AggregationStrategyFactory;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.agg.strategy.DefaultMatchAttractionMergeStrategy.State;
import de.fub.agg2graph.input.GPXWriter;
import de.fub.agg2graph.input.SerializeAgg;
import de.fub.agg2graph.management.MyStatistic;
import de.fub.agg2graph.structs.BoundedQueue;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;

public class CopyOfSecondAggregationStrategy extends AbstractAggregationStrategy {
	public int maxLookahead = Integer.MAX_VALUE;
	public double maxPathDifference = 20;
	public double maxInitDistance = 12.5;
	
	//Statistics variable
	@SuppressWarnings("unused")
	private double aggLength = 0;
	@SuppressWarnings("unused")
	private double traceLength = 0;
	@SuppressWarnings("unused")
	private double matchedAggLength = 0;
	@SuppressWarnings("unused")
	private double matchedTraceLength = 0;

	public enum State {
		NO_MATCH, IN_MATCH
	}

	private State state = State.NO_MATCH;

	/**
	 * Preferably use the {@link AggregationStrategyFactory} for creating
	 * instances of this class.
	 */
	public CopyOfSecondAggregationStrategy() {
		TraceDistanceFactory.setClass(ConformalPathDistance.class);
		traceDistance = TraceDistanceFactory.getObject();
		MergeHandlerFactory.setClass(IterativeClosestPointsMerge.class);
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void aggregate(GPSSegment segment, boolean isAgg) {
		
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
			aggLength =  GPSCalc.traceLengthMeter(segment);
			return;
		}

		BoundedQueue<ILocation> lastParsedCurrentPoints = new BoundedQueue<ILocation>(
				5);
		int i = 0;
		traceLength = GPSCalc.traceLengthMeter(segment);
		while (i < segment.size()) {
			// step 1: find starting point
			// get close points, within 10 meters (merge candidates)
			Set<AggNode> nearPoints = null;
			GPSPoint currentPoint = segment.get(i);

			// no progress? (should not be necessary)
			if (lastParsedCurrentPoints.size() > 2
					&& lastParsedCurrentPoints.get(
							lastParsedCurrentPoints.size() - 1).equals(
							currentPoint)
					&& lastParsedCurrentPoints.get(
							lastParsedCurrentPoints.size() - 2).equals(
							currentPoint)) {
				i++;
				continue;
			}
			lastParsedCurrentPoints.offer(currentPoint);

//			State lastState = state;

			// get all close points, but none that are already in the current
			// match (because we would kinda search backwards)
			nearPoints = aggContainer.getCachingStrategy().getCloseNodes(
					currentPoint, maxInitDistance);
			if (mergeHandler != null) {
				List<AggNode> nodes = mergeHandler.getAggNodes();
				for (int j = 0; j < nodes.size() - 1; j++) {
					nearPoints.remove(nodes.get(j));
				}
			}

			/* Tinus - Filtering near points */
			nearPoints = filterNearPoints(nearPoints);

			boolean isMatch = true;
			if (nearPoints.size() == 0) {
				i++;
				continue;
			} else {
				// get only nearest Point
				AggNode nearest = nearestPoint(currentPoint, nearPoints);
				//unnecessary, but needed atm
				Set<AggNode> nearestSet = new HashSet<AggNode>();
				nearestSet.add(nearest);
				// there is candidates for a match start
				List<List<AggNode>> paths = getPathsByDepth(nearestSet, 1,
						maxLookahead);

				/* Tinus - Filtering Paths */
				removeSamePath(paths);
				for (List<AggNode> path : paths) {
					filterPath(path);
				}
				
				// evaluate paths, pick best, continue
				List<List<AggNode>> bestAggResult = new ArrayList<List<AggNode>>();
				List<List<GPSPoint>> bestTraceResult = new ArrayList<List<GPSPoint>>();
				double value, bestValue = 0;
								
				for (List<AggNode> path : paths) {

					Object[] returnValues = traceDistance.getPathDifference(
							path, segment, i, mergeHandler);
					List<List<AggNode>> aggResult = (List<List<AggNode>>) returnValues[1];
					List<List<GPSPoint>> traceResult = (List<List<GPSPoint>>) returnValues[2];
					if(aggResult.size() == 0 || traceResult.size() == 0
							|| aggResult.size() != traceResult.size())
						continue;
					
					value = getValue(aggResult, traceResult);
					if(value > bestValue) {
						bestValue = value;
						bestAggResult = aggResult;
						bestTraceResult = traceResult;
					}
				}

				// do we have a successful match?
				if (bestValue == 0)
					isMatch = false;

				state = isMatch ? State.IN_MATCH : State.NO_MATCH;
				if (isMatch) {
					for(int j = 0; j < Math.min(bestAggResult.size(), bestTraceResult.size()); j++) {
						mergeHandler = baseMergeHandler.getCopy();
						mergeHandler.setAggContainer(aggContainer);
						
						mergeHandler.addAggNodes(bestAggResult.get(j));
						mergeHandler.addGPSPoints(bestTraceResult.get(j));
						mergeHandler.setDistance(0); //TODO
						finishMatch();
					}
//					i = i + segmentLength - 1;
				}
				break;
			}

//			if (!isMatch
//					&& (lastState == State.IN_MATCH && (state == State.NO_MATCH || i == segment
//							.size() - 1))) {
//				finishMatch();
//				i++; //TODO Martinus
//			} else if (!isMatch && lastState == State.NO_MATCH) {
//				// if there is no close points or no valid match, add it to the
//				// aggregation
//				// Dibutuhkan kalau butuh cabang baru
////				AggNode node = new AggNode(currentPoint, aggContainer);
////				node.setID("A-" + currentPoint.getID());
////				addNodeToAgg(aggContainer, node);
////				lastNode = node;
//				i++;
//			}
		}
		// step 2 and 3 of 3: ghost points, merge everything
		System.out.println("MATCHES : " + matches.size());
//		int locCounter = 0;
		matchedAggLength = 0;
		matchedTraceLength = 0;
		for (IMergeHandler match : matches) {
//			System.out.println(++locCounter + ". Match");
//			System.out.println(match.getAggNodes());
//			System.out.println(match.getGpsPoints());
//			System.out.println();
			matchedAggLength += GPSCalc.traceLengthMeter(match.getAggNodes());
			matchedTraceLength += GPSCalc.traceLengthMeter(match.getGpsPoints());
			if (!match.isEmpty()) {
				match.mergePoints();
			}
		}
		
		//TODO Statistik-Zeug
		System.out.println(this.aggLength);
		System.out.println(this.matchedAggLength);
		System.out.println(this.traceLength);
		System.out.println(this.matchedTraceLength);
		List<Double> value = new ArrayList<Double>();
		value.add(this.aggLength);
		value.add(this.matchedAggLength);
		value.add(this.traceLength);
		value.add(this.matchedTraceLength);
//		try {
//			MyStatistic.writefile("test/exp/SecondStrategy.txt", value);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private double getValue(List<List<AggNode>> aggResult,
			List<List<GPSPoint>> traceResult) {
		double value = 0;
		for(int i = 0; i < Math.min(aggResult.size(), traceResult.size()); i++) {
			value = value + GPSCalc.traceLengthMeter((List<? extends ILocation>) aggResult.get(i)) + GPSCalc.traceLengthMeter((List<? extends ILocation>) traceResult.get(i));
		}
		System.out.println("Value : " + value);
		return value;
	}

	private static void filterPath(List<AggNode> path) {
		boolean found = false;
		for(int i = 0 ; i < path.size() ; i++) {
			if(!found && !path.get(i).isRelevant())
				found = true;
			else if(found) {
				path.remove(i);
				i--;
			}
		}
	}

	private Set<AggNode> filterNearPoints(Set<AggNode> nearPoints) {
		Iterator<AggNode> nearIt = nearPoints.iterator();
		while(nearIt.hasNext()) {
			if(!nearIt.next().isRelevant())
				nearIt.remove();
		}
		return nearPoints;
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

	/*
	 * reverse paths
	 */
	private List<List<AggNode>> getPathsByDepth(Set<AggNode> nearPoints,
			int minDepth, int maxDepth) {
		List<List<AggNode>> paths = new ArrayList<List<AggNode>>();
		for (AggNode startNode : nearPoints) {
			List<AggNode> path = new ArrayList<AggNode>();
			path.add(startNode);
			addPaths(paths, path, 1, minDepth, maxDepth);
		}
		return paths;
	}

	private void addPaths(List<List<AggNode>> paths, List<AggNode> path,
			int depth, int minDepth, int maxDepth) {
		if (depth > maxDepth) {
			return;
		}
		// add out nodes
		// TODO load node if necessary instead of null check
		if (path.get(depth - 1).getOut() != null) {
			for (AggConnection outConn : path.get(depth - 1).getOut()) {
				AggNode outNode = outConn.getTo();
				path.add(outNode);
				if (depth >= minDepth) {
					ArrayList<AggNode> pathCopy = new ArrayList<AggNode>();
					pathCopy.addAll(path);
					paths.add(pathCopy);
				}
				addPaths(paths, path, depth + 1, minDepth, maxDepth);
				path.remove(path.size() - 1);
			}
		}
	}

	@Override
	public String toString() {
		return "SecondAggregation";
	}
	
	private static AggNode nearestPoint(ILocation current,
			Set<AggNode> nearPoints) {
		double bestDistance = Double.MAX_VALUE;
		double distance;
		AggNode best = null;
		for (AggNode point : nearPoints) {
			distance = GPSCalc.getDistanceTwoPointsMeter(current, point);
			if (bestDistance > distance) {
				bestDistance = distance;
				best = point;
			}
		}

		return best;
	}
	
	/**
	 * to remove same path. Bug from addPaths
	 * @param paths
	 */
	private void removeSamePath(List<List<AggNode>> paths) {
		for(int i = 0; i < paths.size(); i++) {
			for(int j = 0; j < paths.size(); j++) {
				if(paths.get(i).containsAll(paths.get(j)) && i != j) {
					paths.remove(j);
					if(i > j)
						i--;
					j--;
				}
			}
		}
	}
}
