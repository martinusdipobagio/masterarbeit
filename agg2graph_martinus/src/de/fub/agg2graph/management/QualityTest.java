package de.fub.agg2graph.management;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSSegment;

public class QualityTest {
	public Map<String, Integer> numberOfPoints;
	public Map<String, Double> distances;
	
	public QualityTest() {
		numberOfPoints = new HashMap<String, Integer>();
		distances = new HashMap<String, Double>();
	}
	
	public void insertData(String directory, String name) {
		
		File f = new File("test/input/" + directory + name);
		GPSSegment segment = GPXReader.getSegments(f).get(0);
		
		numberOfPoints.put(name, segment.size());
		double dist = GPSCalc.traceLengthMeter(segment);
//		System.out.println("name : " + name + " : " + dist);
		distances.put(name, dist);
		
	}
	
	public Double getShortestTrack() {
		Double shortestValue = Double.MAX_VALUE;
		String shortestKey = "";
		Set<String> key = distances.keySet();
		for(String k : key) {
			Double currentValue = distances.get(k);
			if(shortestValue > currentValue) {
				shortestValue = currentValue;
				shortestKey = k;
			}
		}
		
		System.out.println("ShortestTrack");
		System.out.println("Name : " + shortestKey + " <> Distance : " + shortestValue);
		System.out.println();
		
		return shortestValue;
	}
	
	public double getLongestTrack() {
		double longestValue = 0;
		String longestKey = "";
		Set<String> key = distances.keySet();
		for(String k : key) {
			double currentValue = distances.get(k);
			if(longestValue < currentValue) {
				longestValue = currentValue;
				longestKey = k;
			}
		}
		
		System.out.println("LongestTrack");
		System.out.println("Name : " + longestKey + " <> Distance : " + longestValue);
		System.out.println();
		
		return longestValue;
	}
	
	public double getAverageTrack() {
		double sum = 0;
		Set<String> key = distances.keySet();
		for(String k : key) {
			double currentValue = distances.get(k);
//			System.out.println(currentValue);
			sum += currentValue;
		}
		
		sum = sum / key.size();
		System.out.println("Average-Distance : " + sum);
		System.out.println();
		
		return sum;
	}
	
	public double getSumDistance() {
		double sum = 0.0;
		Set<String> key = distances.keySet();
		for(String k : key) {
			sum += distances.get(k);
		}
		
		System.out.println("Sum-Distance : " + sum);
		System.out.println();
		
		return sum;
	}
	
	public Integer getNumberOfPoints() {
		Integer sum = 0;
		Set<String> key = numberOfPoints.keySet();
		for(String k : key) {
			sum += numberOfPoints.get(k);
		}
		System.out.println("Number of Points : " + sum);
		System.out.println();
		
		return sum;
	}
	
	public Integer getMostPoints() {
		Integer mostValue = 0;
		String mostKey = "";
		Set<String> key = numberOfPoints.keySet();
		for(String k : key) {
			Integer currentValue = numberOfPoints.get(k);
			if(mostValue < currentValue) {
				mostValue = currentValue;
				mostKey = k;
			}
		}
		
		System.out.println("MostPoints");
		System.out.println("Name : " + mostKey + " <> Distance : " + mostValue);
		System.out.println();
		
		return mostValue;
	}
	
	public Integer getFewestPoints() {
		Integer fewestValue = Integer.MAX_VALUE;
		String fewestKey = "";
		Set<String> key = numberOfPoints.keySet();
		for(String k : key) {
			Integer currentValue = numberOfPoints.get(k);
			if(fewestValue > currentValue) {
				fewestValue = currentValue;
				fewestKey = k;
			}
		}
		
		System.out.println("FewestPoints");
		System.out.println("Name : " + fewestKey + " <> Distance : " + fewestValue);
		System.out.println();
		
		return fewestValue;
	}
	
