package de.fub.agg2graph.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class AggregationStatistic {
	
	/**
	 * Compute the number of new points after executing the Aggregation evaluation
	 * @param list all segments
	 * @param from start number of new segments
	 * @return
	 */
	public static int numberOfNewPoints(List<GPSSegment> list, int from) {
		int sum = 0;
		//If no new segments
		if(from > list.size() - 1) {
			return 0;
		}
		for(int i = from; i < list.size(); i++) {
			GPSSegment currentSegment = list.get(i);
			sum += currentSegment.size();
		}
		return sum;
	}
	
	/**
	 * Compute the number of matched points after executing aggregation.
	 * @param list all segments
	 * @param numberSegment number of original segments
	 * @param start initial value of k
	 * @return
	 */
	public static int numberOfPointMatched(List<GPSSegment> list, int numberSegment, int start) {
		int sum = 0;
		//Original tracks
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				if(p.getK() > start)
					sum += p.getK() - start;
			}
		}
		//New tracks
		for(int i = numberSegment; i < list.size(); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				sum += p.getK() - 1;
			}
		}
		
		return sum;
	}
	
	/**
	 * Compute the number of points from original tracks, which are never matched.
	 * @param list all segments
	 * @param numberSegment number of original segments
	 * @param start initial value of k
	 * @return
	 */
	public static int portionOfUnmatched(List<GPSSegment> list, int numberSegment, int start) {
		int sum = 0;
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				if(p.getK() == start)
					sum += 1;
			}
		}
		return sum;
	}
	
	/**
	 * Compute the number of points from original tracks, which are matched at least once.
	 * @param list all segments
	 * @param numberSegment number of original segments
	 * @param start initial value of k
	 * @return
	 */
	public static int portionOfMatched(List<GPSSegment> list, int numberSegment, int start) {
		int sum = 0;
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			sum += currentSegment.size();
		}
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				if(p.getK() == start)
					sum -= 1;
			}
		}
		return sum;
	}
	
	/**
	 * Compute the length of original tracks in meter
	 * @param list 
	 * @param numberSegment
	 * @return
	 */
	public static double lengthOriginalTrack(List<GPSSegment> list, int numberSegment) {
		double sum = 0;
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			sum += GPSCalc.traceLengthMeter(currentSegment);
		}
		
		return sum;
	}
	
	public static double lengthNewTrack(List<GPSSegment> list, int start) {
		double sum = 0;
		//If no new segments
		if(start > list.size() - 1) {
			return 0;
		}
		
		for(int i = start; i < list.size(); i++) {
			GPSSegment currentSegment = list.get(i);
			sum += GPSCalc.traceLengthMeter(currentSegment);
		}
		
		return sum;
	}
	
	public static int originalPoints(List<GPSSegment> list, int numberSegment) {
		int sum = 0;
		
		for(GPSSegment currentSegment : list) {
			sum += currentSegment.size();
		}
		
		return sum;
	}
	
	public static void main(String[] args) throws IOException {
		List<GPSSegment> normal = GPXReader.getSegments(new File("test/input/01-tester/map0.gpx"));
		List<GPSSegment> pathDef = GPXReader.getSegments(new File("test/input/01-tester/map120PathDefault.gpx"));
		List<GPSSegment> hausdorffAtt = GPXReader.getSegments(new File("test/input/01-tester/map120HausdorffAttraction.gpx"));
		List<GPSSegment> gpxIte = GPXReader.getSegments(new File("test/input/01-tester/map120GPXIterative.gpx"));
		List<GPSSegment> secondAtt = GPXReader.getSegments(new File("test/input/01-tester/map120SecondAttraction.gpx"));
		String output = "test/input/01-tester/output.txt";
		
		String pathDefault = originalPoints(normal, 45) + "\t"
				+ portionOfUnmatched(pathDef, 45, 4) + "\t"
				+ portionOfMatched(pathDef, 45, 4) + "\t"
				+ numberOfNewPoints(pathDef, 45) + "\t"
				+ numberOfPointMatched(pathDef, 45, 4) + "\t"
				+ lengthOriginalTrack(normal, 45) + "\t"
				+ lengthOriginalTrack(pathDef, 45) + "\t"
				+ lengthNewTrack(pathDef, 45);
		
		String hausdorffAttraction = originalPoints(normal, 45) + "\t"
				+ portionOfUnmatched(hausdorffAtt, 45, 4) + "\t"
				+ portionOfMatched(hausdorffAtt, 45, 4) + "\t"
				+ numberOfNewPoints(hausdorffAtt, 45) + "\t"
				+ numberOfPointMatched(hausdorffAtt, 45, 4) + "\t"
				+ lengthOriginalTrack(normal, 45) + "\t"
				+ lengthOriginalTrack(hausdorffAtt, 45) + "\t"
				+ lengthNewTrack(hausdorffAtt, 45);
		
		String kantenIterative = originalPoints(normal, 45) + "\t"
				+ portionOfUnmatched(gpxIte, 45, 4) + "\t"
				+ portionOfMatched(gpxIte, 45, 4) + "\t"
				+ numberOfNewPoints(gpxIte, 45) + "\t"
				+ numberOfPointMatched(gpxIte, 45, 4) + "\t"
				+ lengthOriginalTrack(normal, 45) + "\t"
				+ lengthOriginalTrack(gpxIte, 45) + "\t"
				+ lengthNewTrack(gpxIte, 45);
		
		String secondAttraction = originalPoints(normal, 45) + "\t"
				+ portionOfUnmatched(secondAtt, 45, 4) + "\t"
				+ portionOfMatched(secondAtt, 45, 4) + "\t"
				+ numberOfNewPoints(secondAtt, 45) + "\t"
				+ numberOfPointMatched(secondAtt, 45, 4) + "\t"
				+ lengthOriginalTrack(normal, 45) + "\t"
				+ lengthOriginalTrack(secondAtt, 45) + "\t"
				+ lengthNewTrack(secondAtt, 45);
		
//		System.out.println(pathDefault);
//		System.out.println(hausdorffAttraction);
//		System.out.println(kantenIterative);
//		System.out.println(secondAttraction);
		
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
