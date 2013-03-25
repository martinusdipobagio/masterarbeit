package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.input.Trace;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.Pair;
import de.fub.agg2graph.structs.frechet.TreeAggMap;


public class FrechetMatchDefaultAggregationStrategy extends
		AbstractAggregationStrategy {

	public int maxLookahead = 5;
	public double maxPathDifference = 100;
	public double maxInitDistance = 10;
	
	public List<AggNode> internalAggNodes = new ArrayList<AggNode>();

	
	public FrechetMatchDefaultAggregationStrategy() {
		TraceDistanceFactory.setClass(FreeSpaceMatch.class);
		traceDistance = TraceDistanceFactory.getObject();
		baseMergeHandler = MergeHandlerFactory.getObject();
	}

	@Override
	public void aggregate(GPSSegment segment) {
		// reset all attributes
		lastNode = null;
		mergeHandler = null;
		matches = new ArrayList<IMergeHandler>();
		//Parameter Setting

		
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
				internalAggNodes.add(node);
//				if(i > 0)
//					((FreeSpaceMatch)traceDistance).map.insertConnection(new AggConnection(lastNode, node, aggContainer));
				lastNode = node;
				i++;
			}
			return;
		}
		
		List<List<AggNode>> aggPart = splitAgg(internalAggNodes);
		for(List<AggNode> nextPart : aggPart) {
			((FreeSpaceMatch)traceDistance).map = new TreeAggMap(aggContainer);
			for(int i = 0; i < nextPart.size() - 1;i++) {
				((FreeSpaceMatch)traceDistance).map.insertConnection(new AggConnection(nextPart.get(i), nextPart.get(i+1), aggContainer));
			}
			
			
			((FreeSpaceMatch)traceDistance).trace = new Trace();
			
			for(int i = 0; i < segment.size(); i++) {
				((FreeSpaceMatch)traceDistance).trace.insertEdgeLocation(i, segment.get(i));
			}
			// 1. Find start
			GPSPoint currentPoint = segment.get(0);
			

			((FreeSpaceMatch)traceDistance).start = currentPoint; 
			
			// 2. Expand merge sequence
			@SuppressWarnings("unchecked")
			Pair<List<AggConnection>, List<GPSEdge>>[] result = (Pair<List<AggConnection>, List<GPSEdge>>[]) 
					traceDistance.getPathDifference(null, null, 0, mergeHandler);

			System.out.println("RESULT : " + result.length);
			for(Pair<List<AggConnection>, List<GPSEdge>> r : result) {
				if(mergeHandler == null) {
					mergeHandler = baseMergeHandler.getCopy();
					mergeHandler.setAggContainer(aggContainer);
				}
				
				List<AggNode> first = new ArrayList<AggNode>();
				List<GPSPoint> second = new ArrayList<GPSPoint>();
				
//				System.out.println(r.first().size());
//				System.out.println(r.second().size());
				
				for(AggConnection f : r.first()) {
					first.add(f.getFrom());
				}
				first.add(r.first().get(r.first().size() - 1).getTo());
				
				for(GPSEdge s : r.second()) {
					second.add(s.getFrom());
				}
				second.add(r.second().get(r.second().size() - 1).getTo());
				
//				System.out.println(first.size());
//				System.out.println(second.size());
				
				addToMergeHandler(first, second, 10);
				finishMatch();
			}
		}
		
		
		// 3. Merge
		for (IMergeHandler match : matches) {
			if (!match.isEmpty()) {
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

	protected List<List<AggNode>> splitAgg(List<AggNode> internalAggNodes) {
		List<List<AggNode>> ret = new ArrayList<List<AggNode>>();
		List<AggNode> temp = new ArrayList<AggNode>();
		for(int i = 0; i < internalAggNodes.size(); i++) {
			if(!internalAggNodes.get(i).isRelevant()) {
				if(temp.size() > 0) {
					temp.add(internalAggNodes.get(i));
					List<AggNode> seq = new ArrayList<AggNode>(temp);
					ret.add(seq);
				}
				temp.clear();
			} else {
				temp.add(internalAggNodes.get(i));
			}
		}
		if(temp.size() > 1)
			ret.add(temp);
		
		return ret;
	}
	
}
