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
package de.fub.agg2graph.structs;

import junit.framework.TestCase;

import org.junit.Test;

public class GPSCalcTest extends TestCase {
	private final double EPSILON = 10e-4;
	private final double LARGE_EPSILON = 10e-1;

	@Test
	public void testGetDistance() {
		// test if it is a mathematical distance measure

		// d(a, a) = 0
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(40, 10, 40, 10) < 1);
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(-3, -10, -3, -10) < 1);
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(0, 0, 0, 0) < 1);

		// d(a, b) = d(b, a)
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(30, 40, 1, 2) == GPSCalc.getDistanceTwoPointsMeter(1,
				2, 30, 40));
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(-10, 20, -1, 0) == GPSCalc.getDistanceTwoPointsMeter(
				-1, 0, -10, -20));

		// d(a, b) + d(b, c) >= d(a, c)
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(-10, 20, -1, 0)
				+ GPSCalc.getDistanceTwoPointsMeter(-1, 0, 10, 22) >= GPSCalc.getDistanceTwoPointsMeter(
				-10, 20, 10, 22));
		assertTrue(GPSCalc.getDistanceTwoPointsMeter(0, 0, -10, 0)
				+ GPSCalc.getDistanceTwoPointsMeter(-10, 0, -30, 70) >= GPSCalc.getDistanceTwoPointsMeter(
				0, 0, -30, 70));

		// some real values
		// assertTrue(GPSCalc.getDistance(52.519018,13.406307, ));

	}

	@Test
	public void testGetGradient() {
		assertEquals(1.0, GPSCalc.getGradient(3, 3, 4, 4));
		assertEquals(-1.0, GPSCalc.getGradient(0, 0, 10, -10));
		assertEquals(0.1, GPSCalc.getGradient(0.1, 0.5, 0.2, 1.5));
		// extra cases
		assertEquals(0.0, GPSCalc.getGradient(0.1, 0.5, 0.1, 1.5));
		assertEquals(Double.POSITIVE_INFINITY,
				GPSCalc.getGradient(0.1, 0, 1.1, 0));
		assertEquals(Double.NEGATIVE_INFINITY,
				GPSCalc.getGradient(0.1, 591, -1.1, 591));
	}

	@Test
	public void testGetPointToPoint() {
		double result = Math.abs(CartesianCalc.getDistancePointToPoint(
				new XYPoint(0, 0), new XYPoint(0, 0)));
		assertTrue("Distance 0,0 to 0,0 is " + result, result < EPSILON);
		result = Math.abs(CartesianCalc.getDistancePointToPoint(new XYPoint(0,
				0), new XYPoint(1, 1)));
		assertTrue("Distance 0,0 to 1,1 is " + result,
				result - 1.4142136 < EPSILON);
		result = Math.abs(CartesianCalc.getDistancePointToPoint(new XYPoint(-1,
				-1), new XYPoint(2.1, 2.5)));
		assertTrue("Distance -1,-1 to 2.1,2.5 is " + result,
				result - 4.67547 < EPSILON);
		result = Math.abs(CartesianCalc.getDistancePointToPoint(new XYPoint(
				-1000, 1), new XYPoint(7482, -2.5)));
		assertTrue(result - 8482 < 1);
		result = Math.abs(CartesianCalc.getDistancePointToPoint(new XYPoint(1,
				1), new XYPoint(1, 10)));
		assertTrue(result - 9 < EPSILON);
	}

	@Test
	public void testGetPointToLine() {
		assertTrue(Math.abs(CartesianCalc.getDistancePointToLine(new XYPoint(3,
				10), new XYPoint(1, 0), new XYPoint(100, 0))) - 10 < EPSILON);
		assertTrue(Math.abs(CartesianCalc.getDistancePointToLine(new XYPoint(0,
				1), new XYPoint(0, 0), new XYPoint(1, 1))) - 0.7071067811 < EPSILON);
		assertTrue(Math.abs(CartesianCalc.getDistancePointToLine(new XYPoint(1,
				0), new XYPoint(0, 0), new XYPoint(1, 1))) - 0.7071067811 < EPSILON);
		assertTrue(Math.abs(CartesianCalc.getDistancePointToLine(new XYPoint(
				-1000, 1), new XYPoint(0, 0), new XYPoint(1, 0))) > 1000);
	}

	@Test
	public void testGetAngleBetweenLines() {
		assertTrue("0 degrees angle, same lines", Math.abs(CartesianCalc
				.getAngleBetweenLines(new XYPoint(-1, -1), new XYPoint(1, 1),
						new XYPoint(-1, -1), new XYPoint(1, 1)) - 0) < EPSILON);
		assertTrue("180 degrees angle, inverse lines",
				Math.abs(CartesianCalc.getAngleBetweenLines(
						new XYPoint(-1, -1), new XYPoint(1, 1), new XYPoint(1,
								1), new XYPoint(-1, -1)) - 180) < EPSILON);
		assertTrue("90 degrees angle, perpendicular lines",
				Math.abs(CartesianCalc.getAngleBetweenLines(new XYPoint(1, -1),
						new XYPoint(-1, 1), new XYPoint(1, 1), new XYPoint(-1,
								-1)) - 90) < EPSILON);
		assertTrue("26 degrees angle, skewed lines",
				Math.abs(CartesianCalc.getAngleBetweenLines(new XYPoint(1, 1),
						new XYPoint(1000, 1), new XYPoint(1, 1), new XYPoint(3,
								2)) - 26.56506117707799) < EPSILON);
	}

	@Test
	public void testGetProjectionPoint() {
		ILocation result = CartesianCalc.getProjectionPoint(new XYPoint(1, 1),
				new XYPoint(0, 0), new XYPoint(6, 0));
		assertTrue(Math.abs(result.getX() - 1) < EPSILON);
		assertTrue(Math.abs(result.getY() - 0) < EPSILON);

		result = CartesianCalc.getProjectionPoint(new XYPoint(2, 4),
				new XYPoint(2, 2), new XYPoint(4, 4));
		assertTrue(Math.abs(result.getX() - 3) < EPSILON);
		assertTrue(Math.abs(result.getY() - 3) < EPSILON);
	}

	@Test
	public void testGetDistancePointToLineGPS() {
		// standard distance with easy projection to the edge
		double result = GPSCalc.getDistancePointToEdgeMeter(new GPSPoint(
				50.737908207781340, 7.107553482055664), new GPSPoint(
				50.738315602709160, 7.113003730773926), new GPSPoint(
				50.739008165953194, 7.112778425216675));
		assertTrue(Math.abs(result - 385.94780017414035) < LARGE_EPSILON);
		// projection to a really close, but very long edge
		result = GPSCalc.getDistancePointToEdgeMeter(new GPSPoint(
				53.566551145258636, 10.278916954994202), new GPSPoint(
				53.565904409287740, 10.279136896133423), new GPSPoint(
				53.567223357879720, 10.278772115707397));
		assertTrue(Math.abs(result - 2.775695740078514) < LARGE_EPSILON);
		// projection is not on edge, so distances to edge end points have to be
		// considered
		result = GPSCalc.getDistancePointToEdgeMeter(new GPSPoint(
				61.006781502315240, 24.466016292572020), new GPSPoint(
				61.001071024045690, 24.451682567596436), new GPSPoint(
				61.001934422718506, 24.456789493560790));
		assertTrue(Math.abs(result - 732.8747157114395) < LARGE_EPSILON);
	}

	@Test
	public void testGetSmallGradientFromEdges() {
		// total match
		double result = GPSCalc.getSmallGradientFromEdges(new XYPoint(0, 0),
				new XYPoint(1, 1), new XYPoint(0, 0), new XYPoint(1, 1));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 0, 10e-3);
		// parallel match
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(0, 0),
				new XYPoint(1, 1), new XYPoint(3, 3), new XYPoint(4, 4));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 0, 10e-3);
		// parallel match with length difference
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(0, 0),
				new XYPoint(1, 1), new XYPoint(3, 3), new XYPoint(7, 7));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 0, 10e-3);
		// with negative gradients
		result = GPSCalc
				.getSmallGradientFromEdges(new XYPoint(1, 1), new XYPoint(-1,
						-1), new XYPoint(3, 3), new XYPoint(-2.2, -2.2));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 0, 10e-3);
		// 90 degrees
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(1, 1),
				new XYPoint(-1, -1), new XYPoint(1, 1), new XYPoint(2, 0));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 2, 10e-3);
		// 180 degrees
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(1, 1),
				new XYPoint(-1, -1), new XYPoint(-1, -1), new XYPoint(1, 1));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 4, 10e-3);
		// 270 degrees
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(1, 1),
				new XYPoint(-1, -1), new XYPoint(1, 1), new XYPoint(0, 2));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 2, 10e-3);
		// small angle spanning over two different quarters
		result = GPSCalc.getSmallGradientFromEdges(new XYPoint(0, 5),
				new XYPoint(-0.01, -10), new XYPoint(0, 5), new XYPoint(0.01,
						-10));
		System.out.println(result);
		assertEqualsWithEpsilon(result, 0, 10e-1);
	}

	private void assertEqualsWithEpsilon(double value, double expected,
			double epsilon) {
		assertTrue(Math.abs(value - expected) < epsilon);
	}
}
