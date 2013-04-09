package de.fub.agg2graph.structs.frechet;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.Interval;
import de.fub.agg2graph.structs.Pair;

/**
 * 
 * @author Martinus
 * 
 */
public class FrechetDistance {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger("agg2graph.agg.frechet.dist");

	public double maxDistance = 60;
	/** Epsilon value */

	/** Angle between Path and Trace */

	public List<AggConnection> P; //p
	/** Supposed to be P */
	public List<GPSEdge> Q; //q
	/** Supposed to be Q */
	private Cell[] cells = null;
	AggContainer aggContainer;

	public FrechetDistance(double maxDistance) {
		P = null;
		Q = null;
		this.maxDistance = maxDistance;
	}
	
	public int getSizeP() {
		return (P != null) ? P.size() : 0;
	}
	
	public int getSizeQ() {
		return (Q != null) ? Q.size() : 0;
	}

	public FrechetDistance(List<GPSEdge> a, List<GPSEdge> t, double epsilon) {
		this.P = new ArrayList<AggConnection>();
		for(GPSEdge agg : a) {
			P.add(new AggConnection(agg.getFrom(), agg.getTo(), aggContainer));
		}
		this.Q = t;
		this.maxDistance = epsilon;
		calculateReachableSpace();
	}

	void updateFreeSpace() {
		for (int i = 0; i < P.size(); ++i) {
			for (int j = 0; j < Q.size(); ++j) {
				getCell(i, j).updateCell();
			}
		}
		calculateReachableSpace();
	}

	public double getEpsilon() {
		return maxDistance;
	}

	public void setEpsilon(double epsilon) {
		this.maxDistance = epsilon;
		updateFreeSpace();
	}

	public class Cell {
		public int i;
		public int j;
		private double a, b, c, d;
		private GPSEdge q;
		private AggConnection p;

		//Extension
		List<Point> all = new ArrayList<Point>();
		List<Point> le = new ArrayList<Point>(); 	//using vertical line
		List<Point> ri = new ArrayList<Point>();	//using vertical line
		List<Point> bo = new ArrayList<Point>(); 	//using horizontal line
		List<Point> to = new ArrayList<Point>();	//using horizontal line
		Integer alls = Integer.MIN_VALUE;
		Integer bots = Integer.MAX_VALUE;
		Integer tops = Integer.MIN_VALUE;
		Integer lef = Integer.MAX_VALUE;
		Integer rig = Integer.MIN_VALUE;
		
		
		/** intervals of the reachable space */
		Interval left;
		Interval bottom;

		public Cell(int i, int j) {
			this.i = i;
			this.j = j;
			p = P.get(i);
			q = Q.get(j);
			updateCell();
		}

		private void updateCell() {
			left = GPSCalc.getSegmentCircleIntersection2(p.getFrom()
					.getLon(), p.getFrom().getLat(),
					p.getTo().getLon(), p.getTo().getLat(), q
							.getFrom().getLon(), q.getFrom().getLat(),
					maxDistance);

			a = left.start;
			b = left.end;

			bottom = GPSCalc.getSegmentCircleIntersection2(q.getFrom()
					.getLon(), q.getFrom().getLat(),
					q.getTo().getLon(), q.getTo().getLat(), p
							.getFrom().getLon(), p.getFrom().getLat(),
					maxDistance);

			c = bottom.start;
			d = bottom.end;
		}

