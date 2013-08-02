/*******************************************************************************
   Copyright 2013 Martinus Dipobagio

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
******************************************************************************/
package de.fub.agg2graph.agg.strategy;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.IAggregatedMap;
import de.fub.agg2graph.structs.frechet.ITrace;
import de.fub.agg2graph.structs.frechet.Pair;
import de.fub.agg2graph.structs.frechet.PartialFrechetDistance;

public class ConformalPathDistance implements ITraceDistance {

	public double maxDistance = 20;

	public static AggContainer aggContainer;
	public IAggregatedMap map;
	public ITrace trace;
	public ILocation start;
	
	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
		int segmentLength;
		
		//1. Input: Converting List<GPSPoint> to List<GPSEdge>
		List<GPSEdge> path1 = new ArrayList<GPSEdge>();
		for(int i = 0; i < aggPath.size() - 1; i++) {
			path1.add(new GPSEdge(aggPath.get(i), aggPath.get(i+1)));
		}
		
		//2. Input: Converting List<GPSPoint> to List<GPSEdge>
		List<GPSEdge> path2 = new ArrayList<GPSEdge>();
		for(int i = startIndex; i < tracePoints.size() - 1; i++) {
			path2.add(new GPSEdge(tracePoints.get(i), tracePoints.get(i+1)));
		}
		segmentLength = tracePoints.size();
		tracePoints = tracePoints.subList(startIndex, tracePoints.size());
		
		//Return if empty
		if(path1.size() == 0 || path2.size() == 0)
			return new Object[] {0, new ArrayList<List<AggNode>>(), new ArrayList<List<GPSPoint>>()};
		
		//Generate Partial Frachet Distance
		PartialFrechetDistance pfd = new PartialFrechetDistance(path1, path2, maxDistance/92500.0);
		
		//Gather the result and extract it to AggNodes and GPSPoints
		List<Pair<Point, Point>> ret = pfd.getConformalPath();
		List<List<AggNode>> aggResults = extractAgg(ret, aggPath, tracePoints);
		List<List<GPSPoint>> traceResults = extractTrace(ret, aggPath, tracePoints);
		return new Object[] {segmentLength, aggResults, traceResults};
	}
	
	/**
	 * Converting the conformal path of free space diagram to AggNodes
	 * @param ret
	 * @param aggPath
	 * @param tracePoints
	 * @return
	 */
	private List<List<AggNode>> extractAgg(List<Pair<Point, Point>> ret,
			List<AggNode> aggPath, List<GPSPoint> tracePoints) {
		List<List<AggNode>> aggResults = new ArrayList<List<AggNode>>();
		
		//Generate the results
		for(Pair<Point, Point> r : ret) {
			List<AggNode> agg = new ArrayList<AggNode>();
			//Get the start and next-to-last points from agg's part
			int start = r.a.x;
			int nextToLast = r.b.x;
			//extract the trace between start and next to last points
			for(int i = start; i <= Math.min(nextToLast, aggPath.size()); i++) {
				agg.add(aggPath.get(i));
			}
			
			//The real end point calculation
			int gpsEnd = r.b.y;
			if(nextToLast + 1 < aggPath.size()) {
				if(GPSCalc.getDistanceTwoPointsMeter(aggPath.get(nextToLast + 1), tracePoints.get(gpsEnd)) <= maxDistance)
					agg.add(aggPath.get(nextToLast + 1));
				else if(gpsEnd + 1 < tracePoints.size()) { 
					if(GPSCalc.getDistanceTwoPointsMeter(aggPath.get(nextToLast + 1), tracePoints.get(gpsEnd + 1)) <= maxDistance)
						agg.add(aggPath.get(nextToLast + 1));
				}
			}				
			
			aggResults.add(agg);
		}
		return aggResults;
	}
	
	/**
	 * Converting the conformal path of free space diagram to GPSPoints
	 * @param ret
	 * @param aggPath
	 * @param tracePoints
	 * @return
	 */
	private List<List<GPSPoint>> extractTrace(List<Pair<Point, Point>> ret,
			List<AggNode> aggPath, List<GPSPoint> tracePoints) {
		List<List<GPSPoint>> traceResults = new ArrayList<List<GPSPoint>>();
		
		//Generate the results
		for(Pair<Point, Point> r : ret) {
			List<GPSPoint> trace = new ArrayList<GPSPoint>();
			//Get the start and next-to-last points from gps's part
			int start = r.a.y;
			int nextToLast = r.b.y;
			//extract the trace between start and next to last points
			for(int i = start; i <= Math.min(nextToLast, tracePoints.size()); i++) {
				trace.add(tracePoints.get(i));
			}
			//The real end point calculation
			int aggEnd = r.b.x;
			if(nextToLast + 1 < tracePoints.size()) {
				if(GPSCalc.getDistanceTwoPointsMeter(tracePoints.get(nextToLast + 1), aggPath.get(aggEnd)) <= maxDistance)
					trace.add(tracePoints.get(nextToLast + 1));
				else if(aggEnd + 1 < aggPath.size()) {
					if(GPSCalc.getDistanceTwoPointsMeter(tracePoints.get(nextToLast + 1), aggPath.get(aggEnd + 1)) <= maxDistance)
						trace.add(tracePoints.get(nextToLast + 1));
				}
			}	
			
			traceResults.add(trace);
		}
		return traceResults;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}
}
