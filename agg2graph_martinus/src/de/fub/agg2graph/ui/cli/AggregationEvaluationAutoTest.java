/*******************************************************************************
 * Copyright (c) 2012 Johannes Mitlmeier.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/agpl-3.0.html
 * 
 * Contributors:
 *     Johannes Mitlmeier - initial API and implementation
 ******************************************************************************/
package de.fub.agg2graph.ui.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.graph.RamerDouglasPeuckerFilter;
import de.fub.agg2graph.input.GPSCleaner;
import de.fub.agg2graph.input.GPSFilter;
import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.management.FileList;
import de.fub.agg2graph.structs.GPSSegment;

public class AggregationEvaluationAutoTest {
	
	/** Directories and files names */
	private final String fileListDirectory = "test/configuration/";
	private final String traceFilesDirectory = "test/input/exp/";
	private final String aggFilesDirectory = "test/input/map 2.0a/";
	private final String fileListName1 = "filelist1.txt";
	private final String fileListName2 = "filelist2.txt";
	private final String fileListName3 = "filelist3.txt";
	private String currentAggName; 
	private List<String> aggFileName;
	
	/** Parameter names */
	private List<Integer> kReqs;

	/** Help Classes */
	private AggContainer aggContainer;
	GPSFilter gpsFilter;
	RamerDouglasPeuckerFilter rdpf;
	GPSCleaner gpsCleaner;
	
	
	/** Segments */
	private List<GPSSegment> traceSegments;
	private List<GPSSegment> aggSegment;
	
	public AggregationEvaluationAutoTest(String name) throws IOException {
//			, IAggregationStrategy aggStrategy, ICachingStrategy cacheStrategy) throws IOException {
		//Define Help Classes
		gpsFilter = new GPSFilter();
		gpsCleaner = new GPSCleaner();
		rdpf = new RamerDouglasPeuckerFilter(15);
//		aggContainer = AggContainer.createContainer(new File("test/agg/" + name), aggStrategy, cacheStrategy);
		aggContainer = AggContainer.createContainer(new File("test/agg/" + name));
		
		//Load file & k-req configuration
		List<String> fileList = FileList.readFile(fileListDirectory, fileListName3);
		aggFileName = FileList.getAggFile(fileList);
		kReqs = FileList.getRequirement(fileList);
		
		//Load agg files
		currentAggName = "map0.gpx";
		aggSegment = GPXReader.getSegments(new File(aggFilesDirectory + currentAggName));
		
		//Load trace files
		traceSegments = new ArrayList<GPSSegment>();
		List<String> traceFiles = FileList.getTraceFile(fileList);
		for(String traceFile : traceFiles) {
			GPSSegment traceSegment = GPXReader.getSegments(new File(traceFilesDirectory + traceFile)).get(0);
			traceSegments.add(traceSegment);
		}

		//Load Aggregation Strategy and parameter
	}
	
	/**
	 * Run the evaluation
	 */
	public void runEvaluation() {
		int iteration = 0;
		for(GPSSegment traceSegment : traceSegments) {
			//Filter-Agg
			filterAgg(aggSegment, kReqs.get(iteration));
			
			//Clean-Agg
			cleanAgg(aggSegment);
			
			//Aggregation
			aggregate(aggSegment, traceSegment);
			
			//Set for the next iteration
			currentAggName = aggFileName.get(iteration);
			aggSegment = GPXReader.getSegments(new File(aggFilesDirectory + currentAggName));
			aggContainer.clear();
			iteration++;
		}
	}
	
	private List<GPSSegment> filterAgg(List<GPSSegment> agg, int kReq) {
		List<GPSSegment> filteredAgg = new ArrayList<GPSSegment>();
		gpsFilter.getFilterOptions().setkRequirement(kReq);
		for(GPSSegment a : agg) {
			filteredAgg.add(gpsFilter.filter(a));
		}
		aggContainer.getAggregationStrategy().setAddAllowed(gpsFilter.getFilterOptions().getNewSegmentAllowed());
		
		return filteredAgg;
	}
	
	private List<GPSSegment> cleanAgg(List<GPSSegment> agg) {
		List<GPSSegment> cleanedAgg = new ArrayList<GPSSegment>();
		for(GPSSegment a : agg) {
			for(GPSSegment cleaned : gpsCleaner.clean(a)) {
				cleaned = rdpf.simplify(cleaned);
				cleanedAgg.add(cleaned);
			}
		}
		
		return cleanedAgg;
	}
	
	private void aggregate(List<GPSSegment> agg, GPSSegment trace) {
		for(GPSSegment a : agg) {
			aggContainer.addSegment(a, true);
		}
		aggContainer.addSegment(trace, false);
	}
	
	public static void main(String[] args) throws FileNotFoundException,
			IOException, InterruptedException {
		AggregationEvaluationAutoTest autoTest = new AggregationEvaluationAutoTest("Tinus");
		autoTest.runEvaluation();
	}
}