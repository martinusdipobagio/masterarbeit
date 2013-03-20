package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.DefaultAggregationStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchAggregationStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeAggregationStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchAggregationStrategy;

/**
 * A factory that returns the {@link IAggregationStrategy} currently set via the
 * getObject method.
 * 
 * @author Johannes Mitlmeier
 * 
 */
public class AggregationStrategyFactory {
	/*
	 * Default class to return. Can be overwritten by calls to setClass.
	 */
	private static Class<? extends IAggregationStrategy> defaultClass = DefaultAggregationStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxClass = GpxmergeAggregationStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffP2PClass = HausdorffMatchAggregationStrategy.class;
	@SuppressWarnings("unused")
	private static Class<? extends IAggregationStrategy> frechetClass = FrechetMatchAggregationStrategy.class;
//	private static Class<? extends IAggregationStrategy> hausdorffT2TClass = HausdorffTrackToTrackAggregationStrategy.class;
	//TODO
	private static Class<? extends IAggregationStrategy> factoryClass = HausdorffMatchAggregationStrategy.class;
	
	public static void setClass(Class<? extends IAggregationStrategy> theClass) {
		factoryClass = theClass;
		
		System.out.println(factoryClass.getName() + " is now active!");
	}

	public static IAggregationStrategy getObject() {
		if (factoryClass == null) {
			return null;
		}
		try {
			return factoryClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * TODO noch statisch
	 */
	public static String[] getAllStrategyName() {
		String[] ret = new String[]{defaultClass.getName(), gpxClass.getName(), hausdorffP2PClass.getName()};
		return ret;
	}
	
	public static void setClassByName(String name) {
		if(name.equals(defaultClass.getName()))
			factoryClass = defaultClass;
		else if(name.equals(gpxClass.getName()))
			factoryClass = gpxClass;
		else if(name.equals(hausdorffP2PClass.getName()))
			factoryClass = hausdorffP2PClass;
	}
	
	public static IAggregationStrategy getClassByName(String name) {
		if(name.equals(defaultClass.getName())) {
			try {
				return defaultClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if(name.equals(gpxClass.getName())) {
			try {
				return gpxClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if(name.equals(hausdorffP2PClass.getName())) {
			try {
				return hausdorffP2PClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
