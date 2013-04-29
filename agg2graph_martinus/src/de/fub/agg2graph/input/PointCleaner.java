package de.fub.agg2graph.input;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class PointCleaner {
	
	public static List<GPSSegment> read(File file) {
		List<GPSSegment> segment = GPXReader.getSegments(file);
		
		return segment;
	}
	
	public static void write(File file, List<GPSSegment> segment) throws IOException {
		for(int i = 1 ; i < segment.get(0).size(); i++) {
			segment.get(0).remove(i);
		}
		GPXWriter.writeSegment(file, segment.get(0));
	}
	
	public static void main(String[] args) throws IOException {
		String name = "tempelhof6.gpx";
		String directory = "Scen8 - Tempelhof/";
		File f = new File("test/input/"+directory+name);
		
		List<GPSSegment> segment = read(f);
		write(f, segment);
		
	}
}
