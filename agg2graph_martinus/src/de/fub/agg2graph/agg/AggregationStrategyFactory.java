package de.fub.agg2graph.agg;

import de.fub.agg2graph.agg.strategy.FrechetMatchAttractionMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.FrechetMatchIterativeMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchAttractionMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchIterativeMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.HausdorffMatchAttractionMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.HausdorffMatchDefaultMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.HausdorffMatchFrechetMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.HausdorffMatchIterativeMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.PathScoreMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchAttractionMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.PathScoreMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchDefaultMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.PathScoreMatchFrechetMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchFrechetMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.PathScoreMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.FrechetMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.GpxmergeMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchDefaultMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchFrechetMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchAttractionMergeStrategy;
import de.fub.agg2graph.agg.strategy.HausdorffMatchIterativeMergeStrategy;
import de.fub.agg2graph.agg.strategy.PathScoreMatchIterativeMergeStrategyEval;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyAttraction;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyAttractionEval;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyIterative;
import de.fub.agg2graph.agg.strategy.SecondAggregationStrategyIterativeEval;

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
	private static Class<? extends IAggregationStrategy> defaultDefaultClass = PathScoreMatchDefaultMergeStrategy.class;//A11
	private static Class<? extends IAggregationStrategy> defaultFrechetClass = PathScoreMatchFrechetMergeStrategy.class;//A12
	private static Class<? extends IAggregationStrategy> defaultAttractionClass = PathScoreMatchAttractionMergeStrategy.class;//A13
	private static Class<? extends IAggregationStrategy> defaultIterativeClass = PathScoreMatchIterativeMergeStrategy.class;//A14
	
	private static Class<? extends IAggregationStrategy> gpxAttractionClass = GpxmergeMatchAttractionMergeStrategy.class;//B11
	private static Class<? extends IAggregationStrategy> gpxIterativeClass = GpxmergeMatchIterativeMergeStrategy.class;//B12
	
	private static Class<? extends IAggregationStrategy> hausdorffDefaultClass = HausdorffMatchDefaultMergeStrategy.class;//C11
	private static Class<? extends IAggregationStrategy> hausdorffFrechetClass = HausdorffMatchFrechetMergeStrategy.class;//C12
	private static Class<? extends IAggregationStrategy> hausdorffAttractionClass = HausdorffMatchAttractionMergeStrategy.class;//C13
	private static Class<? extends IAggregationStrategy> hausdorffIterativeClass = HausdorffMatchIterativeMergeStrategy.class;//C14
	
	private static Class<? extends IAggregationStrategy> frechetAttractionClass = FrechetMatchAttractionMergeStrategy.class;//D11
	private static Class<? extends IAggregationStrategy> frechetIterativeClass = FrechetMatchIterativeMergeStrategy.class;//D12

	private static Class<? extends IAggregationStrategy> secondStrategyAttractionClass = SecondAggregationStrategyAttraction.class;//E11
	private static Class<? extends IAggregationStrategy> secondStrategyIterativeClass = SecondAggregationStrategyIterative.class;//E12

	private static Class<? extends IAggregationStrategy> defaultDefaultEval = PathScoreMatchDefaultMergeStrategyEval.class;//A11
	private static Class<? extends IAggregationStrategy> defaultFrechetEval = PathScoreMatchFrechetMergeStrategyEval.class;//A12
	private static Class<? extends IAggregationStrategy> defaultAttractionEval = PathScoreMatchAttractionMergeStrategyEval.class;//A13
	private static Class<? extends IAggregationStrategy> defaultIterativeEval = PathScoreMatchIterativeMergeStrategyEval.class;//A14
	
	private static Class<? extends IAggregationStrategy> gpxAttractionEval = GpxmergeMatchAttractionMergeStrategyEval.class;//B11
	private static Class<? extends IAggregationStrategy> gpxIterativeEval = GpxmergeMatchIterativeMergeStrategyEval.class;//B12
	
	private static Class<? extends IAggregationStrategy> hausdorffDefaultEval = HausdorffMatchDefaultMergeStrategyEval.class;//C11
	private static Class<? extends IAggregationStrategy> hausdorffFrechetEval = HausdorffMatchFrechetMergeStrategyEval.class;//C12
	private static Class<? extends IAggregationStrategy> hausdorffAttractionEval = HausdorffMatchAttractionMergeStrategyEval.class;//C13
	private static Class<? extends IAggregationStrategy> hausdorffIterativeEval = HausdorffMatchIterativeMergeStrategyEval.class;//C14
	
	private static Class<? extends IAggregationStrategy> frechetAttractionEval = FrechetMatchAttractionMergeStrategyEval.class;//D11
	private static Class<? extends IAggregationStrategy> frechetIterativeEval = FrechetMatchIterativeMergeStrategyEval.class;//D12

	private static Class<? extends IAggregationStrategy> secondStrategyAttractionEval = SecondAggregationStrategyAttractionEval.class;//E11
	private static Class<? extends IAggregationStrategy> secondStrategyIterativeEval = SecondAggregationStrategyIterativeEval.class;//E12
	
	private static Class<? extends IAggregationStrategy> factoryClass = PathScoreMatchAttractionMergeStrategy.class;

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
	
	public static IAggregationStrategy getObject(String codeName) throws InstantiationException, IllegalAccessException {
		switch(codeName) {
			case "A11":
				return defaultDefaultClass.newInstance();
			case "A12":
				return defaultFrechetClass.newInstance();
			case "A13":
				return defaultAttractionClass.newInstance();
			case "A14":
				return defaultIterativeClass.newInstance();
			case "B11":
				return gpxAttractionClass.newInstance();
			case "B12":
				return gpxIterativeClass.newInstance();
			case "C11":
				return hausdorffDefaultClass.newInstance();
			case "C12":
				return hausdorffFrechetClass.newInstance();
			case "C13":
				return hausdorffAttractionClass.newInstance();
			case "C14":
				return hausdorffIterativeClass.newInstance();
			case "D11":
				return frechetAttractionClass.newInstance();
			case "D12":
				return frechetIterativeClass.newInstance();
			case "E11":
				return secondStrategyAttractionClass.newInstance();
			case "E12":
				return secondStrategyIterativeClass.newInstance();
			case "A11Eval":
				return defaultDefaultEval.newInstance();
			case "A12Eval":
				return defaultFrechetEval.newInstance();
			case "A13Eval":
				return defaultAttractionEval.newInstance();
			case "A14Eval":
				return defaultIterativeEval.newInstance();
			case "B11Eval":
				return gpxAttractionEval.newInstance();
			case "B12Eval":
				return gpxIterativeEval.newInstance();
			case "C11Eval":
				return hausdorffDefaultEval.newInstance();
			case "C12Eval":
				return hausdorffFrechetEval.newInstance();
			case "C13Eval":
				return hausdorffAttractionEval.newInstance();
			case "C14Eval":
				return hausdorffIterativeEval.newInstance();
			case "D11Eval":
				return frechetAttractionEval.newInstance();
			case "D12Eval":
				return frechetIterativeEval.newInstance();
			case "E11Eval":
				return secondStrategyAttractionEval.newInstance();
			case "E12Eval":
				return secondStrategyIterativeEval.newInstance();
			default:
				return factoryClass.newInstance();

		}
	}
	
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
