package de.fub.agg2graph.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class KStatistic {
	
	/**
	 * Compute the number of matched points after executing aggregation.
	 * @param list all segments
	 * @param numberSegment number of original segments
	 * @param start initial value of k
	 * @return
	 */
	public static SortedMap<Integer, Integer> frequenceK(List<GPSSegment> list) {
		SortedMap<Integer, Integer> ret = new TreeMap<Integer, Integer>();
		for(int i = 0; i < list.size(); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				if(!ret.containsKey(p.getK())) {
					ret.put(p.getK(), 1);
				} else {
					int newValue = ret.get(p.getK()) + 1;
					ret.put(p.getK(), newValue);
				}
			}
		}
		
		return ret;
	}
	
	public static String toStrings(String aggregation, SortedMap<Integer, Integer> data) {
		String ret = "----- " + aggregation + " -----\n";
		for(Integer i : data.keySet()) {
			ret += i + "\t" + data.get(i) + "\n";
		}
		
		return ret;
	}
	
	public static void main(String[] args) throws IOException {
//		List<GPSSegment> normal = GPXReader.getSegments(new File("test/input/01-tester/map0.gpx"));
		List<GPSSegment> pathDef = GPXReader.getSegments(new File("test/input/01-tester/map120PathDefault.gpx"));
		List<GPSSegment> hausdorffAtt = GPXReader.getSegments(new File("test/input/01-tester/map120HausdorffAttraction.gpx"));
		List<GPSSegment> gpxIte = GPXReader.getSegments(new File("test/input/01-tester/map120GPXIterative.gpx"));
		List<GPSSegment> secondAtt = GPXReader.getSegments(new File("test/input/01-tester/map120SecondAttraction.gpx"));
		String output = "test/input/01-tester/outputK.txt";
				
		String pathDefault = toStrings("Path-Default", frequenceK(pathDef));
		
		String hausdorffAttraction = toStrings("Hausdorff-Attraction", frequenceK(hausdorffAtt));
		
		String kantenIterative = toStrings("Kanten-Iterative", frequenceK(gpxIte));
		
		String secondAttraction = toStrings("Second-Attraction", frequenceK(secondAtt));
	
		
		FileWriter fstream = new FileWriter(output, true);
		BufferedWriter fbw = new BufferedWriter(fstream);
		fbw.write(pathDefault);
		fbw.newLine();
		fbw.write(hausdorffAttraction);
		fbw.newLine();
		fbw.write(kantenIterative);
		fbw.newLine();
		fbw.write(secondAttraction);
		fbw.close();
	}
}
