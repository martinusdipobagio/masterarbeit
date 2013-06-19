package de.fub.agg2graph.input;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class ChangeDirector {
	
	public static List<GPSSegment> read(File file) {
		List<GPSSegment> segment = GPXReader.getSegments(file);
		
		return segment;
	}
	
	public static void write(File file, List<GPSSegment> segment) throws IOException {
		GPSSegment reverse = new GPSSegment();
		for(int i = segment.get(0).size()-1; i >= 0; i--) {
			reverse.add(segment.get(0).get(i));
		}
		
		GPXWriter.writeSegment(file, reverse);
	}
	
	public static void main(String[] args) throws IOException {
		String name = "m9.gpx";
		String directory = "01-Testefr/";
		File f = new File("test/input/"+directory+name);
		
		List<GPSSegment> segment = read(f);
		write(f, segment);
		
	}
}
