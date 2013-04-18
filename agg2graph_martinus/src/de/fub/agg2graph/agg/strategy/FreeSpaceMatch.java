package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.input.Trace;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.frechet.BidirectionalFrechetDistance;
import de.fub.agg2graph.structs.frechet.FrechetDistance;
import de.fub.agg2graph.structs.frechet.IAggregatedMap;
import de.fub.agg2graph.structs.frechet.ITrace;
import de.fub.agg2graph.structs.frechet.Pair;
import de.fub.agg2graph.structs.frechet.TreeAggMap;

public class FreeSpaceMatch implements ITraceDistance {

	public double aggReflectionFactor = 4;
	public int maxOutliners = 10;
	public double maxDistance = 0.003;
	public int maxLookahead = 4;
	public double maxPathDifference = 100;
	public int minLengthFirstSegment = 1;
	public double maxAngle = 37;

	public static AggContainer aggContainer;
	public IAggregatedMap map;
	public ITrace trace;
	public ILocation start;

	public double bestValue;

	interface Pather {
		double expectedfd();

		boolean consume();
	}

	/**
	 * This class is responsible to calculate the expected changes to the
	 * frechet distance while adding a possible extension of the matched map
	 * path.
	 * 
	 */
	static class MapPather implements Pather {

		IAggregatedMap map;
		ILocation nl;
		ILocation pl;
		List<AggConnection> path = new ArrayList<AggConnection>();
		private BidirectionalFrechetDistance bfd;
		private AggConnection proposedConsumee = null;

		MapPather(IAggregatedMap map, ILocation start,
				BidirectionalFrechetDistance bfd) {
			this.map = map;
			this.nl = map.searchNN(new AggNode(start, aggContainer));
			this.pl = nl;
			this.bfd = bfd;
		}

		/**
		 * Calculate the expected frechet distances while return the minimum of
		 * all choices.
		 */
		public double expectedfd() {
			Collection<AggConnection> ncandidates = map.getOutConnections(
					(AggNode) nl, null);
			Collection<AggConnection> pcandidates = map.getInConnections(
					(AggNode) pl, null);
			double min = Double.POSITIVE_INFINITY;

			for (AggConnection n : ncandidates) {
				bfd.appendToP(n);
				double appx = bfd.approximateDistance();
				bfd.removeLastOfP();
				if (appx < min) {
					min = appx;
					proposedConsumee = n;
				}
			}

			for (AggConnection p : pcandidates) {
				bfd.prependToP(p);
				double appx = bfd.approximateDistance();
				bfd.removeFirstOfP();
				if (appx < min) {
					min = appx;
					proposedConsumee = p;
				}
			}

			return min;
		}

		/**
		 * Extend the path by the edge calculated with expectedfd.
		 * 
		 * expectedfd needs to be called first.
		 */
		public boolean consume() {
			if (proposedConsumee != null) {
				if (proposedConsumee.getTo().compareTo((GPSPoint) pl) == 0) {
					bfd.prependToP(proposedConsumee);
					if (bfd.isInDistance()) {
						path.add(0, proposedConsumee);
						pl = proposedConsumee.getFrom();
					} else {
						bfd.removeFirstOfP();
						return false;
					}

				} else if (proposedConsumee.getFrom().compareTo((GPSPoint) nl) == 0) {
					bfd.appendToP(proposedConsumee);
					if (bfd.isInDistance()) {
						path.add(proposedConsumee);
						nl = proposedConsumee.getTo();
					} else {
						bfd.removeLastOfP();
						return false;
					}
				}
			}
			return false;
		}

		public List<AggConnection> getPath() {
			return path;
		}
	}

	/**
	 * Hold the trace path, calculate the expected frechet distance by either
	 * appending or prepending the edges.
	 * 
	 */
	static class TracePather implements Pather {
		List<GPSEdge> path = new ArrayList<GPSEdge>();
		private BidirectionalFrechetDistance bfd;
		ListIterator<GPSEdge> forward;
		ListIterator<GPSEdge> backward;

		GPSEdge next = null;
		GPSEdge previous = null;
		private boolean proposeForward;

		TracePather(ITrace trace, ILocation start,
				BidirectionalFrechetDistance bfd) {
			this.bfd = bfd;
			this.forward = trace.edgeListIterator(start);
			this.backward = trace.edgeListIterator(start);
		}

		public boolean initialNext() {
			if (forward.hasNext()) {
				GPSEdge test = forward.next();
				bfd.appendToQ(test);
				next = test;
				path.add(test);
				return true;
			}

			next = null;
			return false;
		}

		public boolean initialPrevious() {
			if (backward.hasPrevious()) {
				GPSEdge test = backward.previous();
				bfd.prependToQ(test);
				previous = test;
				path.add(0, test);
				return true;
			}

			previous = null;
			return false;
		}

		public double expectedfd() {
			double deltab = Double.POSITIVE_INFINITY;
			double deltaf = Double.POSITIVE_INFINITY;

			if (forward.hasNext()) {
				bfd.appendToQ(forward.next());
				deltaf = bfd.approximateDistance();
				bfd.removeLastOfQ();
				forward.previous();
			}
			if (backward.hasPrevious()) {
				bfd.prependToQ(backward.previous());
				deltab = bfd.approximateDistance();
				bfd.removeFirstOfQ();
				backward.next();
			}

			if (deltaf < deltab) {
				proposeForward = true;
			} else {
				proposeForward = false;
			}

			return Math.min(deltaf, deltab);
		}