		/** Draw the free space region of the cell with given epsilon. */
		public BufferedImage getFreeSpace(BufferedImage img, int width) {
			BufferedImage buffer = img;
			if (buffer == null) {
				buffer = new BufferedImage(width, width,
						BufferedImage.TYPE_INT_RGB);
			}
			double stepsize = 1.0 / width;

			//Y
			for (int s = 0; s < width; ++s) {
				double sStep = s * stepsize;
				ILocation pAtt = p.at(sStep);
				alls = Integer.MIN_VALUE;
				List<Point> current = new ArrayList<Point>(2);

				//X
				for (int t = 0; t < width; ++t) {
					double tStep = t * stepsize;
					ILocation qAtt = q.at(tStep);

					double distance = GPSCalc.getDistanceTwoPointsDouble(
							pAtt, qAtt);
					
					if(distance < maxDistance) {
						buffer.setRGB(t, width - 1 - s, Color.WHITE.getRGB());
						if(alls == Integer.MIN_VALUE) {
							current.add(new Point(s, t));
							alls = s;
						} else if(alls == s) {
							if(current.size() == 2)
								current.remove(1);
							current.add(new Point(s, t));
								
						}
					} else {
						buffer.setRGB(t, width - 1 - s, Color.BLACK.getRGB());
					}
				}
				all.addAll(current);
			}
			
			Graphics2D g2 = (Graphics2D) buffer.getGraphics();
//			System.out.println("Cell (i, j) = (" + i + ", " + j + ")");
			if(all.size() > 0) {
				g2.setColor(Color.GREEN);

				for(int i = 0; i < all.size(); i++) {
					g2.drawRect(all.get(i).y, width - 1 - all.get(i).x, 1, 1);
				}
			}
			
			return buffer;
		}

