package de.fub.agg2graph.input;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class PointPartitioner {
	
	public static List<GPSSegment> read(File file) {
		List<GPSSegment> segment = GPXReader.getSegments(file);
		
		return segment;
	}
	
	public static void write(File file, List<GPSSegment> segment, int part) throws IOException {
		double step = 1.0/part;
		for(int i = 0 ; i < segment.get(0).size() - 1; i++) {
			GPSPoint p1 = segment.get(0).get(i);
			GPSPoint p2 = segment.get(0).get(i+1);
			for(int j = 1; j < part; j++) {
				GPSPoint newPoint = new GPSPoint(GPSCalc.getPointAt(step*j, p1, p2));
				segment.get(0).add(i+j, newPoint);
			}
			i = i + part - 1;
		}
		GPXWriter.writeSegment(file, segment.get(0));
	}
	
	public static void main(String[] args) throws IOException {
		String name = "map135.gpx";
		String directory = "01-Tester/";
		File f = new File("test/input/"+directory+name);
		
		List<GPSSegment> segment = read(f);
		write(f, segment, 2);
		
	}
}
