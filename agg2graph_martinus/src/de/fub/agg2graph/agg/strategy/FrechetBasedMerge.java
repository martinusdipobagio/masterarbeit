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
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.IAggregatedMap;
import de.fub.agg2graph.structs.frechet.TreeAggMap;
import de.fub.agg2graph.ui.gui.RenderingOptions;

public class FrechetBasedMerge implements IMergeHandler {

/*
 // Convert to vectors for frechet input.
		Vector<Edge> Av = new Vector<>();
		Av.addAll(A);
		Vector<Edge> Tv = new Vector<>();
		Tv.addAll(T);
		
		//Value of epsilon not relevant in our use case.
		FrechetDistance fd = new FrechetDistance(Av, Tv, 400.);
		// Compute critical values and the meta information needed by our algorithm.
		double epsilon = fd.computeEpsilon();
		System.out.println("FrechetBasedMerge: Use epsilon of: " + epsilon + "  isOk " + fd.isInDistance());
		fd.computeMetaData();
		// Map points from A to T
		HashMap<Location, TreeSet<Location>> AtoT = new HashMap<>();
		for(Entry<Integer, TreeSet<Location>> entry : fd.fromP.entrySet()) {
			int i = entry.getKey();
			if(i < fd.P.size()) {
				AtoT.put(fd.P.get(i).getFrom(), entry.getValue());
			} else if(i == fd.P.size()) {
				AtoT.put(fd.P.get(i - 1).getTo(), entry.getValue());
			}
		}
		
		// Move the aggregate by the mean of the segments introduced by Frechet critical values. See paperwork for clarification.
		for(AggregatedEdge edge : A) {
			int n = edge.getNumbers();
			Location locationToMove = edge.getFrom();
			if(AtoT.containsKey(locationToMove)) {
				TreeSet<Location> affectedTraceLocations = AtoT.get(locationToMove);
				if(affectedTraceLocations != null) {
					
					// Save meta information for display in the gui.
					for(Location ti : affectedTraceLocations){
						double dist = locationToMove.getDistanceTo(ti);
						if(dist <= epsilon) {
							criticalEdges.add(new Edge(locationToMove, ti, 0));
//							System.err.printf("\\draw[dashed,thin] (%.8f, %.8f) to (%.8f, %.8f);\n",
//									locationToMove.getLongitude(), locationToMove.getLatitude(),
//									ti.getLongitude(), ti.getLatitude());
						}
					}
					
					Location weightedpos = Algorithms.CalculateMean(locationToMove, affectedTraceLocations, epsilon);
					if(weightedpos.compareTo(locationToMove) != 0) {
						Algorithms.moveLocation(map, edge, weightedpos, locationToMove);
						edge.setNumbers(n + 1);
					}
				}
			}
		}
		
		// Also the last point.
		AggregatedEdge edge = A.getLast();
		Location locationToMove = edge.getTo();
		if(AtoT.containsKey(locationToMove)) {
			TreeSet<Location> affectedTraceLocations = AtoT.get(locationToMove);
			if(affectedTraceLocations != null) {
				
				// Save meta information for display in the gui.
				for(Location ti : affectedTraceLocations){
					double dist = locationToMove.getDistanceTo(ti);
					if(dist <= epsilon) {
						criticalEdges.add(new Edge(locationToMove, ti, 0));
//						System.err.printf("\\draw[dashed,thin] (%.8f, %.8f) to (%.8f, %.8f);\n",
//								locationToMove.getLongitude(), locationToMove.getLatitude(),
//								ti.getLongitude(), ti.getLatitude());
					}
				}
				
				Location weightedpos = Algorithms.CalculateMean(locationToMove, affectedTraceLocations, epsilon);
				Algorithms.moveLocation(map, edge, weightedpos, locationToMove);
			}
		}
	} 
 */
	
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
	
	//AttractionForce Parameter
	double M = 0;
	double N = 0;
	double y = 0;
	double s1 = 0;
	double s2 = 0;
	
	
	@Override
	public void mergePoints() {
		
	}
	
	@SuppressWarnings("unused")
	public void attractionForce(AggNode currentNode, AggNode before, AggNode after, GPSPoint trajStart, GPSPoint trajEnd) {
		//TODO Null gefahr
		ILocation aggProj = GPSCalc.getProjectionPoint(currentNode, before, after);
		ILocation trajProj = GPSCalc.intersection(aggProj, currentNode, trajStart, trajEnd);
		double distance = GPSCalc.getDistanceTwoPointsMeter(currentNode, trajProj);
		double d = (Math.pow(2, 0.5)*M*N*y)/(Math.pow(Math.PI, 0.5)*Math.exp(Math.pow(y, 2)/(2*Math.pow(s1, 2) + 
				2*Math.pow(s2, 2)))*Math.pow((Math.pow(s1, 2) + Math.pow(s2, 2)), 0.5)*(2*Math.pow(s1, 2) + 2*Math.pow(s2, 2)));
		double at = d/distance;
		ILocation newPos = GPSCalc.getPointAt(at, currentNode, trajProj);
		aggContainer.moveNodeTo(currentNode, newPos);
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
		FrechetBasedMerge object = new FrechetBasedMerge();
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

	@Override
	public List<AggNode> getAggNodes(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<GPSPoint> getGpsPoints(int i) {
		// TODO Auto-generated method stub
		return null;
	}
}
