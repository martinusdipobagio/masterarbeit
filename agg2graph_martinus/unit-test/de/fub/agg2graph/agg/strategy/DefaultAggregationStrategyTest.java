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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.structs.GPSPoint;

public class DefaultAggregationStrategyTest extends TestCase {
	private DefaultMatchDefaultMergeStrategy das;
	private DefaultTraceDistance dtd;
	private DefaultMergeHandler dmh;
	private List<AggNode> path;
	private List<GPSPoint> points;
	private static double epsilon = 1E-2;

	@Override
	public void setUp() {
		das = new DefaultMatchDefaultMergeStrategy();
		dtd = (DefaultTraceDistance) das.getTraceDist();
		dtd.maxOutliners = 1;
		dtd.maxDistance = 40;
		das.maxInitDistance = 100;
		dtd.maxLookahead = 6;
		dtd.maxPathDifference = 100;
		dtd.minLengthFirstSegment = 1;
		dtd.maxAngle = 20;
		dmh = new DefaultMergeHandler(null);
		path = new ArrayList<AggNode>();
		points = new ArrayList<GPSPoint>();
	}

	@Test
	public void testBadCases() {
		// empty lists
		ensureDiff(Double.MAX_VALUE);
		// lists with one entry
		points.add(new GPSPoint(1, 1));
		ensureDiff(Double.MAX_VALUE);
		// lists with one entry
		points.clear();
		path.add(new AggNode(null, 4, 10, null));
		ensureDiff(Double.MAX_VALUE);
		// lists with one entry
		points.add(new GPSPoint(1, 1));
		ensureDiff(Double.MAX_VALUE);
	}

	@Test
	public void testSamePaths() {
		// add test data
		for (int i = 25; i < 38; i++) {
			points.add(new GPSPoint(i, i + i / 7.0));
			path.add(new AggNode(null, i, i + i / 7.0, null));
		}
		ensureDiff(0);
	}

	@Test
	public void testDistanceDifference() {
		for (double i = 25; i < 39; i++) {
			points.add(new GPSPoint(i, i));
			path.add(new AggNode(null, i + 0.0001, i + 0.0001, null));
		}
		double diffClose = getDiff();
		points.clear();
		path.clear();
		for (double i = 25.001; i < 39; i++) {
			points.add(new GPSPoint(i, i));
			path.add(new AggNode(null, i + 0.0002, i + 0.0002, null));
		}
		double diffDistant = getDiff();
		assertTrue(
				"Greater distance means greater difference, but it does not hold that "
						+ diffDistant + " > " + diffClose,
				diffDistant > diffClose);
	}

	@Test
	public void testUncorrelated() {
		for (double i = 25; i < 39; i++) {
			points.add(new GPSPoint(i, i));
		}
		for (double i = 45; i < 50; i++) {
			path.add(new AggNode(null, i, i, null));
		}
		ensureDiff(Double.MAX_VALUE);
	}

	@Test
	public void testLengthDifference() {
		for (double i = 25; i < 29; i = i + 1) {
			points.add(new GPSPoint(i, i));
			path.add(new AggNode(null, i, i + 0.0001, null));
		}
		double diffFew = getDiff();
		points.clear();
		path.clear();
		for (double i = 25; i < 29; i = i + 0.3) {
			points.add(new GPSPoint(i, i));
			path.add(new AggNode(null, i, i + 0.0001, null));
		}
		double diffMany = getDiff();
		// System.out.println("Many: " + diffMany);
		// System.out.println("Few: " + diffFew);
		assertTrue(
				"Longer traces with same distance are more similar, but it does not hold that "
						+ diffFew + " > " + diffMany, diffFew > diffMany);
	}

	private void ensureDiff(double targetDiff) {
		double actualDiff = getDiff();
		assertTrue("Paths " + path + " and " + points
				+ " should have difference " + targetDiff + ", but was "
				+ actualDiff, Math.abs(actualDiff - targetDiff) < epsilon);
	}

	private double getDiff() {
		Object[] difference = dtd.getPathDifference(path, points, 0, dmh);
		return Double.parseDouble(difference[0].toString());
	}
}
