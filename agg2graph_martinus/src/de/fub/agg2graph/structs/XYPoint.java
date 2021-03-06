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

import java.util.Arrays;

public class XYPoint implements ILocation {
	protected double[] xy = new double[2];
	protected int k = 1;
	private final double EPSILON = 10e-6;
	private String ID;

	public XYPoint() {
		super();
	}

	public XYPoint(double x, double y) {
		this();
		setXY(x, y);
	}

	public XYPoint(String ID, double x, double y) {
		this(x, y);
		setID(ID);
	}
	
	public XYPoint(String ID, double x, double y, int k) {
		this(x, y);
		setID(ID);
		setK(k);
	}
	
	public XYPoint(double x, double y, int k) {
		this();
		setXY(x, y);
		setK(k);
	}

	@Override
	public String toString() {
		return String.format("[x: %.3f, y: %.3f]", xy[0], xy[1]);
	}

	@Override
	public String toDebugString() {
		return toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(xy);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		XYPoint other = (XYPoint) obj;
		if (Math.abs(getX() - other.getX()) > EPSILON
				|| Math.abs(getY() - other.getY()) > EPSILON) {
			return false;
		}
		return true;
	}

	@Override
	public void setLat(double lat) {
	}

	@Override
	public void setLon(double lon) {
	}

	@Override
	public void setLatLon(double lat, double lon) {
	}

	@Override
	public void setX(double x) {
		this.xy[0] = x;
	}

	@Override
	public void setY(double y) {
		this.xy[1] = y;
	}

	@Override
	public void setXY(double x, double y) {
		this.xy[0] = x;
		this.xy[1] = y;
	}

	@Override
	public double getLat() {
		return 0.0;
	}

	@Override
	public double getLon() {
		return 0.0;
	}

	@Override
	public double[] getLatLon() {
		return null;
	}

	@Override
	public double getX() {
		return xy[0];
	}

	@Override
	public double getY() {
		return xy[1];
	}

	@Override
	public double[] getXY() {
		return xy;
	}

	@Override
	public void setLatLon(double[] latlon) {
	}

	@Override
	public void setXY(double[] xy) {
		setXY(xy[0], xy[1]);
	}

	@Override
	public double getWeight() {
		return 1;
	}

	@Override
	public void setID(String ID) {
		this.ID = ID;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setK(int k) {
		this.k = k;
	}

	@Override
	public int getK() {
		return k;
	}

	@Override
	public void setRelevant(boolean relevant) {
	}

	@Override
	public boolean isRelevant() {
		return false;
	}

	@Override
	public int getCluster() {
		// TODO Auto-generated method stub
		return 0;
	}
}
