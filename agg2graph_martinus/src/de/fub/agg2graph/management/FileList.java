package de.fub.agg2graph.management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class FileList {	
	public static List<String> readFile(String directory, String name) throws IOException {
		List<String> list = new ArrayList<String>();
		FileReader fr = new FileReader(directory + name);
		BufferedReader br = new BufferedReader(fr);
		String newLine;
		while((newLine = br.readLine()) != null) {
			list.add(newLine);
		}
		br.close();
		
		return list;
	}
	
	/**
	 * Get token from a line if n == 0
	 * @param line
	 * @param n
	 * @return
	 */
	private static String getToken(String line, int n) {
		StringTokenizer st = new StringTokenizer(line);
		while(st.hasMoreTokens()) {
			String nextToken = st.nextToken();
			if(n == 0) {
				return nextToken;
			}
			n--;
		}	
		return null;
	}

	/** Part of Match- and MergeAutoTest to read input files */
	/**
	 * Part of Match- and MergeAutoTest, get kRequirements ==> 1st Token
	 * @param list
	 * @return
	 */
	public static List<Integer> getRequirement(List<String> list) {
		List<Integer> kReqs = new ArrayList<Integer>();
		for(String line : list) {
			Integer k = Integer.parseInt(getToken(line, 0));
			kReqs.add(k);
		}
		
		return kReqs;
	}

	/**
	 * Part of Match- and MergeAutoTest, get Traces ==> 2nd Token
	 * @param list
	 * @return
	 */
	public static List<String> getTraceFile(List<String> list) {
		List<String> traceFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 1).concat(end);
			traceFiles.add(file);
		}
		
		return traceFiles;
	}

	/**
	 * Part of Match- and MergeAutoTest, get Agg ==> 3rd Token
	 * @param list
	 * @return
	 */
	public static List<String> getAggFile(List<String> list) {
		List<String> aggFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 2).concat(end);
			aggFiles.add(file);
		}
		
		return aggFiles;
	}
	/** Part of Statistic to read file */
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> getTrace(List<String> list) {
		List<String> aggFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 0).concat(end);
			aggFiles.add(file);
		}
		
		return aggFiles;
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> getAggFileFirst(List<String> list) {
		List<String> aggFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 1).concat(end);
			aggFiles.add(file);
		}
		
		return aggFiles;
	}
	
	public static List<String> getAggFileSecond(List<String> list) {
		List<String> aggFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 2).concat(end);
			aggFiles.add(file);
		}
		
		return aggFiles;
	}
	
	
	
	public static void main(String[] args) throws IOException {
//		String location = "test/configuration/";
//		String name = "filelist.txt";
//		FileList fl = new FileList(location, name);
//		List<String> list = fl.readFile();
//		List<String> agg = getAggFile(list);
//		List<String> tra = getTraceFile(list);
//		List<Integer> k = getRequirement(list);
//		for(int i = 0; i < list.size(); i++) {
//			System.out.println(k.get(i) + " <> " + tra.get(i) + " <> " + agg.get(i));
//		}
	}
}
