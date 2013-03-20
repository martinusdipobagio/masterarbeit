package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.TraceDistanceFactory;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class HausdorffTrackToTrackAggregationStrategy extends
		AbstractAggregationStrategy {

	public int maxLookahead = 7;
	public double maxPathDifference = 1000;
	public double maxInitDistance = 150;

	public HausdorffTrackToTrackAggregationStrategy() {
		TraceDistanceFactory.setClass(HausdorffTrackToTrackTraceDistance.class);
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

		// List<AggNode> sequences = new ArrayList<AggNode>();
		// AggNode headSequence = null, currentNode;
		double dist = 0, maxDist = 0;
		int i = 0, iStart = 0;
		GPSPoint firstPoint;
		GPSPoint secondPoint;
		Set<AggConnection> nearEdges = null;
		AggConnection bestConn = null, head = null;
		List<AggConnection> sequences = new ArrayList<AggConnection>();

		while (i < segment.size() - 1) {
			firstPoint = segment.get(i);
			secondPoint = segment.get(i + 1);
			GPSEdge currentEdge = new GPSEdge(firstPoint, secondPoint);
			nearEdges = aggContainer.getCachingStrategy().getCloseConnections(
					currentEdge, maxInitDistance);
			// logger.warning(nearEdges.size() + " near edges");

			//No near edge found
			if (nearEdges.size() < 1) {
				//Aggregate if sequence is not empty
				if (sequences.size() > 0) {
					System.out.println("Broken");
					for (AggConnection seq : sequences)
						mergeHandler.addAggNodes(seq);
					mergeHandler.addGPSPoints(segment.subList(iStart, i + 1));
					mergeHandler.setDistance(maxDist);
					sequences.clear();
					maxDist = 0;
					head = null;

					if (!mergeHandler.isEmpty()) {
						matches.add(mergeHandler);

						mergeHandler.processSubmatch();
						// connect to previous node
						aggContainer
								.connect(lastNode, mergeHandler.getInNode());
						mergeHandler.setBeforeNode(lastNode);
						// remember outgoing node (for later connection)
						lastNode = mergeHandler.getOutNode();
						// mergeHandler = null;
					}
				}
				else
					System.out.println("No found");
				i++;
				continue;
			} 
			//New Sequence
			else if (sequences.size() < 1) {
				System.out.println("New Sequence");
				mergeHandler = baseMergeHandler.getCopy();
				mergeHandler.setAggContainer(aggContainer);

				Iterator<AggConnection> itNear = nearEdges.iterator();
				Double grade = Double.MAX_VALUE;
				while (itNear.hasNext()) {
					AggConnection near = itNear.next();
					Object[] distReturn = traceDistance.getPathDifference(
							near.toPointList(), currentEdge.toPointList(), 0,
							mergeHandler);
					dist = (Double) distReturn[0];
					if (dist < grade && dist < maxPathDifference) {
						grade = dist;
						bestConn = near;
					}
				}
				head = bestConn;
				maxDist = dist;
				sequences.add(bestConn);
				iStart = i;
				i++;
			}
			else {
				Iterator<AggConnection> itNear = nearEdges.iterator();
				Double grade = Double.MAX_VALUE;
				while (itNear.hasNext()) {
					AggConnection near = itNear.next();
					Object[] distReturn = traceDistance.getPathDifference(
							near.toPointList(), currentEdge.toPointList(), 0,
							mergeHandler);
					dist = (Double) distReturn[0];
					if (dist < grade && dist < maxPathDifference) {
						grade = dist;
						bestConn = near;
					}
				}
				//Continue
				if (head.getTo().equals(bestConn.getFrom())) {
					System.out.println("Continue");
					head = bestConn;
					if (maxDist < dist)
						maxDist = dist;
					sequences.add(bestConn);

				} else if(head.getFrom().equals(bestConn.getFrom()) && head.getTo().equals(bestConn.getTo())) {
					System.out.println("Same");
					if (maxDist < dist)
						maxDist = dist;
				}
				//Broken and New Sequence
				else {
					System.out.println("Broken and New sequence");
					for (AggConnection seq : sequences)
						mergeHandler.addAggNodes(seq);
					mergeHandler.addGPSPoints(segment.subList(iStart, i + 1));
					mergeHandler.setDistance(maxDist);
					sequences.clear();
					maxDist = 0;
					head = null;

					if (!mergeHandler.isEmpty()) {
						matches.add(mergeHandler);

						mergeHandler.processSubmatch();
						// connect to previous node
						aggContainer
								.connect(lastNode, mergeHandler.getInNode());
						mergeHandler.setBeforeNode(lastNode);
						// remember outgoing node (for later connection)
						lastNode = mergeHandler.getOutNode();
						// mergeHandler = null;
					}

					mergeHandler = baseMergeHandler.getCopy();
					mergeHandler.setAggContainer(aggContainer);

					head = bestConn;
					maxDist = dist;
					sequences.add(bestConn);
					iStart = i;
				}
				i++;
			}
		}
		
		if (sequences.size() > 0) {
			for (AggConnection seq : sequences)
				mergeHandler.addAggNodes(seq);
			mergeHandler.addGPSPoints(segment.subList(iStart, i + 1));
			mergeHandler.setDistance(maxDist);
			sequences.clear();
			maxDist = 0;
			head = null;

			if (!mergeHandler.isEmpty()) {
				matches.add(mergeHandler);

				mergeHandler.processSubmatch();
				// connect to previous node
				aggContainer
						.connect(lastNode, mergeHandler.getInNode());
				mergeHandler.setBeforeNode(lastNode);
				// remember outgoing node (for later connection)
				lastNode = mergeHandler.getOutNode();
				// mergeHandler = null;
			}
		}
		
		// step 2 and 3 of 3: ghost points, merge everything
		for (IMergeHandler match : matches) {
			if (!match.isEmpty()) {
				match.mergePoints();
			}
		}
	}

}
