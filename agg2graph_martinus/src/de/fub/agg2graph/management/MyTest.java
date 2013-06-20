package de.fub.agg2graph.management;

import java.io.File;
import java.util.List;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class MyTest {
	
	public static int numberOfNewPoints(List<GPSSegment> list, int from) {
		int sum = 0;
		if(from > list.size() - 1) {
			return 0;
		}
		for(int i = from; i < list.size(); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
					sum += 1;
			}
		}
		return sum;
	}
	
	public static int numberOfPointMatched(List<GPSSegment> list, int numberSegment, int without) {
		int sum = 0;
		for(int i = 0; i < Math.min(numberSegment, list.size()); i++) {
			GPSSegment currentSegment = list.get(i);
			for(GPSPoint p : currentSegment) {
				if(p.getK() > without)
					sum += p.getK() - without;
			}
		}
		return sum;
	}
	
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
	
	public static void main(String[] args) {
		List<GPSSegment> normal = GPXReader.getSegments(new File("test/input/01-tester/map0.gpx"));
		List<GPSSegment> frechetAtt = GPXReader.getSegments(new File("test/input/01-tester/map120FrechetAttraction.gpx"));
		List<GPSSegment> frechetIte = GPXReader.getSegments(new File("test/input/01-tester/map120FrechetIterative.gpx"));
		List<GPSSegment> defAtt = GPXReader.getSegments(new File("test/input/01-tester/map120DefaultAttraction.gpx"));
		List<GPSSegment> defIte = GPXReader.getSegments(new File("test/input/01-tester/map120DefaultIterative.gpx"));
		List<GPSSegment> hausdorffAtt = GPXReader.getSegments(new File("test/input/01-tester/map120HausdorffAttraction.gpx"));
		List<GPSSegment> hausdorffIte = GPXReader.getSegments(new File("test/input/01-tester/map120HausdorffIterative.gpx"));
		List<GPSSegment> gpxAtt = GPXReader.getSegments(new File("test/input/01-tester/map120GPXAttraction.gpx"));
		List<GPSSegment> gpxIte = GPXReader.getSegments(new File("test/input/01-tester/map120GPXIterative.gpx"));
		List<GPSSegment> secondAtt = GPXReader.getSegments(new File("test/input/01-tester/map120SecondAttraction.gpx"));
		List<GPSSegment> secondIte = GPXReader.getSegments(new File("test/input/01-tester/map120SecondIterative.gpx"));
		
		System.out.println("# of Matched Attraction");
		System.out.println("Normal  : " + numberOfPointMatched(normal, 45, 4));
		System.out.println("Default : " + numberOfPointMatched(defAtt, 45,  4));
		System.out.println("GPX     : " + numberOfPointMatched(gpxAtt, 45,  4));
		System.out.println("Hausd   : " + numberOfPointMatched(hausdorffAtt, 45,  4));
		System.out.println("Frechet : " + numberOfPointMatched(frechetAtt, 45, 4));
		System.out.println("Second  : " + numberOfPointMatched(secondAtt, 45,  4));
		System.out.println("# of Matched Iterative");
		System.out.println("Normal  : " + numberOfPointMatched(normal, 45, 4));
		System.out.println("Default : " + numberOfPointMatched(defIte, 45,  4));
		System.out.println("GPX     : " + numberOfPointMatched(gpxIte, 45,  4));
		System.out.println("Hausd   : " + numberOfPointMatched(hausdorffIte, 45,  4));
		System.out.println("Frechet : " + numberOfPointMatched(frechetIte, 45, 4));
		System.out.println("Second  : " + numberOfPointMatched(secondIte, 45,  4));
		
		
		System.out.println();
		System.out.println();
		System.out.println("# of New Points Attraction");
		System.out.println("Normal  : " + numberOfNewPoints(normal, 46));
		System.out.println("Default : " + numberOfNewPoints(defAtt, 46));
		System.out.println("GPX     : " + numberOfNewPoints(gpxAtt, 46));
		System.out.println("Hausd   : " + numberOfNewPoints(hausdorffAtt, 46));
		System.out.println("Frechet : " + numberOfNewPoints(frechetAtt, 46));
		System.out.println("Second  : " + numberOfNewPoints(secondAtt, 46));
		System.out.println("# of New Points Iterative");
		System.out.println("Normal  : " + numberOfNewPoints(normal, 46));
		System.out.println("Default : " + numberOfNewPoints(defIte, 46));
		System.out.println("GPX     : " + numberOfNewPoints(gpxIte, 46));
		System.out.println("Hausd   : " + numberOfNewPoints(hausdorffIte, 46));
		System.out.println("Frechet : " + numberOfNewPoints(frechetIte, 46));
		System.out.println("Second  : " + numberOfNewPoints(secondIte, 46));
		
		
		System.out.println();
		System.out.println();
		System.out.println("Portion of Unmatched Attraction");
		System.out.println("Normal  : " + portionOfUnmatched(normal, 45, 4));
		System.out.println("Default : " + portionOfUnmatched(defAtt, 45,  4));
		System.out.println("GPX     : " + portionOfUnmatched(gpxAtt, 45,  4));
		System.out.println("Hausd   : " + portionOfUnmatched(hausdorffAtt, 45,  4));
		System.out.println("Frechet : " + portionOfUnmatched(frechetAtt, 45, 4));
		System.out.println("Second  : " + portionOfUnmatched(secondAtt, 45,  4));
		System.out.println("Portion of Unmatched Iterative");
		System.out.println("Normal  : " + portionOfUnmatched(normal, 45, 4));
		System.out.println("Default : " + portionOfUnmatched(defIte, 45,  4));
		System.out.println("GPX     : " + portionOfUnmatched(gpxIte, 45,  4));
		System.out.println("Hausd   : " + portionOfUnmatched(hausdorffIte, 45,  4));
		System.out.println("Frechet : " + portionOfUnmatched(frechetIte, 45, 4));
		System.out.println("Second  : " + portionOfUnmatched(secondIte, 45,  4));
		
		System.out.println();
		System.out.println();
		System.out.println("Portion of Matched Attraction");
		System.out.println("Normal  : " + portionOfMatched(normal, 45, 4));
		System.out.println("Default : " + portionOfMatched(defAtt, 45,  4));
		System.out.println("GPX     : " + portionOfMatched(gpxAtt, 45,  4));
		System.out.println("Hausd   : " + portionOfMatched(hausdorffAtt, 45,  4));
		System.out.println("Frechet : " + portionOfMatched(frechetAtt, 45, 4));
		System.out.println("Second  : " + portionOfMatched(secondAtt, 45,  4));
		System.out.println("Portion of Matched Iterative");
		System.out.println("Normal  : " + portionOfMatched(normal, 45, 4));
		System.out.println("Default : " + portionOfMatched(defIte, 45,  4));
		System.out.println("GPX     : " + portionOfMatched(gpxIte, 45,  4));
		System.out.println("Hausd   : " + portionOfMatched(hausdorffIte, 45,  4));
		System.out.println("Frechet : " + portionOfMatched(frechetIte, 45, 4));
		System.out.println("Second  : " + portionOfMatched(secondIte, 45,  4));		
	}
}