		/** Draw the lines marking the cell boundary intervals. */
		public BufferedImage getParameterMarks(BufferedImage img, int width) {
			BufferedImage buffer = img;
			if (buffer == null) {
				buffer = new BufferedImage(width, width,
						BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D g2 = (Graphics2D) buffer.getGraphics();
			g2.clearRect(0, 0, width, width);
			if (a < Double.MAX_VALUE && b > Double.MIN_VALUE) {
				g2.draw(new Line2D.Double(0., width - a * width, 0., width - b
						* width));
			}

			if (c < Double.MAX_VALUE && d > Double.MIN_VALUE) {
				g2.draw(new Line2D.Double(c * width, width - 1, d * width,
						width - 1));
			}
			return buffer;
		}

		/** Draws the reachable space markers. */
		public BufferedImage getReachableMarks(BufferedImage img, int width) {
			BufferedImage buffer = img;
			if (buffer == null) {
				buffer = new BufferedImage(width, width,
						BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D g2 = (Graphics2D) buffer.getGraphics();
			g2.clearRect(0, 0, width, width);

			g2.setColor(Color.red);
			g2.setStroke(new BasicStroke(2.f));
			if (!left.isEmpty()) {
				g2.draw(new Line2D.Double(4., width - left.start * width, 4.,
						width - left.end * width));
			}
			if (!bottom.isEmpty()) {
				g2.draw(new Line2D.Double(bottom.start * width, width - 5.,
						bottom.end * width, width - 5.));
			}

			g2.setComposite(AlphaComposite.SrcAtop.derive(0.4f));
			if (!left.isEmpty()) {
				g2.fill(new Rectangle2D.Double(4., width - left.end * width,
						width - 8, (left.end - left.start) * width));
			}

			if (!bottom.isEmpty()) {
				g2.fill(new Rectangle2D.Double(bottom.start * width, 5.,
						(bottom.end - bottom.start) * width, width - 10));
			}
			return buffer;
		}

		/** This grid may be used to create a height plot of the distance field. */
		public double[] getDistanceGrid(double[] grid, int size) {
			double[] buffer = grid;
			if (buffer == null) {
				buffer = new double[size];
			}
			double stepsize = 1.0 / size;

			for (int s = 0; s < size; ++s) {
				double sStep = s * stepsize;
				ILocation traceAt = q.at(sStep);

				for (int t = 0; t < size; ++t) {
					double tStep = t * stepsize;
					ILocation aggAt = p.at(tStep);

					buffer[(s * size + t)] = GPSCalc
							.getDistanceTwoPointsDouble(traceAt, aggAt);
				}
			}
			return buffer;
		}

		/** Not sure with the form: from Exact Algorithms ... */
		public void scoreFunction() {
			//1. Case
			
			//2. Case
		}
		
		//Distance
		private int distanceL1(Point from, Point to) {
			if(from.x > to.x || from.y > to.y)
				return -1;
			return (to.x - from.x) + (to.y - from.y);
		}
		
		//Three-piece configuration
		private void segment() {
			
		}

		public String toString() {
			return "(" + i + ", " + j + ") left=" + left + "  bottom=" + bottom;
		}
	}

	/**
	 * Checks the condition for the decision problem. The cells must be update
	 * with the actual epsilon beforehand.
	 */
	public boolean isInDistance() {
		if (P.size() < 1 || Q.size() < 1)
			return false;

		Cell lastCell = getCell(P.size() - 1, Q.size() - 1);

		// Calculate a b for and imaginary cell p,q+1;
		Interval gate = new Interval();
		AggConnection lastSegOfP = P.get(P.size() - 1);
		ILocation lastPointOfQ = Q.get(Q.size() - 1).getTo();
		gate = GPSCalc.getSegmentCircleIntersection2(lastSegOfP.getFrom()
				.getLon(), lastSegOfP.getFrom().getLat(), lastSegOfP
				.getTo().getLon(), lastSegOfP.getTo().getLat(),
				lastPointOfQ.getLon(), lastPointOfQ.getLat(), maxDistance);

		if (lastCell.left.isEmpty() && lastCell.bottom.isEmpty())
			return false; // the gate would be empty.
		else if (GPSCalc.compareDouble(gate.end, 1.) == 0)
			return true;
		else
			return false;
	}

	/**
	 * Simple newton approximation for the epsilon. The algorithm starts with
	 * the maximum distance of the path end points. With that start given
	 * maxSteps of newton steps will be made to refine epsilon.
	 * 
	 * @param maxSteps
	 * @return
	 */
	public double approximate(int maxSteps) {
		double startValue = Math.max(
				GPSCalc.getDistanceTwoPointsDouble(P.get(0).getFrom(),
						Q.get(0).getFrom()),
				GPSCalc.getDistanceTwoPointsDouble(
						P.get(P.size() - 1).getTo(),
						Q.get(Q.size() - 1).getTo()));

		setEpsilon(startValue);

		if (isInDistance()) {
			return startValue;
		}

		double lastWorkingEpsilon = Double.MAX_VALUE;
		double stepsize = startValue;
		int steps = 0;
		double epsilonToTest = 0.;
		while (++steps <= maxSteps) {
			if (epsilonToTest == (maxDistance + stepsize))
				break;

			epsilonToTest = maxDistance + stepsize;
			setEpsilon(epsilonToTest);
			if (isInDistance()) {
				lastWorkingEpsilon = maxDistance;
				stepsize = -(stepsize / 2.);
			} else {
				stepsize = 2. * stepsize;
			}
		}

		setEpsilon(lastWorkingEpsilon);
		return maxDistance;
	}

	/** Pair.first is point in P Pair.second is point in Q */
	public HashMap<Pair<ILocation, ILocation>, Double> distanceMatrix = new HashMap<>();

	/**
	 * The location indices, also corresponding index of the edges in P or Q
	 * matched to the points on the other curve
	 */
	public HashMap<Integer, TreeSet<AggNode>> fromP = new HashMap<>();
	public HashMap<Integer, TreeSet<GPSPoint>> fromQ = new HashMap<>();
	boolean needsRecalculation = true;

	private void addP(int cellIndexi, ILocation trace, ILocation other) {
		distanceMatrix.put(new Pair<>(trace, other),
				GPSCalc.getDistanceTwoPointsDouble(trace, other));
		if (cellIndexi != -1) {
			if (!fromP.containsKey(cellIndexi)
					|| fromP.get(cellIndexi) == null) {
				fromP.put(cellIndexi, new TreeSet<AggNode>());
			}
			fromP.get(cellIndexi).add(new AggNode(other, aggContainer));
		}
	}

	private void addQ(int cellIndexj, ILocation agg, ILocation other) {
		distanceMatrix.put(new Pair<>(other, agg),
				GPSCalc.getDistanceTwoPointsDouble(other, agg));
		if (cellIndexj != -1) {
			if (!fromQ.containsKey(cellIndexj)
					|| fromQ.get(cellIndexj) == null) {
				fromQ.put(cellIndexj, new TreeSet<GPSPoint>());
			}
			fromQ.get(cellIndexj).add(new GPSPoint(other));
		}
	}

	/**
	 * Compute the critical edges. These are used by the FrechetBasedMerge merge
	 * algorithm.
	 */
	public void computeMetaData() {
		distanceMatrix.clear();
		fromP.clear();
		fromQ.clear();

		// case a)
		addP(0, P.get(0).getFrom(), Q.get(0).getFrom());
		addQ(0, Q.get(0).getFrom(), P.get(0).getFrom());

		addP(P.size(), P.get(P.size() - 1).getTo(),
				Q.get(Q.size() - 1).getTo());
		addQ(Q.size(), Q.get(Q.size() - 1).getTo(),
				P.get(P.size() - 1).getTo());
		// addP(0, P.firstElement().getFrom(), Q.firstElement().getFrom());
		// addQ(0, Q.firstElement().getFrom(), P.firstElement().getFrom());
		//
		// addP(P.size(), P.lastElement().getTo(), Q.lastElement().getTo());
		// addQ(Q.size(), Q.lastElement().getTo(), P.lastElement().getTo());

		// Case b)
		for (int i = 0; i < P.size(); ++i) {
			for (int j = 0; j < Q.size(); ++j) {
				AggConnection p = P.get(i);
				GPSEdge q = Q.get(j);

				ILocation intersectionPerpQThroughPWithQ = GPSCalc
						.intersectionWithPerpendicularThrough(q.getFrom(), q.getTo(), p.getFrom());
				ILocation intersectionPerpPThroughQWithP = GPSCalc
						.intersectionWithPerpendicularThrough(
								p.getFrom(), p.getTo(), q.getFrom());

				if (intersectionPerpPThroughQWithP != null
						&& GPSCalc.PntOnLine(p.getFrom(), p.getTo(), intersectionPerpPThroughQWithP)) {
					addP(-1, intersectionPerpPThroughQWithP, q.getFrom());
					addQ(j, q.getFrom(), intersectionPerpPThroughQWithP);
				}

				if (intersectionPerpQThroughPWithQ != null
						&& GPSCalc.PntOnLine(q.getFrom(), q.getTo(),
								intersectionPerpQThroughPWithQ)) {
					addP(i, p.getFrom(), intersectionPerpQThroughPWithQ);
					addQ(-1, intersectionPerpQThroughPWithQ, p.getFrom());
				}
			}
		}

		// Case c)

		// The path of p has to be converted into a vertex list.
		ArrayList<GPSPoint> a = new ArrayList<>();
		 for(AggConnection p : P) {
			 a.add(p.getFrom());
		 }
		 a.add(P.get(P.size() - 1).getTo());

		for (int i = 0; i < a.size() - 1; ++i) {
			for (int k = i + 1; k < a.size(); ++k) {
				GPSEdge ik = new GPSEdge((GPSPoint) a.get(i),
						(GPSPoint) a.get(k));
				ILocation mid = ik.at(0.5);
				for (int j = 0; j < Q.size(); ++j) {
					GPSEdge q = Q.get(j);

					// Intersection test of the bisector of P(i), P(k) with Q(j)
					ILocation intersection = GPSCalc
							.IntersectionOfPerpendicularWithLine(ik.getFrom(),
									ik.getTo(), mid, q.getFrom(), q.getTo());
					if (intersection != null
							&& GPSCalc.PntOnLine(q.getFrom(), q.getTo(),
									intersection)) {
						addP(i, a.get(i), intersection);
						addQ(-1, intersection, a.get(i));

						addP(k, a.get(k), intersection);
						addQ(-1, intersection, a.get(k));
					}
				}
			}
		}

		a.clear();

		 for(GPSEdge q : Q) {
			 a.add(q.getFrom());
		 }
		 a.add(Q.get(Q.size() - 1).getTo());

		for (int i = 0; i < a.size() - 1; ++i) {
			for (int k = i + 1; k < a.size(); ++k) {
				GPSEdge ik = new GPSEdge((GPSPoint) a.get(i),
						(GPSPoint) a.get(k));
				ILocation mid = ik.at(0.5);
				for (int j = 0; j < P.size(); ++j) {
					AggConnection p = P.get(j);

					// Intersection test of the bisector of Q(i), Q(k) with P(j)
					ILocation intersection = GPSCalc
							.IntersectionOfPerpendicularWithLine(ik.getFrom(),
									ik.getTo(), mid, p.getFrom(), p.getTo());
					if (intersection != null
							&& GPSCalc.PntOnLine(p.getFrom(), p.getTo(),
									intersection)) {

						addP(-1, intersection, a.get(i));
						addQ(i, a.get(i), intersection);
						
						addP(-1, intersection, a.get(k));
						addQ(k, a.get(k), intersection);
					}
				}
			}
		}
	}

	public List<Double> criticalValues = new ArrayList<Double>();

	/**
	 * Compute epsilon by first calculate all critical points of type a, b and c
	 * described in the paper Alt and Godau 95. Then do a binary search to find
	 * the smallest of the critical values that passes the inDistance test.
	 * 
	 * @return
	 */
	public double computeEpsilon() {
		criticalValues.clear();
		// case a)
		criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
				P.get(0).getFrom(), Q.get(0).getFrom()));
		criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
				P.get(P.size() - 1).getTo(), Q.get(Q.size() - 1).getTo()));
		// criticalValues.add(locationToLocationDistance.getDistance(P.firstElement().getFrom(),
		// Q.firstElement().getFrom()));
		// criticalValues.add(locationToLocationDistance.getDistance(P.lastElement().getTo(),
		// Q.lastElement().getTo()) );

