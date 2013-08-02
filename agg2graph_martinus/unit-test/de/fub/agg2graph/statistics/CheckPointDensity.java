package de.fub.agg2graph.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

public class CheckPointDensity {

	List<Double> segmentDensity;
	List<Integer> numberOfPoints;
	List<Double> segmentLengths;
	
	public CheckPointDensity() {
		segmentDensity = new ArrayList<Double>();
		numberOfPoints = new ArrayList<Integer>();
		segmentLengths = new ArrayList<Double>();
	}
	
	public void getFileList() {
		// Directory path here
		String originalPath = "raw/";

		String files;
		File folder = new File(originalPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			files = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if (files.endsWith(".gpx") || files.endsWith(".GPX")) {
					readData(originalPath + files);
				}
			} else {
				getFileList(originalPath + files + "/");
			}
		}
	}
	
	public void getFileList2() {
		// Directory path here
		String originalPath = "raw/";

		String files;
		File folder = new File(originalPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			files = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if (files.endsWith(".gpx") || files.endsWith(".GPX")) {
					readData2(originalPath + files);
				}
			} else {
				getFileList2(originalPath + files + "/");
			}
		}
	}

	private void getFileList(String originalPath) {
		String files;
		File folder = new File(originalPath);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			files = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if (files.endsWith(".gpx") || files.endsWith(".GPX")) {
					readData(originalPath + files);
				}
			} else {
				getFileList(originalPath + files + "/");
			}
		}
	}
	
	private void getFileList2(String originalPath) {
		String files;
		File folder = new File(originalPath);
		File[] listOfFiles = folder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			files = listOfFiles[i].getName();
			if (listOfFiles[i].isFile()) {
				if (files.endsWith(".gpx") || files.endsWith(".GPX")) {
					readData2(originalPath + files);
				}
			} else {
				getFileList2(originalPath + files + "/");
			}
		}
	}

	private void readData(String string) {
		List<GPSSegment> segments = GPXReader.getSegments(new File(string));
		double dist;
		for(GPSSegment segment : segments) {
			int segmentDist = 0;
			numberOfPoints.add(segment.size());
			for(int i = 1; i < segment.size(); i++) {
				dist = GPSCalc.getDistanceTwoPointsMeter(segment.get(i-1), segment.get(i));
				segmentDist += dist;
			}
			if(segmentDist > 0) {
				segmentDensity.add(segmentDist/(segment.size()-1.0));
			}
		}	
	}
	
	private void readData2(String string) {
		List<GPSSegment> segments = GPXReader.getSegments(new File(string));
		for(GPSSegment segment : segments) {
			double segmentLength = 0;
			GPSPoint last = null;
			numberOfPoints.add(segment.size());
			for(GPSPoint current : segment) {
				if(last == null) {
					last = current;
					continue;
				}
				segmentLength += GPSCalc.getDistanceTwoPointsMeter(last, current);
				last = current;
			}
			segmentLengths.add(segmentLength);
		}	
	}
	
	public void evaluate() {
		System.out.println(numberOfPoints.size());
		System.out.println("Max-Average    : " + Collections.max(segmentDensity) + " Meter");
		System.out.println("Min-Average    : " + Collections.min(segmentDensity) + " Meter");
		System.out.println("Max : " + Collections.max(numberOfPoints));
		System.out.println("Min : " + Collections.min(numberOfPoints));
		double sum = 0;
		for(Double d : segmentDensity) {
			sum += d;
		}
		sum /= segmentDensity.size();
		
		System.out.println("Average        : " + sum + " Meter");
	}
	
	public void evaluate2() {
		int pointSum = 0;
		double lengthSum = 0;
		for(Integer p : numberOfPoints) {
			pointSum += p;
		}
		for(double d : segmentLengths) {
			lengthSum += d;
		}
		System.out.println("Anzahl der Spuren : " + numberOfPoints.size());
		System.out.println("Anzahl der Spuren : " + segmentLengths.size());
		System.out.println("Gesamtlänge der Spuren  : " + lengthSum/1000.0);
		System.out.println("Durschnittliche ....... : " + (lengthSum/1000.0 / (segmentLengths.size()*1.0)));
		System.out.println("Gesamtanzahl der Knoten : " + pointSum);
		System.out.println("Durschnittliche ....... : " + (pointSum / (numberOfPoints.size()*1.0)));
		System.out.println();
	}

	public static void main(String[] args) {
		CheckPointDensity cpd = new CheckPointDensity();
		cpd.getFileList2();
		cpd.evaluate2();
	}
}
