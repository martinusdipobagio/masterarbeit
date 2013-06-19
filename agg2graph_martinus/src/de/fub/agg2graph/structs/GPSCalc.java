/*******************************************************************************
 * Copyright (c) 2012 Johannes Mitlmeier.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/agpl-3.0.html
 * 
 * Contributors:
 *     Johannes Mitlmeier - initial API and implementation
 ******************************************************************************/
package de.fub.agg2graph.structs;

import java.util.Collection;
import java.util.List;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Vector;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.structs.frechet.Interval;

public class GPSCalc {

	private static Double precision = 100000000.0;
	public static final double R = 6371; //6371

	/**
	 * Measure the distance between two points in meter
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double getDistanceTwoPointsMeter(double fromLat, double fromLon,
			double toLat, double toLon) {
		double dLat = Math.toRadians(toLat - fromLat);
		double dLon = Math.toRadians(toLon - fromLon);
		fromLat = Math.toRadians(fromLat);
		toLat = Math.toRadians(toLat);
		
		double a = Math.pow( Math.sin(dLat/2), 2 ) + Math.pow( Math.sin(dLon/2), 2 ) * Math.cos(fromLat) * Math.cos(toLat);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = R * c;
		return d * 1000;
	}

	/**
	 * Measure the distance between two points in meter
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double getDistanceTwoPointsMeter(ILocation a, ILocation b) {
		return getDistanceTwoPointsMeter(a.getLat(), a.getLon(), b.getLat(),
				b.getLon());
	}

	/**
	 * Measure the (simple) distance between two points
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double getDistanceTwoPoints(ILocation a, ILocation b) {
		return getDistanceTwoPoints(a.getLat(), a.getLon(), b.getLat(),
				b.getLon());
	}

	/**
	 * Measure the (simple) distance between two points
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double getDistanceTwoPoints(double lat1, double lon1,
			double lat2, double lon2) {
		double lat = (lat1 + lat2) / 2 * 0.01745;
		double dx = 111.3 * Math.cos(lat) * (lon1 - lon2);
		double dy = 111.3 * (lat1 - lat2);
		double distance = Math.sqrt(dx * dx + dy * dy);
		return distance * 1000;
	}

	/**
	 * Measure the distance between a point and (0,0) in Float 64 bit
	 * 
	 * @param a
	 * @return
	 */
	public static Float64Vector getDistanceTwoPointsFloat64(ILocation a) {
		GPSPoint zero = new GPSPoint(0, 0);
		return getDistanceTwoPointsFloat64(zero, a);
	}

