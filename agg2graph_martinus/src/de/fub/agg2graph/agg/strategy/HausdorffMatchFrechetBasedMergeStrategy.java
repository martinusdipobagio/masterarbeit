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

public class HausdorffMatchFrechetBasedMergeStrategy extends AbstractAggregationStrategy {
//	private static final Logger logger = Logger
//			.getLogger("agg2graph.agg.hausdorff.strategy");

	public int maxLookahead = 7;
	public double maxPathDifference = 1000;
	public double maxInitDistance = 150;
	public boolean firstSegmentAdded = false;
	
	private List<AggNode> aggNodes;
	
	public enum State {
		NO_MATCH, IN_MATCH
	}
		
	public HausdorffMatchFrechetBasedMergeStrategy() {
		TraceDistanceFactory.setClass(HausdorffTraceDistance.class);
		traceDistance = TraceDistanceFactory.getObject();
		MergeHandlerFactory.setClass(FrechetBasedMerge.class);
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	/**
	 * TODO send something to HausdorffTraceDistance
	 */
	@Override
	public void aggregate(GPSSegment segment) {
		// reset all attributes
		lastNode = null;
		mergeHandler = null;
		matches = new ArrayList<IMergeHandler>();

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
				i++;
			}
			firstSegmentAdded = true;
			return;
		}
		
		int i = 0;
		int bestJ = 0;
		double dist = 0;
		double bestDist = Double.MAX_VALUE;
		
		//TODO ACHTUNG overlapping + no filter
		ILocation currentNode, projection;
		while(i < segment.size()) {
			// step 1a: get nearest point in agg from trace and create new point if, the projection is not in agg
			//          maxInitDistance is ignored in this phase. Relevancy is calculated though
			currentNode = segment.get(i);
			for(int j = 0; j < aggNodes.size() - 1; j++) {				
				if(!aggNodes.get(j).isRelevant())
					continue;
				
				dist = GPSCalc.getDistancePointToEdgeMeter(currentNode, aggNodes.get(j), aggNodes.get(j+1));
				
				if(bestDist > dist) {
					bestDist = dist;
					bestJ = j;
				}
			}

			// If the projection is not in data, then create new node
			projection = GPSCalc.getProjectionPoint(currentNode, aggNodes.get(bestJ), aggNodes.get(bestJ+1));
			if(projection != null) {
				AggNode node = new AggNode(projection, aggContainer);
				node.setK(aggNodes.get(bestJ).getK());
				node.setRelevant(aggNodes.get(bestJ).isRelevant());
				node.setID("A-" + i);
				aggNodes.add(bestJ + 1, node);
			}
			bestDist = Double.MAX_VALUE;
			i++;
		}
		
		//Agg is now added to the container
		i = 0;
		while (i < aggNodes.size()) {			
			addNodeToAgg(aggContainer, aggNodes.get(i));
			lastNode = aggNodes.get(i);
			i++;
		}
		
//		System.out.println(aggNodes);
		
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
		
		List<AggNode> sequences = new ArrayList<AggNode>();
		List<GPSPoint> seg = new ArrayList<GPSPoint>();
		
		//step 1b: get nearest point in agg from trace and create new point if, the projection is not in agg
		//         maxInitDistance is ignored in this phase. Unlike 1a, this step works with edges
		while(i < aggNodes.size() - 1) {
			currentFrom = aggNodes.get(i);
			currentTo = aggNodes.get(i+1);
			
			//Relevancy check
			if(!currentFrom.isRelevant()) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				//If lastState true, then finish the match, process the submatch and reset the values
				if(lastState && !state) {
					addToMergeHandler(new ArrayList<AggNode>(sequences), new ArrayList<GPSPoint>(seg), Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
					projectionFrom = null;
					projectionTo = null;
				}
				i++;
				continue;
			}
			
			//get the nearest points
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
			
			//if bestDistance > maxDistance
			if(bestDistFrom > maxInitDistance || bestDistTo > maxInitDistance) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				//If lastState true, then finish the match, process the submatch and reset the values
				if(lastState && !state) {
					addToMergeHandler(new ArrayList<AggNode>(sequences), new ArrayList<GPSPoint>(seg), Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
					projectionFrom = null;
					projectionTo = null;
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
			
			//Sombrero problem
			if(bestJFrom < bestJTo) {
				int j = 0;
				while(bestJFrom + j < bestJTo) {					
					j++;
					if(GPSCalc.getDistancePointToEdgeMeter(segment.get(bestJFrom+j), currentFrom, currentTo) > maxInitDistance) {
						bestDistFrom = Double.MAX_VALUE; 
						bestDistTo = Double.MAX_VALUE;
						state = false;
						//If lastState true, then finish the match, process the submatch and reset the values
						if(lastState && !state) {
							addToMergeHandler(new ArrayList<AggNode>(sequences), new ArrayList<GPSPoint>(seg), Math.max(bestDistFrom, bestDistTo));
							sequences.clear();
							seg.clear();
							lastState = false;
							finishMatch();
							projectionFrom = null;
							projectionTo = null;
							break;
						}
//						i++;
					}
				}
			}
			
			//backwards are not allowed
			else if(bestJFrom > bestJTo) {
				bestDistFrom = Double.MAX_VALUE; 
				bestDistTo = Double.MAX_VALUE;
				state = false;
				if(lastState && !state) {
					addToMergeHandler(new ArrayList<AggNode>(sequences), new ArrayList<GPSPoint>(seg), Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
					projectionFrom = null;
					projectionTo = null;
				}
				i++;
				continue;
			}
			
			if(projectionFrom != null && projectionTo != null) {
				if(!lastState) {
					state = true;
					mergeHandler = baseMergeHandler.getCopy();
					mergeHandler.setAggContainer(aggContainer);
					lastState = true;
				}
				if(!sequences.contains(aggNodes.get(i)))
					sequences.add(aggNodes.get(i));
				if(!sequences.contains(aggNodes.get(i+1)))
					sequences.add(aggNodes.get(i+1));
				if(!seg.contains(projectionFrom))
					seg.add(new GPSPoint(projectionFrom));
				if(!seg.contains(projectionTo))
					seg.add(new GPSPoint(projectionTo));
			} else {
				if(lastState && !state) {
					addToMergeHandler(new ArrayList<AggNode>(sequences), new ArrayList<GPSPoint>(seg), Math.max(bestDistFrom, bestDistTo));
					sequences.clear();
					seg.clear();
					lastState = false;
					finishMatch();
					projectionFrom = null;
					projectionTo = null;
				}
			}			
			
			bestDistFrom = Double.MAX_VALUE; 
			bestDistTo = Double.MAX_VALUE;
			i++;
		}
		
		if(mergeHandler != null && sequences.size() > 0 && seg.size() > 0) {
			addToMergeHandler(new ArrayList<AggNode>(sequences), seg, Math.max(bestDistFrom, bestDistTo));
			finishMatch();
		}
		
		// step 2 and 3 of 3: ghost points, merge everything
		System.out.println("MATCHES : " + matches.size());
		for (IMergeHandler match : matches) {
			if (!match.isEmpty()) {
				match.mergePoints();
			}
		}
		firstSegmentAdded = false;
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
}
