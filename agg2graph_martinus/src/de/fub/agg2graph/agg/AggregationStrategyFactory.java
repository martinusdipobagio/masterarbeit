package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.DefaultMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchFrechetBasedMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchFrechedBasedMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchFrechetBasedStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchFrechetBasedMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.SecondStrategy;

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
	private static Class<? extends IAggregationStrategy> defaultAttractionClass = DefaultMatchAttractionMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> gpxDefaultClass = GpxmergeMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxIterativeClass = GpxmergeMatchIterativeMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxFrechetClass = GpxmergeMatchFrechetBasedStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxAttractionClass = GpxmergeMatchAttractionMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> hausdorffDefaultClass = HausdorffMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffIterativeClass = HausdorffMatchIterativeMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffFrechetClass = HausdorffMatchFrechetBasedMergeStrategy.class;	
	private static Class<? extends IAggregationStrategy> hausdorffAttractionClass = HausdorffMatchAttractionMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> frechetDefaultClass = FrechetMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> frechetIterativeClass = FrechetMatchIterativeMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> frechetFrechetClass = FrechetMatchFrechedBasedMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> frechetAttractionClass = FrechetMatchAttractionMergeStrategy.class;

	private static Class<? extends IAggregationStrategy> secondStrategyClass = SecondStrategy.class;

	//TODO
	private static Class<? extends IAggregationStrategy> factoryClass = SecondStrategy.class;

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
		String[] ret = new String[]{defaultDefaultClass.getName(), gpxDefaultClass.getName(), hausdorffDefaultClass.getName()};
		return ret;
	}
	
	public static void setClassByName(String name) {
		if(name.equals(defaultDefaultClass.getName()))
			factoryClass = defaultDefaultClass;
		else if(name.equals(gpxDefaultClass.getName()))
			factoryClass = gpxDefaultClass;
		else if(name.equals(hausdorffDefaultClass.getName()))
			factoryClass = hausdorffDefaultClass;
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
		} else if(name.equals(gpxDefaultClass.getName())) {
			try {
				return gpxDefaultClass.newInstance();
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
