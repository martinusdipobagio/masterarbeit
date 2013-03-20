package de.fub.agg2graph.input;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class GPSFilter {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger("agg2graph.filter");
	
	private FilterOptions fo = new FilterOptions();
	
	public List<GPSSegment> relevantPart = new ArrayList<GPSSegment>();
	public List<GPSSegment> irrelevantPart = new ArrayList<GPSSegment>();
	
	public GPSFilter enableDefault() {
		fo.kRequirement = 4;
		
		return this;
	}
	
	public GPSSegment filter(GPSSegment segment) {
		List<GPSPoint> pointList = segment;
		for(GPSPoint point : pointList) {
			if(point.getK() < fo.kRequirement)
				point.setRelevant(false);
			else
				point.setRelevant(true);
		}
		return (GPSSegment) pointList;
	}
	
	public FilterOptions getFilterOptions() {
		return this.fo;
	}
}
