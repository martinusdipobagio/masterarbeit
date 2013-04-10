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
import de.fub.agg2graph.structs.Pair;
import de.fub.agg2graph.ui.gui.Layer;
import de.fub.agg2graph.ui.gui.RenderingOptions;
import de.fub.agg2graph.ui.gui.TestUI;

public class AttractionForceMerge implements IMergeHandler {

	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.default.merge");

	// contains only matched points/nodes
	private List<AggNode> aggNodes = null;
	private List<GPSPoint> gpsPoints = null;
	public int maxLookahead = 4;
	public double minContinuationAngle = 45;
	// helper stuff
	private List<PointGhostPointPair> pointGhostPointPairs = new ArrayList<PointGhostPointPair>();

	private AggNode inNode;
	private AggNode outNode;

	private AggContainer aggContainer;
	private RenderingOptions roMatchGPS;
	// cleaning stuff
	private RamerDouglasPeuckerFilter rdpf = new RamerDouglasPeuckerFilter(0,
			125);
	// private static AggCleaner cleaner = new AggCleaner().enableDefault();
	public double maxPointGhostDist = 40; // meters

	private double distance = 0;
	@SuppressWarnings("unused")
	private AggNode beforeNode;

	public double delta = 0.003;
	private final int k = 3;
	private AttractionValue av = new AttractionValue();

	public AttractionForceMerge() {
		// debugging
		logger.setLevel(Level.ALL);
		roMatchGPS = new RenderingOptions();
		roMatchGPS.color = Color.PINK;
		logger.setLevel(Level.OFF);

		aggNodes = new ArrayList<AggNode>();
		gpsPoints = new ArrayList<GPSPoint>();
	}

	public AttractionForceMerge(AggContainer aggContainer) {
		this();

		this.aggContainer = aggContainer;
	}