		// Case b)
		for (int i = 0; i < P.size(); ++i) {
			for (int j = 0; j < Q.size(); ++j) {
				// L^F
				AggConnection p = P.get(i);
				GPSEdge q = Q.get(j);

				criticalValues.add(GPSCalc.getDistancePointToEdgeDouble(
						p.getFrom(), q.getFrom(), q.getTo()));
				criticalValues.add(GPSCalc.getDistancePointToEdgeDouble(
						q.getFrom(), p.getFrom(), p.getTo()));
				// criticalValues.add(locationToEdgeDistance.getDistance(p.getFrom(),
				// q));
				// criticalValues.add(locationToEdgeDistance.getDistance(q.getFrom(),
				// p));
			}
		}
		// The missing last elements.
		{
			AggConnection p = P.get(P.size() - 1);
			// P.get(P.size() - 1);
			for (int j = 0; j < Q.size(); ++j) {
				criticalValues.add(GPSCalc.getDistancePointToEdgeDouble(
						p.getTo(), Q.get(j).getFrom(), Q.get(j).getTo()));
				// criticalValues.add(locationToEdgeDistance.getDistance(p.getTo(),
				// Q.get(j)));
			}
			GPSEdge q = Q.get(Q.size() - 1);
			for (int i = 0; i < P.size(); ++i) {
				criticalValues.add(GPSCalc.getDistancePointToEdgeDouble(
						q.getTo(), P.get(i).getFrom(), P.get(i).getTo()));

				// criticalValues.add(locationToEdgeDistance.getDistance(q.getTo(),
				// P.get(i)));
			}
		}

