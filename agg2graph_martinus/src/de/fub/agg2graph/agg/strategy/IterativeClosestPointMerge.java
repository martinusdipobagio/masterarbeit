package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.fub.agg2graph.agg.AggCleaner;
import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.PointGhostPointPair;
import de.fub.agg2graph.graph.RamerDouglasPeuckerFilter;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.Converter;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.KdTreeIndex;
import de.fub.agg2graph.structs.frechet.TreeAggMap;
import de.fub.agg2graph.ui.gui.RenderingOptions;

public class IterativeClosestPointMerge implements IMergeHandler {

//	public WeightedMeanClosestPoints() {
//		this(1, false);
//	}
//	
//	public WeightedMeanClosestPoints(int k, boolean projected) {
//		setParameter("projected", projected);
//		setParameter("kClosestPoints", k);
//		setParameter("delta", 0.003);
//	}
//
//	@Override
//	public void merge(TreeAggMap map, Sequence<AggregatedEdge> A, Sequence<Edge> T) {
//		Sequence<Location> lT = Algorithms.toLocationSequence(T);
//		int k = (Integer)getParameter("kClosestPoints");
//		double delta  = (Double)getParameter("delta");
//		
//		KdTreeIndex<Location> traceIndex = new KdTreeIndex<>(lT);
//		
//		for(AggregatedEdge a : A) {
//			int n = a.getNumbers();
//			//Location t = traceIndex.searchNN(a.getFrom());
//			List<Location> ts = traceIndex.searchKnn(a.getFrom(), k);
//			
//			Location t = Algorithms.CalculateMean(a.getFrom(), ts, delta);
//			
//			Algorithms.moveLocation(map, a, t, a.getFrom());
//			
//			a.setNumbers(n+1);
//		}
//	}
	
	private List<AggNode> aggNodes = null;
	private List<GPSPoint> gpsPoints = null;
	public int maxLookahead = 4;
	public double minContinuationAngle = 45;
	// helper stuff
	private Map<AggConnection, List<PointGhostPointPair>> newNodesPerConn;
	private List<PointGhostPointPair> pointGhostPointPairs;
	private AggNode inNode;
	private AggNode outNode;

	private AggContainer aggContainer;
	private RenderingOptions roMatchGPS;
	// cleaning stuff
	private RamerDouglasPeuckerFilter rdpf = new RamerDouglasPeuckerFilter(0,
			125);
	private static AggCleaner cleaner = new AggCleaner().enableDefault();
	public double maxPointGhostDist = 40; // meters

	private double distance = 0;
	private AggNode beforeNode;
	
	//IterativeClosestPoints Parameter
	int k = 0;
	double delta = 0;
	
	@Override
	public void mergePoints() {
		
	}
	
	public void closestPointsMerge(TreeAggMap map, List<AggConnection> A, List<GPSEdge> T) {
		List<AggNode> lT = null;
//				Converter.toLocationSequence(T);
		
		KdTreeIndex<AggNode> traceIndex = new KdTreeIndex<AggNode>(lT);
		
		for(AggConnection a : A) {
			int n = a.getNumbers();
			//TODOLocation t = traceIndex.searchNN(a.getFrom());
//			List<AggNode> ts = traceIndex.searchKnn(a.getFrom(), k);
//			
//			ILocation t = GPSCalc.CalculateMean(a.getFrom(), ts, delta);
//			
//			GPSCalc.moveLocation(map, a, t, a.getFrom());
//			
//			a.setNumbers(n+1);
		}
	}

	@Override
	public boolean isEmpty() {
		return gpsPoints.size() == 0 && gpsPoints.size() == 0;
	}

	@Override
	public List<AggNode> getAggNodes() {
		return aggNodes;
	}

	@Override
	public List<GPSPoint> getGpsPoints() {
		return gpsPoints;
	}

	@Override
	public double getDistance() {
		return distance;
	}

	@Override
	public void setDistance(double bestDifference) {
		this.distance = bestDifference;
	}

	@Override
	public AggNode getInNode() {
		return inNode;
	}

	@Override
	public AggNode getOutNode() {
		return outNode;
	}

	@Override
	public void setBeforeNode(AggNode lastNode) {
		this.beforeNode = lastNode;
	}

	@Override
	public void processSubmatch() {

	}

	@Override
	public void addAggNode(AggNode aggNode) {
		if (this.aggNodes.size() > 0
				&& this.aggNodes.get(this.aggNodes.size() - 1).equals(aggNode)) {
			this.aggNodes.remove(this.aggNodes.size() - 1);
		}
		this.aggNodes.add(aggNode);
	}

	@Override
	public void addAggNodes(AggConnection bestConn) {
		this.addAggNode(bestConn.getFrom());
		this.addAggNode(bestConn.getTo());
	}

	@Override
	public void addAggNodes(List<AggNode> aggNodes) {
		int i = 0;
		while (aggNodes.size() > i
				&& this.aggNodes.size() > 0
				&& this.aggNodes.get(this.aggNodes.size() - 1).equals(
						aggNodes.get(i))) {
			this.aggNodes.remove(this.aggNodes.size() - 1);
			i++;
		}
		this.aggNodes.addAll(aggNodes);
	}

	@Override
	public void addGPSPoint(GPSPoint gpsPoint) {
		if (this.gpsPoints.size() > 0
				&& this.gpsPoints.get(this.gpsPoints.size() - 1).equals(
						gpsPoint)) {
			return;
		}
		this.gpsPoints.add(gpsPoint);
	}

	@Override
	public void addGPSPoints(GPSEdge currentEdge) {
		this.addGPSPoint(currentEdge.getFrom());
		this.addGPSPoint(currentEdge.getTo());
	}

	@Override
	public void addGPSPoints(List<GPSPoint> gpsPoints) {
		int i = 0;
		while (gpsPoints.size() > i
				&& this.gpsPoints.size() > 0
				&& this.gpsPoints.get(this.gpsPoints.size() - 1).equals(
						gpsPoints.get(i))) {
			this.gpsPoints.remove(this.gpsPoints.size() - 1);
			i++;
		}
		this.gpsPoints.addAll(gpsPoints);
	}

	@Override
	public IMergeHandler getCopy() {
		IterativeClosestPointMerge object = new IterativeClosestPointMerge();
		object.aggContainer = this.aggContainer;
		object.maxLookahead = this.maxLookahead;
		object.minContinuationAngle = this.minContinuationAngle;
		object.maxPointGhostDist = this.maxPointGhostDist;
		return object;
	}

	@Override
	public AggContainer getAggContainer() {
		return aggContainer;
	}

	@Override
	public void setAggContainer(AggContainer aggContainer) {
		this.aggContainer = aggContainer;
	}
	
	@Override
	public String toString() {
		StringBuilder gps = new StringBuilder();
		for (GPSPoint point : gpsPoints) {
			gps.append(point).append(", ");
		}
		StringBuilder agg = new StringBuilder();
		for (AggNode node : aggNodes) {
			agg.append(node).append(", ");
		}
		return String.format("MergeHandler:\n\tGPS: %s\n\tAgg: %s", gps, agg);
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this, Arrays.asList(new String[] {
				"aggContainer", "distance", "rdpf" })));
		result.add(new ClassObjectEditor(this.rdpf));
		return result;
	}

}
