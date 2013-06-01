package de.fub.agg2graph.management;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyStatistic {

	private double aggLength = 0;
	private double matchedAggLength = 0;
	private double traceLength = 0;
	private double matchedTraceLength = 0;
	private double newAgg = 0;
	private double traceErrorCalculation = 0;
	private String name;
	private int number = 0;
	private long runtime = 0;
	private long memoryUsed = 0;
	
	public MyStatistic(String name) {
		this.name = name;
	}

	/**
	 * Write a file with some statistic value
	 * 
	 * @param name
	 *            file name and directory
	 * @param value
	 *            {agg-length ; ind-length ; matched-agg-length ;
	 *            matched-ind-length ; new-agg-length}
	 * @throws IOException
	 */
	public void writefile() throws IOException {
		List<Double> values = wrapValues();
		FileWriter fstream = new FileWriter(name, true);
		BufferedWriter fbw = new BufferedWriter(fstream);
		fbw.write(number++ + "\t");
		for (Double value : values) {
			fbw.write(value + "\t");
		}
		fbw.newLine();
		fbw.close();
	}
	
	public List<Double> wrapValues() {
		List<Double> values = new ArrayList<Double>();
		values.add(aggLength);
		values.add(matchedAggLength);
		values.add(traceLength);
		values.add(matchedTraceLength);
		values.add(newAgg);
		values.add(traceErrorCalculation);
		
		return values;
	}

	// public static void main(String[] args) throws IOException {
	// FileWriter fstream = new
	// FileWriter("test/exp/GpxMatch-AttractionMerge.txt", true);
	// BufferedWriter fbw = new BufferedWriter(fstream);
	// fbw.write("Hier passiert etwas ...             ");
	// fbw.write("Hello World!!!");
	// fbw.newLine();
	// fbw.close();
	// }

	public void setAggLength(double aggLength) {
		this.aggLength += aggLength;
	}
	
	public void resetAggLength() {
		this.aggLength = 0;
	}

	public void setMatchedAggLength(double matchedAggLength) {
		this.matchedAggLength += matchedAggLength;
	}
	
	public void resetMatchedAggLength() {
		this.matchedAggLength = 0;
	}
	
	public void setTraceLength(double traceLength) {
		this.traceLength += traceLength;
	}

	public void resetTraceLength() {
		this.traceLength = 0;
	}
	
	public void setMatchedTraceLength(double matchedTraceLength) {
		this.matchedTraceLength += matchedTraceLength;
	}
	
	public void resetMatchedTraceLength() {
		this.matchedTraceLength = 0;
	}

	public void setNewAgg(double newAgg) {
		this.newAgg += newAgg;
	}
	
	public void resetNewAgg() {
		this.newAgg = 0;
	}

	public void setTraceErrorCalculation(double traceErrorCalculation) {
		this.traceErrorCalculation += traceErrorCalculation;
	}
	
	public void resetTraceErrorCalculation() {
		this.traceErrorCalculation = 0;
	
	}	
	public void setRuntime(long runtime) {
		this.runtime = runtime;
	}

	public void resetRuntime() {
		this.runtime = 0;
	}
	
	public void setMemoryUsed(long memoryUsed) {
		this.memoryUsed = memoryUsed;
	}

	public void resetMemoryUsed() {
		this.memoryUsed = 0;
	}
	
	public void resetAll() {
		this.resetAggLength();
		this.resetMatchedAggLength();
		this.resetTraceLength();
		this.resetMatchedTraceLength();
		this.resetNewAgg();
		this.resetTraceErrorCalculation();
		this.resetRuntime();
		this.resetMemoryUsed();
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public double getAggLength() {
		return aggLength;
	}

	public double getMatchedAggLength() {
		return matchedAggLength;
	}

	public double getTraceLength() {
		return traceLength;
	}

	public double getMatchedTraceLength() {
		return matchedTraceLength;
	}

	public double getNewAgg() {
		return newAgg;
	}

	public double getTraceErrorCalculation() {
		return traceErrorCalculation;
	}
	
	public String getName() {
		return name;
	}

	public long getRuntime() {
		return runtime;
	}

	public long getMemoryUsed() {
		return memoryUsed;
	}
}
