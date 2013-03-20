package de.fub.agg2graph.agg.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.IMergeHandler;
import de.fub.agg2graph.agg.ITraceDistance;
import de.fub.agg2graph.structs.ClassObjectEditor;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.Pair;
import de.fub.agg2graph.structs.frechet.BidirectionalFrechetDistance;
import de.fub.agg2graph.structs.frechet.FrechetDistance;
import de.fub.agg2graph.structs.frechet.IAggregatedMap;
import de.fub.agg2graph.structs.frechet.ITrace;

public class FreeSpaceMatch implements ITraceDistance {

	public static AggContainer aggContainer;
	public IAggregatedMap map;
	public ITrace trace;
	public ILocation start;

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

	public Collection<Pair<List<AggConnection>, List<GPSEdge>>> match(
			IAggregatedMap map, ITrace trace, ILocation start, double epsilon) {
		BidirectionalFrechetDistance bfd = new BidirectionalFrechetDistance();
		bfd.setEpsilon(epsilon);
		final TracePather tp = new TracePather(trace, start, bfd);
		final MapPather mp = new MapPather(map, start, bfd);

		// Set up the initial path to match against.
		if (!tp.initialNext()) {
			if (!tp.initialPrevious()) {
				// give up

				return Collections.emptyList();
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

		int minLength = 5; // TODO
		if (result.first().size() >= minLength
				&& result.second().size() >= minLength) {
			Collection<Pair<List<AggConnection>, List<GPSEdge>>> list = new ArrayList<>();
			list.add(result);

			FrechetDistance fd = new FrechetDistance();
			List<GPSPoint> tpPoint = new ArrayList<GPSPoint>();
			for (GPSEdge t : tp.path)
				tpPoint.add(t.getFrom());
			tpPoint.add(tp.path.get(tp.path.size() - 1).getTo());

			List<AggNode> mpPoint = new ArrayList<AggNode>();
			for (AggConnection m : mp.path)
				mpPoint.add(m.getFrom());
			mpPoint.add(mp.path.get(mp.path.size() - 1).getTo());

			System.out.printf("FD: %.8f Soll: %.8f\n",
					fd.getDistance(mp.path, tp.path), epsilon);
			return list;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public Object[] getPathDifference(List<AggNode> aggPath,
			List<GPSPoint> tracePoints, int startIndex, IMergeHandler dmh) {
		Collection<Pair<List<AggConnection>, List<GPSEdge>>> res = match(map, trace, start, 0.0006);
		
		@SuppressWarnings({ "unchecked" })
		Pair<List<AggConnection>, List<GPSEdge>>[] result = new Pair[res.size()];
		int counter = 0;
		Iterator<Pair<List<AggConnection>, List<GPSEdge>>> itRes = res.iterator();
		while(itRes.hasNext()) {
			result[counter++] = itRes.next();
		}
		
		return result;
	}

	@Override
	public List<ClassObjectEditor> getSettings() {
		List<ClassObjectEditor> result = new ArrayList<ClassObjectEditor>();
		result.add(new ClassObjectEditor(this));
		return result;
	}
}
