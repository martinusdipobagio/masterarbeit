package de.fub.agg2graph.input;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.structs.GPSSegment;


public class SerializeAgg {

//	public static List<GPSSegment> myResult = new ArrayList<GPSSegment>();
	
	public static GPSSegment getSegmentFromLastNode(AggNode last) {

		GPSSegment result = new GPSSegment();
		result.add(last);
		while(!last.getIn().isEmpty()) {
			last = last.getIn().iterator().next().getFrom();
			result.add(0, last);
		}
		return result;
	}
	
	public static List<GPSSegment> getSerialize(AggNode root) {
		List<GPSSegment> result = new ArrayList<GPSSegment>();
		if(root != null) {
			GPSSegment trace = new GPSSegment();
			result.add(trace);
			trace.add(root);
			
			if(root.getIn().size() == 1)
				previousBranch(result, trace, root.getIn().iterator().next().getFrom(), root);
			else if(root.getIn().size() > 1) {
				for(AggConnection conn : root.getIn()) {
					GPSSegment branchTrace = new GPSSegment();
					result.add(branchTrace);
					branchTrace.add(conn.getTo());
					previousBranch(result, branchTrace, conn.getFrom(), root);
				}

			}
			
			if(root.getOut().size() == 1)
				continueBranch(result, trace, root.getOut().iterator().next().getTo(), root);
			else if(root.getOut().size() > 1) {
				for(AggConnection conn : root.getOut()) {
					GPSSegment branchTrace = new GPSSegment();
					result.add(branchTrace);
					branchTrace.add(conn.getFrom());
					continueBranch(result, branchTrace, conn.getTo(), root);
				}

			}
		}
		
		return result;
	}
	
	public static void previousBranch(List<GPSSegment> tree, GPSSegment trace, AggNode node, AggNode source) {
		trace.add(0, node);
//		ArrayList<AggNode> out = null;
		if(node.getIn().size() == 1) {
			previousBranch(tree, trace, node.getIn().iterator().next().getFrom(), node);
			for(AggConnection connTo : node.getOut()) {
				if(!connTo.getTo().equals(source)) {
					GPSSegment branchTrace = new GPSSegment();
					tree.add(branchTrace);
					branchTrace.add(connTo.getFrom());
					continueBranch(tree, branchTrace, connTo.getTo(), connTo.getFrom());
				}
			}
//			out = new ArrayList<AggNode>(node.getOut().iterator().next().getTo());
		}
			
		else if(node.getIn().size() > 1) {
			for(AggConnection conn : node.getIn()) {
				GPSSegment branchTrace = new GPSSegment();
				tree.add(branchTrace);
				branchTrace.add(conn.getTo());
				previousBranch(tree, branchTrace, conn.getFrom(), node);
				for(AggConnection connTo : node.getOut()) {
					if(!connTo.getTo().equals(source)) {
						GPSSegment branchTraceTo = new GPSSegment();
						tree.add(branchTraceTo);
						branchTraceTo.add(connTo.getFrom());
						continueBranch(tree, branchTrace, connTo.getTo(), connTo.getFrom());
					}
				}
			}
		}
	}
	
	public static void continueBranch(List<GPSSegment> tree, GPSSegment trace, AggNode node, AggNode source) {
		trace.add(node);
//		ArrayList<AggNode> out = null;
		if(node.getOut().size() == 1) {
			continueBranch(tree, trace, node.getOut().iterator().next().getTo(), node);
			for(AggConnection connFrom : node.getIn()) {
				if(!connFrom.getFrom().equals(source)) {
					GPSSegment branchTraceFrom = new GPSSegment();
					tree.add(branchTraceFrom);
					branchTraceFrom.add(connFrom.getTo());
					previousBranch(tree, branchTraceFrom, connFrom.getFrom(), connFrom.getTo());
				}
			}
//			out = new ArrayList<AggNode>(node.getOut().iterator().next().getTo());
		}
			
		else if(node.getOut().size() > 1) {
			for(AggConnection conn : node.getOut()) {
				GPSSegment branchTrace = new GPSSegment();
				tree.add(branchTrace);
				branchTrace.add(conn.getFrom());
				continueBranch(tree, branchTrace, conn.getTo(), node);
				for(AggConnection connFrom : node.getIn()) {
					if(!connFrom.getFrom().equals(source)) {
						GPSSegment branchTraceFrom = new GPSSegment();
						tree.add(branchTraceFrom);
						branchTraceFrom.add(connFrom.getTo());
						previousBranch(tree, branchTraceFrom, connFrom.getFrom(), connFrom.getTo());
					}
				}
			}
		}
	}
}
