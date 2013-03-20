package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;

public class HausdorffMatchAggregationStrategy extends AbstractAggregationStrategy {
//	private static final Logger logger = Logger
//			.getLogger("agg2graph.agg.hausdorff.strategy");

	public int maxLookahead = 7;
	public double maxPathDifference = 1000;
	public double maxInitDistance = 10;
	public boolean firstSegmentAdded = false;
	
	private List<AggNode> aggNodes;
	
	public enum State {
		NO_MATCH, IN_MATCH
	}
		
	public HausdorffMatchAggregationStrategy() {
		// Still default
		TraceDistanceFactory.setClass(HausdorffPointToPointTraceDistance.class);
		traceDistance = TraceDistanceFactory.getObject();
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	@Override
	public void aggregate(GPSSegment segment) {
		// reset all attributes
		lastNode = null;
		mergeHandler = null;
		matches = new ArrayList<IMergeHandler>();

		// insert first segment without changes (assuming somewhat cleaned
		// data!)
		// attention: node counter is not necessarily accurate!
		if (!firstSegmentAdded) {
			int i = 0;
			aggNodes = new ArrayList<AggNode>();
			while (i < segment.size()) {
				GPSPoint pointI = segment.get(i);
				AggNode node = new AggNode(pointI, aggContainer);
				node.setK(pointI.getK());
				node.setRelevant(pointI.isRelevant());
				node.setID("A-" + pointI.getID());
				aggNodes.add(node);
//				addNodeToAgg(aggContainer, node);
//				lastNode = node;
				i++;
			}
			firstSegmentAdded = true;
			return;
		}
		
		int i = 0, bestJ = 0;
		double dist = 0, bestDist = Double.MAX_VALUE;
//		System.out.println(aggContainer.getCachingStrategy().getLoadedConnections());
//		System.out.println(aggNodes);
		//TODO ACHTUNG overlapping + no filter
		ILocation currentNode, projection;
		while(i < segment.size()) {
			// step 1a: get nearest point in agg from trace and create new point if, the projection is not in agg 
			//TODO without filter
			currentNode = segment.get(i);
			for(int j = 0; j < aggNodes.size() - 1; j++) {
				dist = GPSCalc.getDistancePointToEdgeMeter(currentNode, aggNodes.get(j), aggNodes.get(j+1));
				
				if(bestDist > dist) {
					bestDist = dist;
					bestJ = j;
				}
			}
			//dicek apa point terdekat nya itu apa mesti bikin baru
			projection = GPSCalc.getProjectionPoint(currentNode, aggNodes.get(bestJ), aggNodes.get(bestJ+1));
			if(projection != null) {
				AggNode node = new AggNode(projection, aggContainer);
				node.setK(aggNodes.get(bestJ).getK());
				node.setRelevant(aggNodes.get(bestJ).isRelevant());
//				node.setID("A-" + projection.getID());
				node.setID("A-" + i);
				aggNodes.add(bestJ + 1, node);
			}
			bestDist = Double.MAX_VALUE;
			i++;
		}
		
		//Container must be cleared
		i = 0;
		while (i < aggNodes.size()) {			
			addNodeToAgg(aggContainer, aggNodes.get(i));
			lastNode = aggNodes.get(i);
			i++;
		}
//		System.out.println(aggContainer.getCachingStrategy().getNodeCount());
//
//		System.out.println(aggContainer.getCachingStrategy().getLoadedConnections());
//		System.out.println(aggNodes);
//		System.out.println(aggNodes.size());

		i = 0;
		ILocation currentFrom = null;
		ILocation currentTo = null;
		ILocation projectionFrom;
		ILocation projectionTo;
		double distFrom, distTo; 
		double bestDistFrom = Double.MAX_VALUE; 
		double bestDistTo = Double.MAX_VALUE;
		int bestJFrom = 0;
		int bestJTo = 0;
		boolean lastState = false;
		boolean state = false;
		
//		mergeHandler = baseMergeHandler.getCopy();
//		mergeHandler.setAggContainer(aggContainer);
		List<AggNode> sequences = new ArrayList<AggNode>();
		List<GPSPoint> seg = new ArrayList<GPSPoint>();
		
		//TODO angle
		while(i < aggNodes.size() - 1) {
			currentFrom = aggNodes.get(i);
			currentTo = aggNodes.get(i+1);
			
			if(!currentFrom.isRelevant()) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				if(lastState && !state) {
					addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
				}
				i++;
				continue;
			}
			
			for(int j = 0; j < segment.size() - 1; j++) {
				distFrom = GPSCalc.getDistancePointToEdgeMeter(currentFrom, segment.get(j), segment.get(j+1));
				distTo = GPSCalc.getDistancePointToEdgeMeter(currentTo, segment.get(j), segment.get(j+1));
				if(bestDistFrom > distFrom) {
					bestDistFrom = distFrom;
					bestJFrom = j;
				}
				if(bestDistTo > distTo) {
					bestDistTo = distTo;
					bestJTo = j;
				}
			}
			
			if(bestDistFrom > maxInitDistance || bestDistTo > maxInitDistance) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				if(lastState && !state) {
					addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
				}
				i++;
				continue;
			}
			
			//Determine the projection
			projectionFrom = GPSCalc.getProjectionPoint(currentFrom, segment.get(bestJFrom), segment.get(bestJFrom+1));
			if(projectionFrom == null) {
				if(GPSCalc.getDistanceTwoPointsMeter(currentFrom, segment.get(bestJFrom)) < 
						GPSCalc.getDistanceTwoPointsMeter(currentFrom, segment.get(bestJFrom+1)))
					projectionFrom = segment.get(bestJFrom);
				else
					projectionFrom = segment.get(bestJFrom+1);
			}
			
			projectionTo = GPSCalc.getProjectionPoint(currentTo, segment.get(bestJTo), segment.get(bestJTo+1));
			
			if(projectionTo == null) {
				if(GPSCalc.getDistanceTwoPointsMeter(currentTo, segment.get(bestJTo)) < 
						GPSCalc.getDistanceTwoPointsMeter(currentTo, segment.get(bestJTo+1)))
					projectionTo = segment.get(bestJTo);
				else
					projectionTo = segment.get(bestJTo+1);
			}
			
			//TODO Dicek apakah ada lubang
			if(bestJFrom < bestJTo) {
				int j = 0;
				while(bestJFrom + j < bestJTo) {					
					j++;
					if(GPSCalc.getDistancePointToEdgeMeter(segment.get(bestJFrom+j), currentFrom, currentTo) > maxInitDistance) {
						bestDistFrom = Double.MAX_VALUE; 
						bestDistTo = Double.MAX_VALUE;
						state = false;
						if(lastState && !state) {
							addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
							sequences.clear();
							seg.clear();
							lastState = false;
							finishMatch();
							projectionFrom = null;
							projectionTo = null;
							break;
						}
						i++;
					}
				}
			} 
			//Mundur
			else if(bestJFrom > bestJTo) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				if(lastState && !state) {
					addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
				}
				i++;
				continue;
			}
			
