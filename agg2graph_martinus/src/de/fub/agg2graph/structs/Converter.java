package de.fub.agg2graph.structs;

import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;

public class Converter {

	/**Extract locations our merge is base on these. */
	public static List<AggNode> toLocationSequence(List<GPSEdge> edgeSequence, AggContainer aggContainer) {
		List<AggNode> result;
		result = new ArrayList<AggNode>();
		for(GPSEdge a : edgeSequence) {
			result.add(new AggNode(a.getFrom(), aggContainer));
		}
		result.add(new AggNode(edgeSequence.get(edgeSequence.size() - 1).getTo(), aggContainer));
		
		return result;
	}

}
