package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.PathScoreMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchFrechetMergeStrategy;
import de.fub.agg2graph.agg.strategy.DefaultMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchFrechetMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyAttraction;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyIterative;

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
	private static Class<? extends IAggregationStrategy> defaultDefaultClass = PathScoreMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultFrechetClass = PathScoreMatchFrechetMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultAttractionClass = PathScoreMatchAttractionMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> defaultIterativeClass = DefaultMatchIterativeMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> gpxAttractionClass = GpxmergeMatchAttractionMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> gpxIterativeClass = GpxmergeMatchIterativeMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> hausdorffDefaultClass = HausdorffMatchDefaultMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffFrechetClass = HausdorffMatchFrechetMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffAttractionClass = HausdorffMatchAttractionMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> hausdorffIterativeClass = HausdorffMatchIterativeMergeStrategy.class;
	
	private static Class<? extends IAggregationStrategy> frechetAttractionClass = FrechetMatchAttractionMergeStrategy.class;
	private static Class<? extends IAggregationStrategy> frechetIterativeClass = FrechetMatchIterativeMergeStrategy.class;

	private static Class<? extends IAggregationStrategy> secondStrategyAttractionClass = SecondAggregationStrategyAttraction.class;
	private static Class<? extends IAggregationStrategy> secondStrategyIterativeClass = SecondAggregationStrategyIterative.class;

	private static Class<? extends IAggregationStrategy> factoryClass = HausdorffMatchDefaultMergeStrategy.class;

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
		String[] ret = new String[]{defaultAttractionClass.getName()};
		return ret;
	}
	
	public static void setClassByName(String name) {
		if(name.equals(defaultAttractionClass.getName()))
			factoryClass = defaultAttractionClass;
	}
	
	public static IAggregationStrategy getClassByName(String name) {
		if(name.equals(defaultAttractionClass.getName())) {
			try {
				return defaultAttractionClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
