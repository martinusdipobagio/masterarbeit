package de.fub.agg2graph.structs;

import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;

public class Converter {

	/**Extract locations our merge is base on these. */
	public static List<AggNode> toLocationSequence(List<? extends AggConnection> edgeSequence) {
		List<AggNode> result;
		result = new ArrayList<AggNode>();
		for(AggConnection a : edgeSequence) {
			result.add(a.getFrom());
		}
		result.add(edgeSequence.get(edgeSequence.size() - 1).getTo());
		
		return result;
	}

}