	/**
	 * Measure the distance between two points in Float 64 bit
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Float64Vector getDistanceTwoPointsFloat64(ILocation a,
			ILocation b) {
		return Float64Vector.valueOf(b.getLat() - a.getLat(),
				b.getLon() - a.getLon());
	}

	/**
	 * Distance between two Points (WARNING: Fischer's Works) SquaredEuclidian
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static double getDistanceTwoPointsDouble(ILocation from, ILocation to) {
		double deltaLat = from.getLat() - to.getLat();
		double deltaLong = from.getLon() - to.getLon();
		return Math.sqrt(deltaLat * deltaLat + deltaLong * deltaLong);
	}
	
	/**
	 * Distance between two Points (WARNING: Fischer's Works) SquaredEuclidian
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static double getDistanceTwoPointsDouble(double fromLat, double fromLon, double toLat, double toLon) {
		double deltaLat = fromLat - toLat;
		double deltaLong = fromLon - toLon;
		return Math.sqrt(deltaLat * deltaLat + deltaLong * deltaLong);
	}

	/**
	 * Measure distance between point and trace and measure, whether distance
	 * below the limit
	 * 
	 * @param point
	 * @param list
	 * @param limit
	 * @return true if there is a distance below the limit
	 */
	public static boolean isDistancePointToTraceBelowLimit(ILocation point,
			List<? extends ILocation> list, double limit) {
		double distHere = 0;
		if (list.size() == 1) {
			return getDistanceTwoPointsMeter(point, list.get(0)) < limit;
		}
		for (int j = 0; j < list.size() - 1; j++) {
			distHere = GPSCalc.getDistancePointToEdgeMeter(point, list.get(j),
					list.get(j + 1));
			/*
			 * return true if the distance between a point and at least one edge
			 * below limit
			 */
			if (distHere < limit) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Measure the distance between a point and edge in Double
	 * 
	 * @param q
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static double getDistancePointToEdgeDouble(GPSPoint q, GPSPoint s1,
			GPSPoint s2) {
		double vx = -(s2.getLat() - s1.getLat());
		double vy = s2.getLon() - s1.getLon();

		double rx = s1.getLon() - q.getLon();
		double ry = s1.getLat() - q.getLat();

		// Calculate |v dot r|
		double f1 = Math.abs(vx * rx + vy * ry);
		double f2 = Math.sqrt(vx * vx + vy * vy);
		if (f2 == 0) {
			return Math.sqrt(rx * rx + ry * ry);
		}
		return (f1 / f2);
	}

	/**
	 * Measure the distance between point and edge in Meter
	 * 
	 * @param point
	 * @param start
	 * @param end
	 * @return
	 */
	public static double getDistancePointToEdgeMeter(ILocation point,
			ILocation start, ILocation end) {
		// project to edge and get distance to projection
		// if(!point.isRelevant() || !start.isRelevant() || !end.isRelevant())
		// return Double.MAX_VALUE;
		ILocation projection = getProjectionPoint(point, start, end);
		if (projection != null) {
			return getDistanceTwoPointsMeter(point, projection);
		}

		// double angleWithStart = getAngleBetweenEdges(start, point, start,
		// end);
		// double angleWithEnd = getAngleBetweenEdges(point, end, start,
		// end);
		// if ((angleWithStart > 90 && angleWithEnd < 90)
		// || (angleWithStart < 90 && angleWithEnd > 90)) {

		double pointToA = getDistanceTwoPointsMeter(start, point);
		double pointToB = getDistanceTwoPointsMeter(end, point);
		return Math.min(pointToA, pointToB);
		// }
	}

	/**
	 * Measure the distance between point and edge
	 * 
	 * @param point
	 * @param start
	 * @param end
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static double getDistancePointToEdgeMeter(ILocation point, IEdge edge) {
		return getDistancePointToEdgeMeter(point, edge.getFrom(), edge.getTo());
	}

	/**
	 * Measure the distance between point and traces (or List of edge)
	 * 
	 * @param point
	 * @param list
	 * @return the distance and the position
	 */
	public static double[] getDistancePointToTraceMeter(ILocation point,
			List<? extends ILocation> list) {
		int minPos = 0;
		double distHere = 0;
		double minDist = Double.MAX_VALUE;
		if (list.size() == 1) {
			return new double[] {
					getDistanceTwoPointsMeter(point, list.get(0)), 0 };
		}
		for (int j = minPos; j < list.size() - 1; j++) {

			distHere = GPSCalc.getDistancePointToEdgeMeter(point, list.get(j),
					list.get(j + 1));

			if (distHere < minDist) {
				minDist = distHere;
				minPos = j;
			}
			if (Double.isNaN(minDist)) {
				minDist = Double.MAX_VALUE;
			}
		}
		return new double[] { minDist, minPos };
	}
	
	public static double traceLengthMeter(List<? extends ILocation> trace) {
		double sum = 0;
		for(int i = 0; i < trace.size() - 1; i++) {
			sum += getDistanceTwoPointsMeter(trace.get(i), trace.get(i+1));
		}
		return sum;
	}

	public static double traceLengthMeter(GPSSegment segment) {
		double sum = 0;
		for(int i = 0; i < segment.size() - 1; i++) {
			sum += getDistanceTwoPointsMeter(segment.get(i), segment.get(i+1));

		}
		return sum;
	}
	
	/**
	 * Get gradient between two edges
	 * 
	 * @param edgeA
	 * @param edgeB
	 * @return
	 */
	public static double getSmallGradientFromEdges(
			IEdge<? extends ILocation> edgeA, IEdge<? extends ILocation> edgeB) {
		return getSmallGradientFromEdges(edgeA.getFrom(), edgeA.getTo(),
				edgeB.getFrom(), edgeB.getTo());
	}

	/**
	 * Get gradient (Steigung) between two edges
	 * 
	 * @param pointA1
	 * @param pointA2
	 * @param pointB1
	 * @param pointB2
	 * @return
	 */
	public static double getSmallGradientFromEdges(ILocation pointA1,
			ILocation pointA2, ILocation pointB1, ILocation pointB2) {
		double mA = (pointA1.getX() - pointA2.getX())
				- (pointA1.getY() - pointA2.getY());
		double mB = (pointB1.getX() - pointB2.getX())
				- (pointB1.getY() - pointB2.getY());
		if (mA > 1) {
			mA = 2 - (1 / mA);
		}
		if (mB > 1) {
			mB = 2 - (1 / mB);
		}
		return Math.abs(mA - mB);
	}

	/**
	 * Get the middle point between two edges
	 * ("Only usable for very short distances")
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static ILocation getMidwayLocation(ILocation a, ILocation b) {
		double newLat = a.getLat() + (b.getLat() - a.getLat()) / 2;
		double newLon = a.getLon() + (b.getLon() - a.getLon()) / 2;
		return new GPSPoint(newLat, newLon);
	}

	/**
	 * Get Point between Edge with given proportional (WARNING: Fischer's Works)
	 * 
	 * @param t
	 * @param from
	 * @param to
	 * @return
	 */
	public static ILocation getPointAt(double t, ILocation from, ILocation to) {
		return new GPSPoint((1 - t) * from.getLat() + t * to.getLat(), (1 - t)
				* from.getLon() + t * to.getLon());
	}

	/**
	 * Get the gradient (Steigung) between two edges
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double getGradient(double lat1, double lon1, double lat2,
			double lon2) {
		// m = (delta_y / delta_x)

		// border cases
		if (Math.abs(lon2 - lon1) < 10e-4) {
			if (lat2 - lat1 == 0) {
				return 0;
			} else if (lat2 - lat1 > 0) {
				return Double.POSITIVE_INFINITY;
			} else {
				return Double.NEGATIVE_INFINITY;
			}
		}
		return (lat2 - lat1) / (lon2 - lon1);
	}

	/**
	 * Get the gradient (Steigung) between two edges
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double getGradient(ILocation a, ILocation b) {
		return getGradient(a.getLat(), a.getLon(), b.getLat(), b.getLon());
	}

	/**
	 * Measure the angle between two Edges
	 * 
	 * @param pointA1
	 * @param pointA2
	 * @param pointB1
	 * @param pointB2
	 * @return
	 */
	public static double getAngleBetweenEdges(ILocation pointA1,
			ILocation pointA2, ILocation pointB1, ILocation pointB2) {
		Float64Vector vecA = getDistanceTwoPointsFloat64(pointA1, pointA2);
		Float64Vector vecB = getDistanceTwoPointsFloat64(pointB1, pointB2);
		return Math.toDegrees(Math.acos((vecA.times(vecB).divide(vecA
				.normValue() * vecB.normValue())).doubleValue()));
	}
	
	/**
	 * Measure the angle between two Edges
	 * 
	 * @param edge1
	 * @param edge2
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static double getAngleBetweenEdges(IEdge edge1, IEdge edge2) {
		return getAngleBetweenEdges(edge1.getFrom(), edge1.getTo(),
				edge2.getFrom(), edge2.getTo());
	}

	/**
	 * Get the average distance between each nodes and (0,0)
	 * 
	 * @param locations
	 * @return
	 */
	public static ILocation getPointAverage(List<ILocation> locations) {
		if (locations.size() == 0) {
			return null;
		}
		GPSPoint zero = new GPSPoint(0, 0);
		Float64Vector sum = getDistanceTwoPointsFloat64(zero);
		for (ILocation point : locations) {
			sum = sum.plus(getDistanceTwoPointsFloat64(point));
		}
		Float64Vector result = sum.times(1.0 / locations.size());
		return new GPSPoint(result.getValue(0), result.getValue(1));
	}

	/**
	 * Projection point
	 * 
	 * @http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
	 * @param point
	 * @param start
	 * @param end
	 * @return
	 */
	public static ILocation getProjectionPoint(ILocation point,
			ILocation start, ILocation end) {
		ILocation result = new GPSPoint();
		Float64Vector w = getDistanceTwoPointsFloat64(start, point);
		Float64Vector a = getDistanceTwoPointsFloat64(start, end);
		Float64 factor = (w.times(a).divide(Math.pow(a.normValue(), 2)));
		Float64Vector proj = getDistanceTwoPointsFloat64(new GPSPoint(0, 0),
				start).plus(a.times(factor));
		if (!((proj.get(0).doubleValue() >= start.getLat() && proj.get(0)
				.doubleValue() <= end.getLat()) || (proj.get(0).doubleValue() <= start
				.getLat() && proj.get(0).doubleValue() >= end.getLat()))) {
			return null;
		}
		if (!((proj.get(1).doubleValue() >= start.getLon() && proj.get(1)
				.doubleValue() <= end.getLon()) || (proj.get(1).doubleValue() <= start
				.getLon() && proj.get(1).doubleValue() >= end.getLon()))) {
			return null;
		}
		result.setLat(proj.get(0).doubleValue());
		result.setLon(proj.get(1).doubleValue());
		return result;
	}

	/**
	 * Projection point
	 * 
	 * @http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
	 * @param point
	 * @param start
	 * @param end
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static ILocation getProjectionPoint(ILocation point, IEdge edge) {
		return getProjectionPoint(point, edge.getFrom(), edge.getTo());
	}

	/**
	 * Comparing two double
	 * 
	 * @param d1
	 * @param d2
	 * @return
	 */
	public static int compareDouble(double d1, double d2) {
		Double r1 = Math.round(d1 * precision) / precision;
		Double r2 = Math.round(d2 * precision) / precision;
		return (Double.compare(r1, r2));
	}

	/**
	 * TODO in meter? Circle-Segment intersection
	 * 
	 * @http://mathworld.wolfram.com/Circle-LineIntersection.html
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @param cx
	 * @param cy
	 * @param radius
	 * @return
	 */
	public static Interval getSegmentCircleIntersection2(double ax, double ay,
			double bx, double by, // Segment
			double cx, double cy, // Circle center
			double radius) {
		final double vx = bx - ax;
		final double vy = by - ay;
		final double sx = ax - cx;
		final double sy = ay - cy;

		// polygon
		final double a = vx * vx + vy * vy; // V^2
		final double b = 2 * (sx * vx + sy * vy); // 2(S dot V)
		final double c = (sx * sx + sy * sy) - radius * radius; // S^2 - r^2

		Interval result = new Interval();

		if (a == 0.) { // Input is a line degraded to a point
			final double incircle = Math.sqrt(sx * sx + sy * sy);
			if (incircle <= radius) {
				result.start = 0.;
				result.end = 1.;
			}
		} else {

			// Discriminant
			final double D = b * b - 4 * a * c;

			if (D >= 0) {
				final double Dsq = Math.sqrt(D);

				final double r1 = (-b - Dsq) / (2 * a);

				final double r2 = (D == 0) ? r1 : (-b + Dsq) / (2 * a);

				if (r1 < r2 && r1 < 1. && r2 > 0.) {
					result.start = (r1 < 0.) ? 0. : r1;
					result.end = (r2 > 1.) ? 1. : r2;
				}
			}
		}

		return result;
	}

	/**
	 * Intersection (WARNING: Fischer's Works)
	 * 
	 * @param p1
	 * @param p2
	 * @param q
	 * @param r1
	 * @param r2
	 * @return
	 */
	public static ILocation IntersectionOfPerpendicularWithLine(ILocation p1,
			ILocation p2, ILocation q, ILocation r1, ILocation r2) {
		final double p1x = p1.getLon();
		final double p1y = p1.getLat();
		final double p2x = p2.getLon();
		final double p2y = p2.getLat();

		final double qx = q.getLon();
		final double qy = q.getLat();

		final double a = p1y - p2y;
		final double b = p2x - p1x;

		final double pa = -b;
		final double pb = a;
		final double pc = pa * qx + pb * qy;

		final double r1x = r1.getLon();
		final double r1y = r1.getLat();
		final double r2x = r2.getLon();
		final double r2y = r2.getLat();

		final double ra = r1y - r2y;
		final double rb = r2x - r1x;
		final double rc = r2x * r1y - r2y * r1x;

		final double D = (ra * pb) - (pa * rb);

		if (D != 0.) {
			return new GPSPoint((ra * pc - pa * rc) / D, (pb * rc - rb * pc)
					/ D);
		} else {
			return null;
		}
	}
	
	public static ILocation intersectionWithPerpendicularThrough(
			GPSPoint p1, GPSPoint p2, GPSPoint q) {
		final double p1x = p1.getLon();
		final double p1y = p1.getLat();
		final double p2x = p2.getLon();
		final double p2y = p2.getLat();
		
		final double qx = q.getLon();
		final double qy = q.getLat();
		
		final double a = p1y - p2y;
		final double b = p2x - p1x;
		final double c = p2x*p1y - p2y*p1x;
		
		final double pa = -b;
		final double pb = a;
		final double pc = pa*qx + pb*qy;
		
		final double D = (a*pb) - (pa*b);

		if(D != 0.) {
			return new AbstractLocation((a*pc - pa*c)/D, (pb*c - b*pc)/D);
		} else {
			return null;
		}
	}
	
	public static ILocation intersection(ILocation p1, ILocation p2, ILocation q1, ILocation q2) {
		final double p1x = p1.getLon();
		final double p1y = p1.getLat();
		final double p2x = p2.getLon();
		final double p2y = p2.getLat();
		
		final double q1x = q1.getLon();
		final double q1y = q1.getLat();
		final double q2x = q2.getLon();
		final double q2y = q2.getLat();
		
		final double a1 = p1y - p2y;
		final double b1 = p2x - p1x;
		final double c1 = p2x*p1y - p2y*p1x;
		
		final double a2 = q1y - q2y;
		final double b2 = q2x - q1x;
		final double c2 = q2x*q1y - q2y*q1x;
		
		final double D = (a1*b2) - (a2*b1);
		
		if(D != 0.) {
			return new AbstractLocation((a1*c2 - a2*c1)/D, (b2*c1 - b1*c2)/D);
		} else {
			return null;
		}
	}
	
//	public static void main(String[] args) {
//		ILocation p1 = new GPSPoint(3, 1);
//		ILocation p2 = new GPSPoint(5, 1);
//		ILocation q1 = new GPSPoint(2, 6);
//		ILocation q2 = new GPSPoint(2, 7);
//		System.out.println(Intersection(p1, p2, q1, q2));
//	}
	
	public static boolean PntOnLine(ILocation p, ILocation q, ILocation t)	{
		/*
		 * given a line through P:(px,py) Q:(qx,qy) and T:(tx,ty)
		 * return 0 if T is not on the line through      <--P--Q-->
		 *        1 if T is on the open ray ending at P: <--P
		 *        2 if T is on the closed interior along:   P--Q
		 *        3 if T is on the open ray beginning at Q:    Q-->
		 */
		final double px = p.getLon();
		final double py = p.getLat();
		final double qx = q.getLon();
		final double qy = q.getLat();
		final double tx = t.getLon();
		final double ty = t.getLat();
		
		if ((px == qx) && (py == qy)) {
			if ((tx == px) && (ty == py))
				return true;
			else
				return false;
		}


		if ( Math.abs((qy-py)*(tx-px)-(ty-py)*(qx-px)) >=
				(Math.max(Math.abs(qx-px), Math.abs(qy-py)))) return false;
		if (((qx<px)&&(px<tx)) || ((qy<py)&&(py<ty))) return false;
		if (((tx<px)&&(px<qx)) || ((ty<py)&&(py<qy))) return false;
		if (((px<qx)&&(qx<tx)) || ((py<qy)&&(qy<ty))) return false;
		if (((tx<qx)&&(qx<px)) || ((ty<qy)&&(qy<py))) return false;
		
		return true;
	}
	
	public static AggNode calculateMean(AggNode locationToMove, Collection<GPSPoint> affectedTraceLocations, 
			double epsilon, AggContainer aggContainer, boolean dampFactor) {
		final double alon = locationToMove.getLon();
		final double alat = locationToMove.getLat();
	
		double slon = 0;
		double slat = 0;
		
		int div = 0;
		
		for(ILocation ti : affectedTraceLocations) {
			double dist = ((GPSPoint) locationToMove).getDistanceTo((GPSPoint) ti);
//			System.out.println("dist = " + dist);
			if(dist > epsilon) 
				continue;
			
//			double damp = damp(dist, epsilon);
			double damp;
			if(dampFactor) {
				if(locationToMove.getK() >= 4)
					damp = damp(dist, epsilon) / (Math.log10(locationToMove.getK()) / Math.log10(2));
				else
					damp = damp(dist, epsilon);
			}
			else 
				damp = damp(dist, epsilon);

			slon += damp*(ti.getLon() - alon);
			slat += damp*(ti.getLat() - alat);
			++div;
		}
		
		if(div == 0) return locationToMove;
		
		slon /= div;
		slat /= div;
	
//		return null;
		return new AggNode(slat + alat, slon + alon, aggContainer);
	}
	
	public static AggNode moveLocation(AggNode fix, AggNode toMove, AggContainer aggContainer) {
		// along To Perpendicular from Trace
		final double alon = toMove.getLon();
		final double alat = toMove.getLat();
		final double elon = fix.getLon();
		final double elat = fix.getLat();
		
		final double n = 0.5;
		
		return new AggNode( (alat * n + elat) / (n + 1.), (alon * n +  elon) / (n + 1.), aggContainer);
	}
	
	public static double damp(double distance, double epsilon) {
		final double d = distance / (4*epsilon);
		final double fval = Math.exp(-(5*d*d)); // clamp into [0..1] approx.
				
		if(fval <= 0) {
			return 0.;
		} else if(fval >= 1) {
			return 1.;
		} else {
			return fval;
		}
	}
}
