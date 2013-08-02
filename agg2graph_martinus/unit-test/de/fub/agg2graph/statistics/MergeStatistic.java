package de.fub.agg2graph.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.management.FileList;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;

public class MergeStatistic {
	private final String fileListDirectory = "test/configuration/"; // Manually
																	// inserted
	private final String traceFilesDirectory = "test/input/mergeEval-trace/"; // Manually
																				// inserted
	private final String aggFiles1Directory = "test/input/mergeEval-agg1/"; // Manually
																			// inserted
	private final String aggFiles2Directory = "test/input/mergeEval-agg2/"; // Manually
																			// inserted
	private final String outputDirectory = "test/output/"; 					// Manually
																			// inserted
	private List<String> firstAggName;
	private List<String> secondAggName;
	private List<String> traceName;
	private String outputName;
	
	public MergeStatistic(String fileListName, String output) throws IOException {
		// Default file list
		if (fileListName.equals(""))
			fileListName = "statistic_of_mergelist1.txt";
		List<String> fileList = FileList.readFile(fileListDirectory,
				fileListName);
		outputName = outputDirectory + output;
		
		traceName = FileList.getTrace(fileList);
		firstAggName = FileList.getAggFileFirst(fileList);
		secondAggName = FileList.getAggFileSecond(fileList);
		
		if(traceName.size() != firstAggName.size() && firstAggName.size() != secondAggName.size())
			System.err.println("Stimmt irgendwas nicht");
		
		for(int i = 0; i < traceName.size(); i++) {
			GPSSegment trace = GPXReader.getSegments(
					new File(traceFilesDirectory + traceName.get(i))).get(0);
			GPSSegment agg1 = GPXReader.getSegments(
					new File(aggFiles1Directory + firstAggName.get(i))).get(0);
			GPSSegment agg2 = GPXReader.getSegments(
					new File(aggFiles2Directory + secondAggName.get(i))).get(0);
			double[] agg1ToInd = getPointToLineDistances(agg1, trace);
			double[] agg2ToInd = getPointToLineDistances(agg2, trace);
			double avgAgg1ToInd = getAverageDistance(agg1ToInd);
			double avgAgg2ToInd = getAverageDistance(agg2ToInd);

			double[] indToAgg1 = getPointToLineDistances(trace, agg1);
			double[] indToAgg2 = getPointToLineDistances(trace, agg2);
			double avgIndToAgg1 = getAverageDistance(indToAgg1);
			double avgIndToAgg2 = getAverageDistance(indToAgg2);
			
			System.out.println("========== " + traceName.get(i) + " <> " + firstAggName.get(i) + " & " + secondAggName.get(i) + " ==========");
			System.out.println("AGG1 --> IND : " + avgAgg1ToInd);
			System.out.println("IND --> AGG1 : " + avgIndToAgg1);
			System.out.println("AGG 2 <> IND");
			System.out.println("AGG2 --> IND : " + avgAgg2ToInd);
			System.out.println("IND --> AGG2 : " + avgIndToAgg2);
			String value = avgAgg1ToInd + "\t" + avgIndToAgg1 + "\t" + avgAgg2ToInd + "\t" + avgIndToAgg2;
			writefile(value);
		}
	}
	
	private double[] getPointToLineDistances(List<? extends ILocation> from,
			List<? extends ILocation> to) {
		double[] result = new double[from.size()];
		ILocation loc;
		for (int i = 0; i < from.size(); i++) {
			loc = from.get(i);			
				
			result[i] = GPSCalc.getDistancePointToTraceMeter(loc, to)[0];
		}
		return result;
	}
	
	private double getAverageDistance(double[] aggToTraceDistances) {
		double sum = 0;
		int maxCounter = 0;
		for (double d : aggToTraceDistances) {
			if(d < Double.MAX_VALUE) 
				sum += d;
			else
				maxCounter++;
		}
		return sum / (aggToTraceDistances.length - maxCounter);
	}
	
	private void writefile(String value) throws IOException {
		FileWriter fstream = new FileWriter(outputName, true);
		BufferedWriter fbw = new BufferedWriter(fstream);
		fbw.write(value);
		
		fbw.newLine();
		fbw.close();
	}

	public static void main(String[] args) throws IOException {
		new MergeStatistic("statistic_of_mergelist2.txt", "HausdorffIterative-abbiegen-somegood.txt");
	}
}