	public static void main(String[] args) {
		String[] dir = {"Scen1 - Kreisverkehr/",  "Scen2 - Konstanzerstrasse/", 
				"Scen3 - Normal/",  "Scen4 - Rudolstadter/",
				"Scen5 - Kynaststrasse/",  "Scen6 - Rosenthaler/", "Scen7 - Spandauer/",  "Scen8 - Tempelhof/",
				"Scen9 - Spichernstrasse/",  "Scen10 - Potsdamer/"};
		String[] name = {"agg.gpx", "indi1.gpx", "indi2.gpx", "indi3.gpx", //4
				
				"konstanzerstrasse1.gpx", "konstanzerstrasse2.gpx", //2
				
				"1.gpx", "3.gpx", //2
				
				"a100_autobahn2.gpx", "a100_autobahn4.gpx", "a100_autobahn5.gpx", "a100_autobahn6.gpx", "a100_autobahn7.gpx", "a100_autobahn8.gpx", "a100_autobahn9.gpx", //7
				
				"kynaststrasse1.gpx", "kynaststrasse2.gpx", //2
				
				"rosenthaler1.gpx", "rosenthaler3.gpx", //2
				
				"spandauerstrasse1.gpx", "spandauerstrasse2.gpx", "spandauerstrasse3.gpx", "spandauerstrasse4.gpx", "spandauerstrasse5.gpx", //5
				
				"tempelhof6.gpx", "tempelhof7.gpx", "tempelhof10.gpx", "tempelhof11.gpx", "tempelhof12.gpx", 
				"tempelhof13.gpx", "tempelhof14.gpx", "tempelhof15.gpx", "tempelhof16.gpx", "tempelhof17.gpx", //10
				
				"spichernstrasse3.gpx", "spichernstrasse5.gpx", "spichernstrasse6.gpx", "spichernstrasse7.gpx", "spichernstrasse9.gpx", //5
				
				"potsdamer1.gpx", "potsdamer2.gpx", "potsdamer3.gpx", "potsdamer4.gpx", "potsdamer5.gpx" //5
		};
		
		QualityTest qt = new QualityTest();
		qt.insertData(dir[0], name[0]);
		qt.insertData(dir[0], name[1]);
		qt.insertData(dir[0], name[2]);
		qt.insertData(dir[0], name[3]);
		
		qt.insertData(dir[1], name[4]);
		qt.insertData(dir[1], name[5]);
		
		qt.insertData(dir[2], name[6]);
		qt.insertData(dir[2], name[7]);

		qt.insertData(dir[3], name[8]);
		qt.insertData(dir[3], name[9]);
		qt.insertData(dir[3], name[10]);
		qt.insertData(dir[3], name[11]);
		qt.insertData(dir[3], name[12]);
		qt.insertData(dir[3], name[13]);
		qt.insertData(dir[3], name[14]);
		
		qt.insertData(dir[4], name[15]);
		qt.insertData(dir[4], name[16]);
		
		qt.insertData(dir[5], name[17]);
		qt.insertData(dir[5], name[18]);
		
		qt.insertData(dir[6], name[19]);
		qt.insertData(dir[6], name[20]);
		qt.insertData(dir[6], name[21]);
		qt.insertData(dir[6], name[22]);
		qt.insertData(dir[6], name[23]);
		
		qt.insertData(dir[7], name[24]);
		qt.insertData(dir[7], name[25]);
		qt.insertData(dir[7], name[26]);
		qt.insertData(dir[7], name[27]);
		qt.insertData(dir[7], name[28]);
		qt.insertData(dir[7], name[29]);
		qt.insertData(dir[7], name[30]);
		qt.insertData(dir[7], name[31]);
		qt.insertData(dir[7], name[32]);
		qt.insertData(dir[7], name[33]);
		
		qt.insertData(dir[8], name[34]);
		qt.insertData(dir[8], name[35]);	
		qt.insertData(dir[8], name[36]);	
		qt.insertData(dir[8], name[37]);
		qt.insertData(dir[8], name[38]);
		
		qt.insertData(dir[9], name[39]);
		qt.insertData(dir[9], name[40]);
		qt.insertData(dir[9], name[41]);
		qt.insertData(dir[9], name[42]);
		qt.insertData(dir[9], name[43]);
		
		
		qt.getShortestTrack();
		qt.getLongestTrack();
		qt.getAverageTrack();
		qt.getSumDistance();
		qt.getNumberOfPoints();
		qt.getMostPoints();
		qt.getFewestPoints();
			
	}
}
