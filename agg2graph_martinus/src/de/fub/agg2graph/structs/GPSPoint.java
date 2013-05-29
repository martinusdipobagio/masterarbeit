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

import java.util.ArrayList;

import org.openstreetmap.gui.jmapviewer.Coordinate;

import de.fub.agg2graph.agg.AggNode;

/**
 * A gps point with some useful constructors additionally to the basic
 * functionality inherited from {@link AbstractLocation}.
 * 
 * @author Johannes Mitlmeier
 * 
 */
public class GPSPoint extends AbstractLocation {
	public GPSPoint() {
		super();
	}

	public GPSPoint(double lat, double lon) {
		super(lat, lon);
	}

	public GPSPoint(String ID, double lat, double lon) {
		super(ID, lat, lon);
	}

	public GPSPoint(ILocation location) {
		super(location);
	}

	public GPSPoint(Coordinate position) {
		super(position);
	}
	
	public GPSPoint(AggNode aggNode) {
		super(aggNode.getID(), aggNode.getLat(), aggNode.getLon());
	}
	
	/** TODO MARTINUS **/
	public int compareLL(GPSPoint o) {
		if (this.getCluster() > o.getCluster()) {
			return 1;
		} else if (this.getCluster() < o.getCluster()) {
			return -1;
		} else if (getLat() > o.getLat()) {
			return 1;
		} else if (getLat() < o.getLat()) {
			return -1;
		} else if (getLon() > o.getLon()) {
			return 1;
		} else if (getLon() < o.getLon()) {
			return -1;
		}
		return 0;
	}

	public int compareTo(GPSPoint o) {
		if (compareLL(o) != 0) {
			return compareLL(o);
		} else if (this.elevation > o.getElevation()) {
			return 1;
		} else if (this.elevation < o.getElevation()) {
			return -1;
		}
		return 0;
	}

	public int getCluster() {
		Integer cluster = (int) (Math.ceil(getLat()) * 361
				+ Math.ceil(getLon()) + 180);
		return cluster;
	}

	public ArrayList<Integer> getNearClusters() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int cluster = this.getCluster();

		list.add(cluster);

		double modLat = getLat() % 1;
		double modLong = getLon() % 1;

		if (modLat < (0.1)) {

			list.add(cluster - 361);

			if (modLong < (0.1)) {
				list.add(cluster - 362);
			} else if (modLong > (0.9)) {
				list.add(cluster - 360);
			}

		} else if (modLat > (0.9)) {
			list.add(cluster + 361);

			if (modLong < (0.1)) {
				list.add(cluster + 360);
			} else if (modLong > (0.9)) {
				list.add(cluster + 362);
			}
		}
		if (modLong < (0.1)) {
			list.add(cluster - 1);
		} else if (modLong > (0.9)) {
			list.add(cluster + 1);
		}

		return list;
	}

	public double getDistanceTo(GPSPoint to) {
		return Math.sqrt(this.getSquaredDistanceTo(to));
	}

	public double getSquaredDistanceTo(GPSPoint to) {
		double deltaLat = getLat() - to.getLat();
		double deltaLong = getLon() - to.getLon();
		return deltaLat * deltaLat + deltaLong * deltaLong;
	}

//	public String toString() {
//		return this.getLat() + "|" + this.getLon();
//	}
	
	public String toString() {
		if (ID != null) {
			return "{" + ID + "}";
		}
		return "I [lat=" + getLat() + ", lon=" + getLon() + "]";
	}

	public static GPSPoint min(GPSPoint a, GPSPoint b) {
		return (a.compareTo(b) <= 0) ? a : b;
	}

	public static GPSPoint max(GPSPoint a, GPSPoint b) {
		return (a.compareTo(b) >= 0) ? a : b;
	}
}
