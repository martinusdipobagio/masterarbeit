package de.fub.agg2graph.statistics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyStatistic {

	private String name;
	private int number = 0;
	private long runtimeMatch = 0;
	private long runtimeMerge = 0;
	
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
		values.add(new Double(runtimeMatch));
		values.add(new Double(runtimeMerge));
		return values;
	}

	public void setRuntimeMatch(long runtime) {
		this.runtimeMatch = runtime;
	}

	public void resetRuntimeMatch() {
		this.runtimeMatch = 0;
	}
	
	public void setRuntimeMerge(long runtimeMerge) {
		this.runtimeMerge = runtimeMerge;
	}
	
	public void resetRuntimeMerge() {
		this.runtimeMerge = 0;
	}
	
	public void resetAll() {
		this.resetRuntimeMatch();
		this.resetRuntimeMerge();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public long getRuntimeMatch() {
		return runtimeMatch;
	}

	public long getRuntimeMerge() {
		return runtimeMerge;
	}
}