		public boolean consume() {
			if (proposeForward) {
				if (forward.hasNext()) {
					GPSEdge test = forward.next();
					bfd.appendToQ(test);
					if (bfd.isInDistance()) {
						next = test;
						path.add(test);
						return true;
					} else {
						bfd.removeLastOfQ();
						forward.previous();
					}
				}
				next = null;
				return false;
			} else {
				if (backward.hasPrevious()) {
					GPSEdge test = backward.previous();
					bfd.prependToQ(test);
					if (bfd.isInDistance()) {
						previous = test;
						path.add(0, test);
						return true;
					} else {
						bfd.removeFirstOfQ();
						backward.next();
					}
				}
				previous = null;
				return false;
			}
		}

		List<GPSEdge> getPath() {
			return path;
		}
	}

	public Pair<List<AggConnection>, List<GPSEdge>> match(IAggregatedMap map,
			ITrace trace, ILocation start, double epsilon) {
		BidirectionalFrechetDistance bfd = new BidirectionalFrechetDistance(
				epsilon);
		bfd.setEpsilon(epsilon);
		final TracePather tp = new TracePather(trace, start, bfd);
		final MapPather mp = new MapPather(map, start, bfd);

		// Set up the initial path to match against.
		if (!tp.initialNext()) {
			if (!tp.initialPrevious()) {
				// give up

				return null;
			}
		}
		mp.expectedfd();
		mp.consume();

		// Further extend the paths as long as the criteria are matched.
		boolean proceed = true;

		while (proceed) {
			double dm = mp.expectedfd();
			double dt = tp.expectedfd();
			if (dm <= dt) {
				if (!mp.consume()) {
					proceed = tp.consume();
				} else {
					proceed = true;
				}
			} else {
				if (!tp.consume()) {
					proceed = mp.consume();
				} else {
					proceed = true;
				}
			}

			if (bfd.approximateDistance() > epsilon) {
				break;
			}
		}

		// TODO
		Pair<List<AggConnection>, List<GPSEdge>> result = new Pair<List<AggConnection>, List<GPSEdge>>(
				mp.getPath(), tp.getPath());
		// if(visitor != null) {
		// visitor.visit(result);
		// }

		// int minLength = 5; // TODO
		// if (result.first().size() >= minLength
		// && result.second().size() >= minLength) {
		// Collection<Pair<List<AggConnection>, List<GPSEdge>>> list = new
		// ArrayList<>();
		// list.add(result);

		// TODO bestValue
		FrechetDistance fd = new FrechetDistance(maxDistance);
		List<GPSPoint> tpPoint = new ArrayList<GPSPoint>();
		for (GPSEdge t : tp.path)
			tpPoint.add(t.getFrom());
		tpPoint.add(tp.path.get(tp.path.size() - 1).getTo());

		List<AggNode> mpPoint = new ArrayList<AggNode>();
		for (AggConnection m : mp.path)
			mpPoint.add(m.getFrom());
		mpPoint.add(mp.path.get(mp.path.size() - 1).getTo());

		System.out.printf("FD: %.8f Soll: %.8f\n",
				this.bestValue = fd.getDistance(mp.path, tp.path), epsilon);
		return result;
		// } else {
		// return Collections.emptyList();
		// }
	}

	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
		double bestValue = Double.MIN_VALUE;
		double bestValueLength = 0;

		List<AggNode> aggResult = new ArrayList<AggNode>();
		List<GPSPoint> traceResult = new ArrayList<GPSPoint>();

		List<AggNode> aggLocations = aggPath;
//		System.out.println("AGG-Before : " + aggPath);
		/* TEST */
		// List<GPSPoint> traceLocations = tracePoints.subList(startIndex,
		// tracePoints.size());
		/* UNSAFE */
		AggNode lastAgg = aggLocations.get(aggLocations.size() - 1);
		double bestLastDistance = Double.MAX_VALUE;
		int bestI = -1;
		for (int i = startIndex; i < tracePoints.size(); i++) {
			if (bestLastDistance > GPSCalc.getDistanceTwoPointsDouble(lastAgg,
					tracePoints.get(i))) {
				bestI = i;
				bestLastDistance = GPSCalc.getDistanceTwoPointsDouble(lastAgg,
						tracePoints.get(i));
			}
		}
		List<GPSPoint> traceLocations = (bestI > -1) ? tracePoints.subList(
				startIndex, bestI + 1) : tracePoints.subList(startIndex,
				tracePoints.size());

		map = new TreeAggMap(aggContainer);
		AggNode last = null;
		for (AggNode node : aggLocations) {
			if (last != null)
				map.insertConnection(new AggConnection(last, node, aggContainer));
			last = node;
		}

		start = traceLocations.get(0);
		trace = new Trace();
		for (int i = 0; i < traceLocations.size(); i++) {
			trace.insertEdgeLocation(i, traceLocations.get(i));
		}
		// TODO null gefahr
		Pair<List<AggConnection>, List<GPSEdge>> res = match(map, trace, start,
				maxDistance);
		bestValue = this.bestValue;

		if (res == null)
			return null;

		for (int i = 0; i < res.a.size(); i++) {
			aggResult.add(res.a.get(i).getFrom());
		}
		aggResult.add(res.a.get(res.a.size() - 1).getTo());

		for (int i = 0; i < res.b.size(); i++) {
			traceResult.add(res.b.get(i).getFrom());
		}
		traceResult.add(res.b.get(res.b.size() - 1).getTo());

		bestValueLength = aggResult.size();
//		System.out.println("AGG-After : " + aggResult);
		return new Object[] { bestValue, bestValueLength, aggResult,
				traceResult };
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}
}
