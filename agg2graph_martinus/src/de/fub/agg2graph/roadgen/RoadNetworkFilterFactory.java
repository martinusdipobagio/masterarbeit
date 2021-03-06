package de.fub.agg2graph.roadgen;

/**
 * A factory that returns the {@link IRoadNetworkFilter} currently set via the
 * getObject method.
 * 
 * @author Johannes Mitlmeier
 * 
 */
public class RoadNetworkFilterFactory {
	/*
	 * Default class to return. Can be overwritten by calls to setClass.
	 */
	private static Class<?> factoryClass = DefaultRoadNetworkFilter.class;

	public static void setClass(Class<?> clazz) {
		factoryClass = clazz;
	}

	public static IRoadNetworkFilter getObject() {
		if (factoryClass == null) {
			return null;
		}
		try {
			return (IRoadNetworkFilter) factoryClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
}
