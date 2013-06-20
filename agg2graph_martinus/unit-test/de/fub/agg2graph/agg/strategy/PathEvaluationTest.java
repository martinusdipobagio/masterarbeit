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
package de.fub.agg2graph.agg.strategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.AggregationStrategyFactory;
import de.fub.agg2graph.agg.IAggregationStrategy;
import de.fub.agg2graph.agg.MergeHandlerFactory;
import de.fub.agg2graph.agg.tiling.DefaultCachingStrategy;
import de.fub.agg2graph.structs.GPSPoint;

public class PathEvaluationTest extends TestCase {

	private IAggregationStrategy aggStrategy;
	private DefaultCachingStrategy cachingStrategy;
	private AggContainer container;
	private List<AggNode> nodes;
	private List<GPSPoint> pointsClose;
	private List<GPSPoint> pointsDistant;

	@Override
	protected void setUp() {
		AggregationStrategyFactory.setClass(PathScoreMatchDefaultMergeStrategy.class);
		aggStrategy = AggregationStrategyFactory.getObject();
		cachingStrategy = new DefaultCachingStrategy();
		container = AggContainer.createContainer(new File("test/agg/unittest"),
				aggStrategy, cachingStrategy);
		container.clear();
		nodes = new ArrayList<AggNode>(1000);
		AggNode lastNode = null, node;
		for (double i = 10; i < 60; i += 0.2) {
			node = new AggNode(i, i, container);
			container.addNode(node);
			container.connect(lastNode, node);
			nodes.add(node);
			lastNode = node;
		}

		double baseDist = 0.00001;
		// close
		pointsClose = new ArrayList<GPSPoint>(20);
		GPSPoint point;
		for (double i = 20; i < 30; i += 0.5) {
			point = new GPSPoint(i + baseDist, i);
			pointsClose.add(point);
		}
		// distant
		pointsDistant = new ArrayList<GPSPoint>(20);
		for (double i = 20; i < 30; i += 0.5) {
			point = new GPSPoint(i + baseDist * 10, i);
			pointsDistant.add(point);
		}

	}

	public void testPathEvaluationCloserIsBetter() {
		double closeResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes, pointsClose, 0,
						MergeHandlerFactory.getObject())[0];
		double distantResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes, pointsDistant, 0,
						MergeHandlerFactory.getObject())[0];
		// System.out.println("close: " + closeResult);
		// System.out.println("distant: " + distantResult);
		assertTrue("distant lines are less similar than close ones",
				distantResult > closeResult);
	}

	public void testPathEvaluationLongerIsBetter() {
		double longResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes, pointsClose, 0,
						MergeHandlerFactory.getObject())[0];
		double shortResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes,
						pointsClose.subList(0, pointsClose.size() / 2), 0,
						MergeHandlerFactory.getObject())[0];
		// System.out.println("long: " + longResult);
		// System.out.println("short: " + shortResult);
		assertTrue("long matches are more similar (better) than short ones",
				shortResult > longResult);
	}

	public void testPathEvaluationBadAngle() {
		double goodResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes, pointsClose, 0,
						MergeHandlerFactory.getObject())[0];
		container.clear();
		nodes.clear();
		AggNode lastNode = null, node;
		for (double i = 10; i < 12; i += 0.5) {
			node = new AggNode(i, i, container);
			container.addNode(node);
			container.connect(lastNode, node);
			nodes.add(node);
			lastNode = node;
		}
		node = new AggNode(5, 15, container);
		container.addNode(node);
		container.connect(lastNode, node);
		nodes.add(node);
		double badResult = (Double) aggStrategy.getTraceDist()
				.getPathDifference(nodes, pointsClose, 0,
						MergeHandlerFactory.getObject())[0];
		// System.out.println("good angle: " + goodResult);
		// System.out.println("bad angle: " + badResult);
		assertTrue(
				"match with bad angle is less similar (worse) than match with good angle",
				badResult > goodResult);

	}
}