		// Case c)

		// The path of p has to be converted into a vertex list.
		ArrayList<ILocation> a = new ArrayList<ILocation>();
//		for (int i = 0; i < Q.size(); i++) {
//			a.add(Q.get(i));
//		}
		 for(AggConnection p : P) {
			 a.add(p.getFrom());
		 }
		 a.add(P.get(P.size() - 1).getTo());

		for (int i = 0; i < a.size(); ++i) {
			for (int k = 0; k < a.size(); ++k) {
				if (i == k)
					continue;
				GPSEdge ik = new GPSEdge((GPSPoint) a.get(i),
						(GPSPoint) a.get(k));
				ILocation mid = ik.at(0.5);
				for (int j = 0; j < Q.size(); ++j) {
					GPSEdge q = Q.get(j);

					// Intersection test of the bisector of P(i), P(k) with Q(j)
					ILocation intersection = GPSCalc
							.IntersectionOfPerpendicularWithLine(ik.getFrom(),
									ik.getTo(), mid, q.getFrom(), q.getTo());
					if (intersection != null
					// && Algorithms.PntOnLine(q.getFrom(), q.getTo(),
					// intersection)
					) {
						criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
								a.get(i), intersection));
						criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
								a.get(k), intersection));
						// criticalValues.add(a.get(i).getDistanceTo(intersection));
						// criticalValues.add(a.get(k).getDistanceTo(intersection));
					}
				}
			}
		}

		a.clear();
