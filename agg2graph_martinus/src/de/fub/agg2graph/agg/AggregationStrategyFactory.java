package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.DefaultMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchFrechetBasedMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategy;

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
	private static Class<? extends IAggregationStrategy> defaultDefaultClass = DefaultMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultIterativeClass = DefaultMatchIterativeMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultFrechetClass = DefaultMatchFrechetBasedMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultAttractionClass = DefaultMatchAttractionMergeStrategy.class;//TODO
	
	private static Class<? extends IAggregationStrategy> gpxIterativeClass = GpxmergeMatchIterativeMergeStrategy.class;//TODO
	
	private static Class<? extends IAggregationStrategy> hausdorffIterativeClass = HausdorffMatchIterativeMergeStrategy.class;//TODO
	
	private static Class<? extends IAggregationStrategy> frechetAttractionClass = FrechetMatchAttractionMergeStrategy.class;//TODO

	private static Class<? extends IAggregationStrategy> secondStrategyClass = SecondAggregationStrategy.class;

	//TODO
	private static Class<? extends IAggregationStrategy> factoryClass = SecondAggregationStrategy.class;

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
		String[] ret = new String[]{defaultDefaultClass.getName()};
		return ret;
	}
	
	public static void setClassByName(String name) {
		if(name.equals(defaultDefaultClass.getName()))
			factoryClass = defaultDefaultClass;
	}
	
	public static IAggregationStrategy getClassByName(String name) {
		if(name.equals(defaultDefaultClass.getName())) {
			try {
				return defaultDefaultClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
