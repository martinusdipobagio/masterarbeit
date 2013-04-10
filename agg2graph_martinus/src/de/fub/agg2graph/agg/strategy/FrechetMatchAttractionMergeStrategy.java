package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.structs.BoundedQueue;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;

public class FrechetMatchAttractionMergeStrategy extends
		AbstractAggregationStrategy {

	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.default.strategy");

	public int maxLookahead = 7;
	public double maxPathDifference = 1000;
	public double maxInitDistance = 150;

	public enum State {
		NO_MATCH, IN_MATCH
	}

	private State state = State.NO_MATCH;
	List<AggNode> internalAggNodes = new ArrayList<AggNode>();

	
	public FrechetMatchAttractionMergeStrategy() {
		TraceDistanceFactory.setClass(FreeSpaceMatch.class);
		traceDistance = TraceDistanceFactory.getObject();
		MergeHandlerFactory.setClass(AttractionForceMerge.class);
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
				internalAggNodes.add(node);
			i++;
			}

			return;
		}

		BoundedQueue<ILocation> lastParsedCurrentPoints = new BoundedQueue<ILocation>(
				5);
		int i = 0;
		while (i < segment.size()) {
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

			/* Tinus - Filtering near points */
			nearPoints = filterNearPoints(nearPoints);

			logger.log(Level.FINE, "near points: " + nearPoints);

			boolean isMatch = true;
			if (nearPoints.size() == 0) {
				isMatch = false;
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
				for (List<AggNode> path : paths) {
					filterPath(path);
				}

				// evaluate paths, pick best, continue
				logger.log(Level.FINE, "Paths from " + nearPoints + " in agg: "
						+ paths);
				double bestDifference = Double.MAX_VALUE, difference;
				int length;
				List<AggNode> bestPath = null;
				List<GPSPoint> bestTrace = null;
				int bestPathLength = 0;

				for (List<AggNode> path : paths) {
					Object[] returnValues = traceDistance.getPathDifference(
							path, segment, i, mergeHandler);
					if(returnValues == null)
						continue;
					difference = (Double) returnValues[0];
					length = (int) Math.round(Double.valueOf(returnValues[1]
							.toString()));
					logger.info(String.format(
							"Difference of path %s and %s is %.3f", path,
							segment.subList(i, i + length), difference));
					if (difference < bestDifference
							|| (difference == bestDifference && length > bestPathLength)) {
						bestDifference = difference;
						logger.log(Level.FINE, "This is the new best path.");
						bestPathLength = length;
						bestPath = new ArrayList<AggNode>((List<AggNode>)returnValues[2]);
						bestTrace = new ArrayList<GPSPoint>((List<GPSPoint>)returnValues[3]);
						if (bestPath.size() == 0) {
							int j = i;
							i = j;
						}
					}
				}

				// do we have a successful match?
				if (bestDifference >= maxPathDifference || bestPath == null) {
					// i++;
					logger.log(Level.FINE,
							"Best path not good enough (anymore)");
					isMatch = false;
				} else if (bestPath.size() <= 1 && bestPathLength <= 1) {
					isMatch = false;
				}

				state = isMatch ? State.IN_MATCH : State.NO_MATCH;
				if (isMatch) {
					// make a merge handler if the match would start here
					if (lastState == State.NO_MATCH) {
						mergeHandler = baseMergeHandler.getCopy();
						mergeHandler.setAggContainer(aggContainer);
					}
					logger.log(
							Level.FINE,
							String.format(
									"best path found: %s, value: %.8f\ncomsuming %d GPS points: %s",
									bestPath, bestDifference, bestPathLength,
									segment.subList(i, i + bestPathLength)));
					mergeHandler.addAggNodes(aggNodesExchange(bestPath));
					mergeHandler.addGPSPoints(bestTrace);
					mergeHandler.setDistance(bestDifference);
					logger.log(Level.FINE, "Path so far: " + mergeHandler);
					i = i + bestPathLength - 1;
				}
			}

			if (!isMatch
					&& (lastState == State.IN_MATCH && (state == State.NO_MATCH || i == segment
							.size() - 1))) {
				finishMatch();
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
		}
		// step 2 and 3 of 3: ghost points, merge everything
		// System.out.println("MATCHES : " + matches.size());
//		int locCounter = 0;
		for (IMergeHandler match : matches) {
			// System.out.println(++locCounter + ". Match");
			System.out.println(match.getAggNodes());
			if (!match.isEmpty()) {
				match.mergePoints();
			}
		}
	}
	
	/**
	 * Problem with new class
	 * @param bestPath
	 * @return
	 */
	private List<AggNode> aggNodesExchange(List<AggNode> bestPath) {
		AggNode currentNode;
		for(int i = 0; i < bestPath.size(); i++) {
			currentNode = bestPath.get(i);
			for(AggNode internal : internalAggNodes) {
				if(currentNode.getLat() == internal.getLat() && currentNode.getLon() == internal.getLon()) {
					bestPath.remove(i);
					bestPath.add(i, internal);
					break;
				}
			}
		}
		
		return bestPath;
	}
	
	private static void filterPath(List<AggNode> path) {
		boolean found = false;
		for (int i = 0; i < path.size(); i++) {
			if (!found && !path.get(i).isRelevant())
				found = true;
			else if (found) {
				path.remove(i);
				i--;
			}
		}
	}

	private Set<AggNode> filterNearPoints(Set<AggNode> nearPoints) {
		Iterator<AggNode> nearIt = nearPoints.iterator();
		while (nearIt.hasNext()) {
			if (!nearIt.next().isRelevant())
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
}