//		for (int i = 0; i < P.size(); i++) {
//			a.add(P.get(i));
//		}
		 for(GPSEdge q : Q) {
			 a.add(q.getFrom());
		 }
		 a.add(Q.get(Q.size() - 1).getTo());

		for (int i = 0; i < a.size(); ++i) {
			for (int k = 0; k < a.size(); ++k) {
				if (i == k)
					continue;
				GPSEdge ik = new GPSEdge((GPSPoint) a.get(i),
						(GPSPoint) a.get(k));
				ILocation mid = ik.at(0.5);
				for (int j = 0; j < P.size(); ++j) {
					AggConnection p = P.get(j);

					// Intersection test of the bisector of Q(i), Q(k) with P(j)
					ILocation intersection = GPSCalc
							.IntersectionOfPerpendicularWithLine(ik.getFrom(),
									ik.getTo(), mid, p.getFrom(), p.getTo());
					if (intersection != null
					// && Algorithms.PntOnLine(p.getFrom(), p.getTo(),
					// intersection)
					) {
						criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
								a.get(i), intersection));
						criticalValues.add(GPSCalc.getDistanceTwoPointsDouble(
								a.get(k), intersection));
						// criticalValues.add(a.get(i).getDistanceTo(intersection));
						// criticalValues.add(a.get(k).getDistanceTo(intersection));
					}
				}
			}
		}

		// Finally binsearch for the real value of epsilon.
		Collections.sort(criticalValues);
		final double epsilon2 = binSearch(criticalValues);

		return epsilon2;
	}

	private double binSearch(List<Double> criticalValues) {
		int low = 0;
		int high = criticalValues.size() - 1;
		int median = 0;
		int goodMedian = -1;
		double goodDistance = Double.POSITIVE_INFINITY;

		while (low <= high) {
			median = (low + high) / 2;

			double test = criticalValues.get(median);
			setEpsilon(test);

			if (isInDistance()) {
				high = median - 1;
				if (test < goodDistance) {
					goodDistance = test;
					goodMedian = median;
				}
			} else {
				low = median + 1;
			}
		}

		if (goodMedian < 0) {
			double check = criticalValues.get(criticalValues.size() - 1);
			// Seems rounding errors lead to not detect good values for distance
			// right at the ends of the lines.
			check += 0.0000001;
			setEpsilon(check);
			if (isInDistance()) {
				goodDistance = check;
			}
		}

		setEpsilon(goodDistance);

		return goodDistance;
	}

	void calculateReachableSpace() {
		calculateReachableSpace(0, 0, false);
	}

	// The reachable space is calculated by dynamic programming.
	void calculateReachableSpace(int iOfPivot, int jOfPivot, boolean skipInit) {
		if (P.size() < 1 || Q.size() < 1)
			return;

		if (!skipInit) {
			Cell nullnull = getCell(iOfPivot, jOfPivot);
			if (nullnull == null)
				return;

			if (nullnull.left.start > 0 && nullnull.bottom.start > 0) {
				nullnull.left = new Interval();
				nullnull.bottom = new Interval();
			}
		}

		// Calculate Rv_i0
		for (int i = iOfPivot + 1; i < P.size(); ++i) {
			Cell cell = getCell(i, jOfPivot);
			cell.left = new Interval();
			if (cell.bottom.isEmpty()) {
				continue;
			}

			Cell bottomCell = getCell(i - 1, jOfPivot);
			if (bottomCell.bottom.isEmpty()
					|| bottomCell.bottom.start > cell.bottom.end) {
				cell.bottom = new Interval(); // Empty.
			}
		}

		// Calculate Rh_0j
		for (int j = jOfPivot + 1; j < Q.size(); ++j) {
			Cell cell = getCell(iOfPivot, j);
			cell.bottom = new Interval();
			if (cell.left.isEmpty()) {
				continue; // All following cells will be made empty.
			}

			Cell leftCell = getCell(iOfPivot, j - 1);
			if (leftCell.left.isEmpty() || leftCell.left.start > cell.left.end) {
				cell.left = new Interval(); // Empty Interval.
			}
		}

		for (int i = iOfPivot; i < P.size(); ++i) {
			for (int j = jOfPivot; j < Q.size(); ++j) {
				Cell cell = getCell(i, j);
				Cell rightCell = (j < Q.size() - 1) ? getCell(i, j + 1)
						: null;
				Cell topCell = (i < P.size() - 1) ? getCell(i + 1, j)
						: null;
				// Cell rightCell = (j < Q.size() - 1) ? getCell(i, j + 1) :
				// null;
				// Cell topCell = (i < P.size() - 1) ? getCell(i + 1, j) : null;

				if (rightCell != null) {
					if ((!cell.bottom.isEmpty())
							|| cell.left.start < rightCell.left.start) {
						rightCell.left = rightCell.left;
					} else if (cell.left.isEmpty()
							|| cell.left.start > rightCell.left.end) {
						rightCell.left = new Interval(); // Empty.
					} else {
						rightCell.left = new Interval(cell.left.start,
								rightCell.left.end);
					}
				}

				if (topCell != null) {
					if ((!cell.left.isEmpty())
							|| cell.bottom.start < topCell.bottom.start) {
						topCell.bottom = topCell.bottom;
					} else if (cell.bottom.isEmpty()
							|| cell.bottom.start > topCell.bottom.end) {
						topCell.bottom = new Interval();
					} else {
						topCell.bottom = new Interval(cell.bottom.start,
								topCell.bottom.end);
					}
				}
			}
		}
	}

	void resizeCells() {
		int newSize = Q.size() * P.size();
		if (cells == null) {
			cells = new Cell[newSize];
		} else {
			// if(cells.length > newSize) {
			// return;
			// } else {
			// cells = new Cell[newSize + 10*10];
			// needsRecalculation = true;
			// }
			cells = new Cell[newSize];
		}
	}

	//

	// TODO Edge --> Point Compability
	public Cell getCell(int i, int j) {
		assert (0 <= i && i < P.size());
		assert (0 <= j && j < Q.size());

		if (!(0 <= i && i < P.size() && 0 <= j && j < Q
				.size()))
			return null;

		if (cells == null) {
			cells = new Cell[Q.size() * P.size()];
		}

		if (cells[j * P.size() + i] == null) {
			cells[j * P.size() + i] = new Cell(i, j);
		}

		return cells[j * P.size() + i];
	}

	// TODO Edge --> Point Compability
	public double getDistance(List<AggConnection> a, List<GPSEdge> b) {
		if (a == null || b == null || a.size() < 1 || b.size() < 1) {
			return Double.POSITIVE_INFINITY;
		}
		
		
		
		this.P = new ArrayList<AggConnection>(a);
		this.Q = new ArrayList<GPSEdge>(b);
//		for(GPSEdge agg : b) {
//			P.add(new AggConnection(agg.getFrom(), agg.getTo(), aggContainer));
//		}
		resizeCells();

		double result = 0;
		computeEpsilon();

		this.P = null;
		this.Q = null;

		return result;
	}

	//
	// TODO Edge --> Point Compability
	public boolean isInDistance(List<GPSEdge> a, List<GPSEdge> b,
			double epsilon) {
		if (a == null || b == null || a.size() < 1 || b.size() < 1) {
			return false;
		}

		this.P = new ArrayList<AggConnection>();
		for(GPSEdge agg : a) {
			P.add(new AggConnection(agg.getFrom(), agg.getTo(), aggContainer));
			this.Q = new ArrayList<GPSEdge>(b);
		}

		resizeCells();
		setEpsilon(epsilon);

		boolean result = isInDistance();

		this.P = null;
		this.Q = null;

		return result;
	}
}
