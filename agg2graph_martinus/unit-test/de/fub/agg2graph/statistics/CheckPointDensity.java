package de.fub.agg2graph.statistics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSSegment;

public class CheckPointDensity {

	List<Double> segmentDensity;
	
	public CheckPointDensity() {
		segmentDensity = new ArrayList<Double>();
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

	private void readData(String string) {
		List<GPSSegment> segments = GPXReader.getSegments(new File(string));
		double dist;
		for(GPSSegment segment : segments) {
			int segmentDist = 0;
			for(int i = 1; i < segment.size(); i++) {
				dist = GPSCalc.getDistanceTwoPointsMeter(segment.get(i-1), segment.get(i));
				segmentDist += dist;
			}
			if(segmentDist > 0) {
				segmentDensity.add(segmentDist/(segment.size()-1.0));
			}
		}	
	}
	
	public void evaluate() {
		System.out.println("Max-Average    : " + Collections.max(segmentDensity) + " Meter");
		System.out.println("Min-Average    : " + Collections.min(segmentDensity) + " Meter");
		
		double sum = 0;
		for(Double d : segmentDensity) {
			sum += d;
		}
		sum /= segmentDensity.size();
		
		System.out.println("Average        : " + sum + " Meter");
	}

	public static void main(String[] args) {
		CheckPointDensity cpd = new CheckPointDensity();
		cpd.getFileList();
		cpd.evaluate();
	}
}
