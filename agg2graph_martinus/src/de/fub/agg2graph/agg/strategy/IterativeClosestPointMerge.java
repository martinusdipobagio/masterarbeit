package de.fub.agg2graph.agg.strategy;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.PointGhostPointPair;
import de.fub.agg2graph.graph.RamerDouglasPeuckerFilter;
import de.fub.agg2graph.input.Globals;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.TreeAggMap;
import de.fub.agg2graph.ui.gui.Layer;
import de.fub.agg2graph.ui.gui.RenderingOptions;
import de.fub.agg2graph.ui.gui.TestUI;

public class IterativeClosestPointMerge implements IMergeHandler {

	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.default.merge");

	//contains only matched points/nodes
	private List<List<AggNode>> aggNodes = null;
	private List<List<GPSPoint>> gpsPoints = null;
	int max = 0;
	public int maxLookahead = 4;
	public double minContinuationAngle = 45;
	// helper stuff
//	private Map<AggNode, List<GPSPoint>> kNeighbours = new HashMap<AggNode, List<GPSPoint>>();
	private List<PointGhostPointPair> pointGhostPointPairs = new ArrayList<PointGhostPointPair>();

	private AggNode inNode;
	private AggNode outNode;

	private AggContainer aggContainer;
	private RenderingOptions roMatchGPS;
	// cleaning stuff
	private RamerDouglasPeuckerFilter rdpf = new RamerDouglasPeuckerFilter(0,
			125);
//	private static AggCleaner cleaner = new AggCleaner().enableDefault();
	public double maxPointGhostDist = 40; // meters

	private double distance = 1000;
	@SuppressWarnings("unused")
	private AggNode beforeNode;
	
	public double delta = 0.0001;
	private final int k = 1;
	public TreeAggMap map = null;

	public IterativeClosestPointMerge() {
		// debugging
		logger.setLevel(Level.ALL);
		roMatchGPS = new RenderingOptions();
		roMatchGPS.color = Color.PINK;
		logger.setLevel(Level.OFF);

		aggNodes = new ArrayList<List<AggNode>>();
		gpsPoints = new ArrayList<List<GPSPoint>>();
	}

	public IterativeClosestPointMerge(AggContainer aggContainer) {
		this();

		this.aggContainer = aggContainer;
	}

