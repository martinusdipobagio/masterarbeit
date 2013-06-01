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
	
	public static List<String> getAggFile(List<String> list) {
		List<String> aggFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 2).concat(end);
			aggFiles.add(file);
		}
		
		return aggFiles;
	}
	
	public static List<String> getTraceFile(List<String> list) {
		List<String> traceFiles = new ArrayList<String>();
		String end = ".gpx";
		for(String line : list) {
			String file = getToken(line, 1).concat(end);
			traceFiles.add(file);
		}
		
		return traceFiles;
	}
	
	public static List<Integer> getRequirement(List<String> list) {
		List<Integer> kReqs = new ArrayList<Integer>();
		for(String line : list) {
			Integer k = Integer.parseInt(getToken(line, 0));
			kReqs.add(k);
		}
		
		return kReqs;
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