			if(projectionFrom != null && projectionTo != null) {
				if(!lastState) {
					state = true;
					mergeHandler = baseMergeHandler.getCopy();
					mergeHandler.setAggContainer(aggContainer);
					lastState = state;
				}
				if(!sequences.contains(aggNodes.get(i)))
					sequences.add(aggNodes.get(i));
				if(!sequences.contains(aggNodes.get(i+1)))
					sequences.add(aggNodes.get(i+1));
				if(!seg.contains(projectionFrom))
					seg.add(new GPSPoint(projectionFrom));
				if(!seg.contains(projectionTo))
					seg.add(new GPSPoint(projectionTo));
//				finishMatch();
			} else {
				if(lastState && !state) {
					addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
				}
			}			
			
			bestDistFrom = Double.MAX_VALUE; 
			bestDistTo = Double.MAX_VALUE;
			i++;
		}
		
		if(mergeHandler != null && sequences.size() > 0 && seg.size() > 0) {
			addToMergeHandler(sequences, seg, Math.max(bestDistFrom, bestDistTo));
			finishMatch();
		}
			
		
		// step 2 and 3 of 3: ghost points, merge everything
		for (IMergeHandler match : matches) {
			if (!match.isEmpty()) {
				System.out.println(match.getAggNodes());
				System.out.println(match.getGpsPoints());
				match.mergePoints();
			}
		}		
	}
	
	private void addToMergeHandler(List<AggNode> sequences,
			List<GPSPoint> segment, double dist) {
		mergeHandler.addAggNodes(sequences);
		mergeHandler.addGPSPoints(segment);
		mergeHandler.setDistance(dist);
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

//	private AggNode getNearestPoints(GPSPoint currentPoint, Set<AggNode> neighbours) {
//		if(neighbours.size() == 0)
//			return null;
//		else if(neighbours.size() == 1)
//			return neighbours.iterator().next();
//		
//		double bestDist = Double.MAX_VALUE;
//		AggNode currentNeighbour, nearestNeighbour = null;
//		Iterator<AggNode> itNeighbours = neighbours.iterator();
//		while(itNeighbours.hasNext()) {
//			currentNeighbour = itNeighbours.next();
//			if(GPSCalc.getDistanceTwoPoints(currentPoint, currentNeighbour) < bestDist && currentNeighbour.isRelevant()) {
//				nearestNeighbour = currentNeighbour;
//				bestDist = GPSCalc.getDistanceTwoPoints(currentPoint, currentNeighbour);
//			}
//		}			
//		return nearestNeighbour;
//	}

}
