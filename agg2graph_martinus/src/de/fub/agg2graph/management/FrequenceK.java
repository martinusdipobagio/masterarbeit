package de.fub.agg2graph.management;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.xml.sax.Attributes;

import de.fub.agg2graph.input.GPXPointReaderHandler;

public class FrequenceK {

	SortedMap<Integer, Integer> frequency;

	String outputK;
	String outputFreq;
	
	public FrequenceK() {
		frequency = new TreeMap<Integer, Integer>();
	}
	
	public FrequenceK(GPXPointReaderHandler reader) {
		frequency = new TreeMap<Integer, Integer>();
		List<Integer> ks = reader.getK();
		
		for(Integer k : ks) {
			putToTable(k);
		}
		
		printOut();
	}

	public FrequenceK(String namespaceURI, String localName,
			String qName, Attributes atts) {
		frequency = new TreeMap<Integer, Integer>();
		GPXPointReaderHandler reader = new GPXPointReaderHandler();
		reader.startElement(namespaceURI, localName, qName, atts);
		List<Integer> ks = reader.getK();
		
		for(Integer k : ks) {
			putToTable(k);
		}
		
		printOut();
	}
	
	public void putToTable(Integer k) {
		if(!frequency.containsKey(k)) {
			frequency.put(k, 1);
		}
		else {
			int v = frequency.get(k);
			v = v + 1;
			frequency.put(k, v);
		}
	}
	
	public void printOut() {
		// TODO write to txt file
		System.out.println("Sorted Map : " + frequency);
		
		outputK = "[ ";
		outputFreq = "[ ";
		Set<Entry<Integer, Integer>> k = frequency.entrySet();
        Iterator<Entry<Integer, Integer>> it = k.iterator();
        Entry<Integer, Integer> entry;
        Integer prevValue = -1;
        while(it.hasNext()) {
        	entry = it.next();
        	if(prevValue == -1 || (entry.getKey() - prevValue == 1)) {
        		outputK += entry.getKey() + " ";
        		outputFreq += entry.getValue() + " ";
        		prevValue = entry.getKey();
        	} 
        	else {
        		while(entry.getKey() - prevValue > 1) {
        			outputK += ++prevValue + " ";
            		outputFreq += 0 + " ";
        		}
        		outputK += entry.getKey() + " ";
        		outputFreq += entry.getValue() + " ";
        		prevValue = entry.getKey();
        	}
        	
        }
		outputK += "]";
		outputFreq += "]";
		System.out.println("k = " + outputK);
		System.out.println("f = " + outputFreq);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
