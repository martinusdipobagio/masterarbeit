package de.fub.agg2graph.structs;

import org.jscience.mathematics.vector.Float64Vector;

public class Projection {
	public enum Mode {
		METER, DOUBLE, FLOAT64VECTOR;
	}
	
	private double distanceMeter;
	private double distancePoint;
	private Float64Vector distance64;
	private ILocation projection;
	
	public Projection(double distance, ILocation projection, Mode mode) {
		if(mode.equals(Mode.METER))
			distanceMeter = distance;
		else
			distancePoint = distance;
		this.projection = projection;
	}
	
	public Projection(Float64Vector distance, ILocation projection) {
		distance64 = distance;
	}

	public double getDistance(Mode mode) {
		if(mode.equals(Mode.METER))
			return distanceMeter;
		else
			return distancePoint;
	}


	public Float64Vector getDistance64() {
		return distance64;
	}

	public ILocation getProjection() {
		return projection;
	}
	
}