	public IterativeClosestPointMerge(AggContainer aggContainer,
			List<AggNode> aggNodes, List<GPSPoint> gpsPoints) {
		this.aggNodes.add(aggNodes);
		this.gpsPoints.add(gpsPoints);
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
	public List<AggNode> getAggNodes() {
		//TODO
		return getAggNodes(0);
	}

	public List<AggNode> getAggNodes(int i) {
		return this.aggNodes.get(i);
	}

	//TODO Not sure ...
	@Override
	public void addAggNode(AggNode aggNode) {
		List<AggNode> agg = new ArrayList<AggNode>();
		agg.add(aggNode);
		this.aggNodes.add(agg);
	}

	//TODO To test
	@Override
	public void addAggNodes(List<AggNode> aggNodes) {
		this.aggNodes.add(aggNodes);
	}

	@Override
	public List<GPSPoint> getGpsPoints() {
		//TODO change the caller
		return this.getGpsPoints(0);
	}

	@Override
	public List<GPSPoint> getGpsPoints(int i) {
		return this.gpsPoints.get(i);
	}

	//TODO To test
	@Override
	public void addGPSPoints(List<GPSPoint> gpsPoints) {
		this.gpsPoints.add(gpsPoints);
	}

	@Override
	public void addGPSPoint(GPSPoint gpsPoint) {
		List<GPSPoint> tra = new ArrayList<GPSPoint>();
		tra.add(gpsPoint);
		this.gpsPoints.add(tra);
	}

	@Override
	public void processSubmatch() {
		this.max = aggNodes.size();
		for(int x = 0; x < this.max; x++) {
			List<AggNode> internalAggNodes = new ArrayList<AggNode>(aggNodes.get(x));
			for(AggNode node : internalAggNodes) {
				List<GPSPoint> neighbour = getKSmallest(gpsPoints.get(x), node, k);
				if(neighbour.size() > 0)
					pointGhostPointPairs.add(PointGhostPointPair.createIterative(node, neighbour, 0));
//				//neighbour > 0
//				if(neighbour.size() > 0 && !kNeighbours.containsKey(node)) {
//					kNeighbours.put(node, neighbour);
////					System.out.println(node + " => " + neighbour);
//				}
			}
		}
	}
	
	private static List<GPSPoint> getKSmallest(List<GPSPoint> trace, AggNode from, int k) {
		double currentMaxDistance = 0;
		GPSPoint currentMax = null;
		if(trace.size() == 0)
			return null;
		else if(trace.size() <= k)
			return trace;
		else {
			List<GPSPoint> dist = new ArrayList<GPSPoint>(k);
			for(GPSPoint p : trace) {
				if(dist.size() < k) {
					dist.add(p);
				} else {
					currentMaxDistance = GPSCalc.getDistanceTwoPointsMeter(from, p);
					currentMax = p;
					for(GPSPoint d : dist) {
						if(currentMaxDistance < GPSCalc.getDistanceTwoPointsMeter(from, d)) {
							currentMaxDistance = GPSCalc.getDistanceTwoPointsMeter(from, d);
							currentMax = d;
						}
					}
					if(dist.contains(currentMax)) {
						dist.remove(currentMax);
						dist.add(p);
					}
				}
			}
			return dist;
		}
	}

	@SuppressWarnings("unused")
	private ILocation testLength(GPSPoint point, ILocation newPoint) {
		if (newPoint != null) {
			if (GPSCalc.getDistanceTwoPointsMeter(point, newPoint) > maxPointGhostDist) {
				return null;
			}
		}
		return newPoint;
	}

	private void showDebugInfo(int x) {
		TestUI ui = (TestUI) Globals.get("ui");
		if (ui == null) {
			return;
		}
		Layer matchingLayer = ui.getLayerManager().getLayer("matching");
		Layer mergingLayer = ui.getLayerManager().getLayer("merging");
		// clone the lists
		List<ILocation> aggNodesClone = new ArrayList<ILocation>(
				aggNodes.get(x).size());
		for (ILocation loc : aggNodes.get(x)) {
			aggNodesClone.add(new GPSPoint(loc));
		}
		matchingLayer.addObject(aggNodesClone);
		matchingLayer.addObject(gpsPoints.get(x)); // , roMatchGPS);
		
		for (PointGhostPointPair pgpp : pointGhostPointPairs) {
			List<ILocation> line = new ArrayList<ILocation>(2);
			line.add(new GPSPoint(pgpp.source));
			for(int j = 0; j < pgpp.ghostPoints.size(); j++) {
				line.add(new GPSPoint(pgpp.ghostPoints.get(j)));
				mergingLayer.addObject(line);
				line = new ArrayList<ILocation>(2);
				line.add(new GPSPoint(pgpp.source));
			}
		}
//		System.out.println(kNeighbours);
//		for(int i = 0; i < aggNodes.get(x).size(); i++) {
//			System.out.println("current Node : " + currentNode);
			
//			if(kNeighbours.get(currentNode) != null) {
//				for(GPSPoint p : kNeighbours.get(currentNode)) {
//					List<ILocation> line = new ArrayList<ILocation>(2);
//					System.out.println(currentNode + " <-> " + p);
//					line.add(currentNode);
//					line.add(p);
//					mergingLayer.addObject(line);
//				}
//			}
//		}
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
	public void mergePoints() {
		this.max = aggNodes.size();
		for(int x = 0; x < max; x++) {
			showDebugInfo(x);
			
			
			
			// add nodes
			AggNode lastNode = null;
			AggConnection conn = null;
			for (AggNode node : getAggNodes(x)) {
				if (lastNode == null) {
					lastNode = node;
					continue;
				}

				/* Make sure that they are connected */
				conn = lastNode.getConnectionTo(node);
				if (conn == null) {
					continue;
				}
//				aConn.add(new AggConnection(lastNode, node, aggContainer));
				conn.tryToFill();
				
				lastNode = node;
			}
			
			for (PointGhostPointPair pgpp : pointGhostPointPairs) {
				closestPointsMerge(map, pgpp.source, pgpp.ghostPoints);
			}
		}
	}
	
	public void closestPointsMerge(TreeAggMap map, AggNode a,
			List<GPSPoint> ts) {
		
//		System.out.println("A        = " + a.getLat() + " <> " + a.getLon());
		for(int i = 0; i < ts.size(); i++) {
//			System.out.println("Neigh " + i + "   = " + ts.get(i).getLat() + " <> " + ts.get(i).getLon());
		}
		AggNode to = GPSCalc.CalculateMean(a, ts, delta, aggContainer);
		AggNode toCopy = new AggNode(to, aggContainer);
//		GPSCalc.moveLocation(map, a, toCopy, aggContainer);
		aggContainer.moveNodeTo(a, toCopy);
//		System.out.println("t         = " + to.getLat() + " <> " + to.getLon());
	}

	@Override
	public AggNode getInNode() {
		return inNode;
	}

	@Override
	public AggNode getOutNode() {
		return outNode;
	}

	//TODO FAUL
	@Override
	public String toString() {
		StringBuilder gps = new StringBuilder();
		for (List<GPSPoint> point : gpsPoints) {
			gps.append(point).append(", ");
		}
		StringBuilder agg = new StringBuilder();
		for (List<AggNode> node : aggNodes) {
			agg.append(node).append(", ");
		}
		return String.format("MergeHandler:\n\tGPS: %s\n\tAgg: %s", gps, agg);
	}

	@Override
	public boolean isEmpty() {
		return gpsPoints.size() == 0 && gpsPoints.size() == 0;
	}

	@Override
	public void setBeforeNode(AggNode lastNode) {
		this.beforeNode = lastNode;
	}

	@Override
	public void addAggNodes(AggConnection bestConn) {
		List<AggNode> agg = new ArrayList<AggNode>();
		agg.add(bestConn.getFrom());
		agg.add(bestConn.getTo());
		addAggNodes(agg);
	}

	@Override
	public void addGPSPoints(GPSEdge edge) {
		List<GPSPoint> tra = new ArrayList<GPSPoint>();
		tra.add(edge.getFrom());
		tra.add(edge.getTo());
		addGPSPoints(tra);
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
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this, Arrays.asList(new String[] {
				"aggContainer", "distance", "rdpf" })));
		result.add(new ClassObjectEditor(this.rdpf));
		return result;
	}
}
