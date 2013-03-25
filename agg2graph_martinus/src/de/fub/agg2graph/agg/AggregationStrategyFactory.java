package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.DefaultMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchDefaultAggregationStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchIterativeMergeAggregationStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeDefaultAggregationStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchDefaultAggregationStrategy;

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
	private static Class<? extends IAggregationStrategy> defaultClass = DefaultMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxClass = GpxmergeDefaultAggregationStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffDefaultClass = HausdorffMatchDefaultAggregationStrategy.class;
	@SuppressWarnings("unused")
	private static Class<? extends IAggregationStrategy> frechetClass = FrechetMatchDefaultAggregationStrategy.class;
	private static Class<? extends IAggregationStrategy> frechetIterativeClass = FrechetMatchIterativeMergeAggregationStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultIterativeClass = DefaultMatchIterativeMergeStrategy.class;
	//TODO
	private static Class<? extends IAggregationStrategy> factoryClass = DefaultMatchIterativeMergeStrategy.class;

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
		String[] ret = new String[]{defaultClass.getName(), gpxClass.getName(), hausdorffDefaultClass.getName()};
		return ret;
	}
	
	public static void setClassByName(String name) {
		if(name.equals(defaultClass.getName()))
			factoryClass = defaultClass;
		else if(name.equals(gpxClass.getName()))
			factoryClass = gpxClass;
		else if(name.equals(hausdorffDefaultClass.getName()))
			factoryClass = hausdorffDefaultClass;
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
		} else if(name.equals(hausdorffDefaultClass.getName())) {
			try {
				return hausdorffDefaultClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
