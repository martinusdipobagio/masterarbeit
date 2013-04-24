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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.AggregationStrategyFactory;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.agg.strategy.DefaultMatchDefaultMergeStrategy.State;
import de.fub.agg2graph.input.GPXWriter;
import de.fub.agg2graph.input.SerializeAgg;
import de.fub.agg2graph.structs.BoundedQueue;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.FrechetDistance.Cell;
import de.fub.agg2graph.structs.frechet.Pair;

public class SecondAggregationStrategy extends AbstractAggregationStrategy {
	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.default.strategy");

	public int maxLookahead = Integer.MAX_VALUE;
	public double maxPathDifference = 500; // 1000;
	public double maxInitDistance = 10; // 150;
	public int counter = 0;

	public enum State {
		NO_MATCH, IN_MATCH
	}

	private State state = State.NO_MATCH;

	/**
	 * Preferably use the {@link AggregationStrategyFactory} for creating
	 * instances of this class.
	 */
	public SecondAggregationStrategy() {
		TraceDistanceFactory.setClass(ConformalPathDistance.class);
		traceDistance = TraceDistanceFactory.getObject();
		MergeHandlerFactory.setClass(IterativeClosestPointsMerge.class);
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void aggregate(GPSSegment segment) {
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
				|| aggContainer.getCachingStrategy().getNodeCount() == 0) {
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

			return;
		}

		BoundedQueue<ILocation> lastParsedCurrentPoints = new BoundedQueue<ILocation>(
				5);
		int i = 0;
//		System.out.println("BEFORE : " + aggContainer.getCachingStrategy().getLoadedConnections());
//		System.out.println("Segment Size : " + segment.size());
		while (i < segment.size()) {
//			System.out.println("Segment : " + i);
			// step 1: find starting point
			// get close points, within 10 meters (merge candidates)
			Set<AggNode> nearPoints = null;
			GPSPoint currentPoint = segment.get(i);
			logger.log(Level.FINE, "current point: " + currentPoint);

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

			State lastState = state;

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

			logger.log(Level.FINE, "near points: " + nearPoints);

			boolean isMatch = true;
			if (nearPoints.size() == 0) {
				isMatch = false;
			} else {
				// get only nearest Point
				AggNode nearest = nearestPoint(currentPoint, nearPoints);
				// unnecessary, but needed atm
				Set<AggNode> nearestSet = new HashSet<AggNode>();
				nearestSet.add(nearest);
				// there is candidates for a match start
				List<List<AggNode>> paths = getPathsByDepth(nearestSet, 1,
						maxLookahead);

				// evaluate paths, pick best, continue
				logger.log(Level.FINE, "Paths from " + nearPoints + " in agg: "
						+ paths);
				double bestDifference = Double.MAX_VALUE, difference;
				int length;
				List<AggNode> bestPath = null;
				List<List<Cell>> trails = null;
				int bestPathLength = 0;

				for (List<AggNode> path : paths) {
					Object[] returnValues = traceDistance.getPathDifference(
							path, segment, i, mergeHandler);
					difference = (Double) returnValues[0];
					length = (int) Math.round(Double.valueOf(returnValues[1]
							.toString()));
					if (difference < bestDifference
							|| (difference == bestDifference && length > bestPathLength)) {
						bestDifference = difference;
						logger.log(Level.FINE, "This is the new best path.");
						bestPathLength = length;
						bestPath = path;
						trails = (List<List<Cell>>) returnValues[2];
						if (bestPath.size() == 0) {
							int j = i;
							i = j;
						}
					}
				}
				
				if(trails == null)
					isMatch = false;
				
				state = isMatch ? State.IN_MATCH : State.NO_MATCH;
				if (isMatch) {
					// make a merge handler if the match would start here
					if (lastState == State.NO_MATCH) {
						mergeHandler = baseMergeHandler.getCopy();
						mergeHandler.setAggContainer(aggContainer);
					}
					for (List<Cell> trail : trails) {
						// TODO
						List<AggNode> agg = extractAggNode(trail);
						List<GPSPoint> trace = extractTrace(trail);
						mergeHandler.addAggNodes(agg);
						mergeHandler.addGPSPoints(trace);
						mergeHandler.setDistance(bestDifference);
					}	
					if(i < trails.get(trails.size() - 1).get(0).j)
						i = trails.get(trails.size() - 1).get(0).j + 1;
				}
			}

			if (!isMatch
					&& (lastState == State.IN_MATCH && (state == State.NO_MATCH || i == segment
							.size() - 1))) {
				finishMatch();
			} else if (!isMatch && lastState == State.NO_MATCH) {
				// if there is no close points or no valid match, add it to the
				// aggregation
				// AggNode node = new AggNode(currentPoint, aggContainer);
				// node.setID("A-" + currentPoint.getID());
				// addNodeToAgg(aggContainer, node);
				// lastNode = node;
				i++;
			}
		}
//		System.out.println("AFTER : " + aggContainer.getCachingStrategy().getLoadedConnections());

		// step 2 and 3 of 3: ghost points, merge everything
		// System.out.println("MATCHES : " + matches.size());
		// int locCounter = 0;
		for (IMergeHandler match : matches) {
			// System.out.println(++locCounter + ". Match");
//			System.out.println(match.getAggNodes().size());
//			System.out.println(match.getGpsPoints().size());
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
	}

	/**
	 * The return value from the trace distance is a list of cells. The AggNode can be
	 * extracted from this cells
	 * @param cells
	 * @return
	 */
	private List<AggNode> extractAggNode(List<Cell> cells) {
		Set<AggConnection> conns = aggContainer.getCachingStrategy()
				.getLoadedConnections();
		List<AggNode> ret = new ArrayList<AggNode>();	// the extracted node
		List<AggNode> copy = new ArrayList<AggNode>();	// ignoring copy elements
		
		//For every cells, get the nodes from the "FromNode". For the last cell, take both "FromNode" and "ToNode"
		//TODO: There are still problem if a cell has two new nodes "FromNode" and "ToNode"
		for (int t = cells.size() - 1; t >= 0; t--) {
			AggConnection current = cells.get(t).p;
			for (AggConnection conn : conns) {
				if (conn.getFrom().getLat() == current.getFrom().getLat()
						&& conn.getFrom().getLon() == current.getFrom()
								.getLon()
						&& conn.getTo().getLat() == current.getTo().getLat()
						&& conn.getTo().getLon() == current.getTo().getLon()) {

					AggNode newNode = conn.at((double)cells.get(t).from.x / cells.get(t).width);
					if(cells.get(t).from.x > 0 && cells.get(t).from.x < cells.get(t).width-1 && !copy.contains(newNode)) {
						aggContainer.insertNode(newNode, conn);
						conns = aggContainer.getCachingStrategy()
								.getLoadedConnections();
						copy.add(newNode);
					}
					ret.add(newNode);
					
					continue;
				}
			}
		}
		
		//Last Cell
		Cell lastCell = cells.get(0);
		AggConnection current = lastCell.p;
		for (AggConnection conn : conns) {
			if (conn.getFrom().getLat() == current.getFrom().getLat()
					&& conn.getFrom().getLon() == current.getFrom()
							.getLon()
					&& conn.getTo().getLat() == current.getTo().getLat()
					&& conn.getTo().getLon() == current.getTo().getLon()) {

				AggNode newNode = conn.at((double)lastCell.to.x / lastCell.width);
				if(lastCell.to.x > 0 && lastCell.to.x < lastCell.width-1) {
					aggContainer.insertNode(newNode, conn);
					conns = aggContainer.getCachingStrategy()
							.getLoadedConnections();
				}
				ret.add(newNode);

				continue;
			}
		}
		return ret;
	}

	/**
	 * The return value from the trace distance is a list of cells. The GPSEdge can be
	 * extracted from this cells
	 * @param cells
	 * @return
	 */
	private List<GPSPoint> extractTrace(List<Cell> cells) {
		List<GPSPoint> ret = new ArrayList<GPSPoint>();
		//For every cells, get the nodes from the "FromNode". For the last cell, take both "FromNode" and "ToNode"
		//TODO: There are still problem if a cell has two new nodes "FromNode" and "ToNode"
		for (int t = cells.size() - 1; t >= 0; t--) {
			GPSPoint current = cells.get(t).q.at((double)cells.get(t).from.y / cells.get(t).width);
			if(!ret.contains(current))
				ret.add(current);
		}
		//Last Cell
		Cell lastCell = cells.get(0);
		GPSPoint current = lastCell.q.at((double)lastCell.to.y / lastCell.width);
		ret.add(current);
		return ret;
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

	@Override
	public String toString() {
		return "SecondAggregation";
	}
}