	public AttractionForceMerge(AggContainer aggContainer,
			List<AggNode> aggNodes, List<GPSPoint> gpsPoints) {
		this.aggNodes = aggNodes;
		this.gpsPoints = gpsPoints;
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
		return this.aggNodes;
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
	public List<GPSPoint> getGpsPoints() {
		return gpsPoints;

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
	public void addGPSPoint(GPSPoint gpsPoint) {
		if (this.gpsPoints.size() > 0
				&& this.gpsPoints.get(this.gpsPoints.size() - 1).equals(
						gpsPoint)) {
			return;
		}
		this.gpsPoints.add(gpsPoint);
	}

	@Override
	public void processSubmatch() {
		List<AggNode> internalAggNodes = new ArrayList<AggNode>(aggNodes);
		List<GPSPoint> internalGPSPoint = new ArrayList<GPSPoint>(gpsPoints);
		Pair<AggNode, AggNode> pairAgg = null;
		Pair<GPSPoint, GPSPoint> pairTraj = null;
		double current, best = Double.MAX_VALUE;
		int bestI = Integer.MAX_VALUE;
		// get the nearest edges
		if (internalAggNodes.size() < 3 || internalGPSPoint.size() < 2)
			return;
		for (int h = -1; h < internalAggNodes.size() - 1; h++) {
			bestI = 0;
			best = Double.MAX_VALUE;
			if (!(h == -1 || h == internalAggNodes.size() - 2)) {
				for (int i = 0; i < internalGPSPoint.size() - 1; i++) {
					current = GPSCalc.getDistancePointToEdgeMeter(
							internalAggNodes.get(h + 1),
							internalGPSPoint.get(i),
							internalGPSPoint.get(i + 1));
					if (current < best && GPSCalc.getProjectionPoint(internalAggNodes.get(h + 1),
							internalGPSPoint.get(i),
							internalGPSPoint.get(i + 1)) != null) {
						best = current;
						bestI = i;
					}
				}
				if (best < Double.MAX_VALUE) {
					pairAgg = new Pair<AggNode, AggNode>(
							internalAggNodes.get(h),
							internalAggNodes.get(h + 2));
					pairTraj = new Pair<GPSPoint, GPSPoint>(
							internalGPSPoint.get(bestI),
							internalGPSPoint.get(bestI + 1));
				}
				if (pairAgg != null && pairTraj != null)
					pointGhostPointPairs.add(PointGhostPointPair
							.createAttraction(internalAggNodes.get(h + 1),
									pairAgg, pairTraj, 0));
			} else {
				int temp = h == -1 ? 0 : internalAggNodes.size() - 1;

				for (int i = 0; i < internalGPSPoint.size(); i++) {
					current = GPSCalc
							.getDistanceTwoPointsMeter(
									internalAggNodes.get(temp),
									internalGPSPoint.get(i));
					if (current < best) {
						best = current;
						bestI = i;
					}
				}

				pointGhostPointPairs
						.add(PointGhostPointPair.createAttraction(
								internalAggNodes.get(temp),
								internalGPSPoint.get(bestI)));
			}
		}
	}

	public Pair<GPSPoint, GPSPoint> getBestEdge(AggNode node, GPSPoint start,
			GPSPoint end) {
		return null;
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

	private void showDebugInfo() {
		TestUI ui = (TestUI) Globals.get("ui");
		if (ui == null) {
			return;
		}
		Layer matchingLayer = ui.getLayerManager().getLayer("matching");
		Layer mergingLayer = ui.getLayerManager().getLayer("merging");
		// clone the lists
		List<ILocation> aggNodesClone = new ArrayList<ILocation>(
				aggNodes.size());
		for (ILocation loc : aggNodes) {
			aggNodesClone.add(new GPSPoint(loc));
		}
		matchingLayer.addObject(aggNodesClone);
		matchingLayer.addObject(gpsPoints); // , roMatchGPS);

		for (PointGhostPointPair pgpp : pointGhostPointPairs) {
			if (!pgpp.isEnd) {
				List<ILocation> line = new ArrayList<ILocation>(2);
				List<ILocation> line2 = new ArrayList<ILocation>(2);
				line.add(new GPSPoint(pgpp.source));
				line.add(new GPSPoint(pgpp.pairTraj.a));
				line2.add(new GPSPoint(pgpp.source));
				line2.add(new GPSPoint(pgpp.pairTraj.b));
				mergingLayer.addObject(line);
				mergingLayer.addObject(line2);
			} else {
				List<ILocation> line = new ArrayList<ILocation>(2);
				line.add(new GPSPoint(pgpp.source));
				line.add(new GPSPoint(pgpp.proj));
				mergingLayer.addObject(line);

			}
		}
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
		if (getAggNodes().size() < 3 || getGpsPoints().size() < 2)
			return;
		showDebugInfo();

		// add nodes
		AggNode lastNode = null;
		AggConnection conn = null;
		for (AggNode node : getAggNodes()) {
			if (lastNode == null) {
				lastNode = node;
				continue;
			}

			/* Make sure that they are connected */
			conn = lastNode.getConnectionTo(node);
			if (conn == null) {
				continue;
			}
			// aConn.add(new AggConnection(lastNode, node, aggContainer));
			conn.tryToFill();

			lastNode = node;
		}

		for (PointGhostPointPair pgpp : pointGhostPointPairs) {
			if (!pgpp.isEnd)
				attractionForce(pgpp.source, pgpp.pairAgg.a, pgpp.pairAgg.b,
						pgpp.pairTraj.a, pgpp.pairTraj.b);
			else
				attractionForce(pgpp.source, pgpp.proj);
		}
	}

	// AttractionForce Parameter
	// double M = 1;
	// double N = 20;
	// double x = 0;
	// double y = 0;
	// double s1 = 5;
	// double s2 = 5;
	//

	public void attractionForce(AggNode currentNode, GPSPoint projection) {
		double distance = GPSCalc.getDistanceTwoPointsMeter(currentNode,
				projection);
//		 System.out.println("Att-Value = " + aValue);
//		 System.out.println("Distance  = " + distance);
		Double aValue = av.getValue(distance);
		if (aValue != null) {
			ILocation newPos = GPSCalc.getPointAt((av.getKey(distance) - aValue) / av.getKey(distance),
					currentNode, projection);
			aggContainer.moveNodeTo(currentNode, newPos);
		}
	}

	public void attractionForce(AggNode currentNode, AggNode before,
			AggNode after, GPSPoint trajStart, GPSPoint trajEnd) {
		// TODO Null gefahr
		double angle = GPSCalc.getAngleBetweenEdges(before, after, trajStart,
				trajEnd);
//		System.out.println("Angle : " + angle);
//		System.out.println("Cos   : " + Math.cos(angle));
//		if (angle > 45
//				|| Math.cos(angle) < 0)
//				|| GPSCalc.getProjectionPoint(currentNode, trajStart, trajEnd) == null) // and
//			// right
//			// side
//			// of
//			return;
		ILocation aggProj = GPSCalc.getProjectionPoint(currentNode, before,
				after);
		if (aggProj == null)
			return;
		ILocation trajProj = GPSCalc.intersection(currentNode, aggProj,
				trajStart, trajEnd);
		if (trajProj == null)
			return;
		double distance = GPSCalc.getDistanceTwoPointsMeter(currentNode,
				trajProj);
		Double aValue = av.getValue(distance);
//		System.out.println(currentNode);
//		System.out.println("Att-Value = " + aValue);
//		System.out.println("Distance  = " + distance);
		if (aValue != null) {
			ILocation newPos = GPSCalc.getPointAt((av.getKey(distance) - aValue) / av.getKey(distance),
					currentNode, trajProj);
			// System.out.println("Current Node : " + currentNode.getLat() + "|"
			// +
			// currentNode.getLon());
			// System.out.println("New Node     : " + newPos);
			aggContainer.moveNodeTo(currentNode, newPos);
		}
	}

	@Override
	public AggNode getInNode() {
		return inNode;
	}

	@Override
	public AggNode getOutNode() {
		return outNode;
	}

	// TODO FAUL
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
		AttractionForceMerge object = new AttractionForceMerge();
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
