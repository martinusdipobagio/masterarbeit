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
package de.fub.agg2graph.input;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSSegment;

public class GPSCleanTest extends TestCase {
	private GPSCleaner gpsCleaner;
	private GPSSegment segment;
	private CleaningOptions o;

	@Override
	public void setUp() {
		gpsCleaner = new GPSCleaner();
		o = gpsCleaner.getCleaningOptions();
		o.filterByEdgeLength = false;
		o.filterByEdgeLengthIncrease = false;
		o.filterBySegmentLength = false;
		o.filterZigzag = false;
		o.filterFakeCircle = false;
		o.filterOutliers = false;

		File inputFile = FileHandler
				.getFile("test/input/unit-test/clean/clean_01_example.gpx");
		List<GPSSegment> segments = GPXReader.getSegments(inputFile);
		segment = segments.get(0);
	}

	@Test
	public void testNoFilters() {
		// check that the cleaner does not harm the track
		List<GPSSegment> cleanSegments = gpsCleaner.clean(segment);
		assertEquals("Return only one segment", 1, cleanSegments.size());
		assertEquals("Returned segment is of same length as input",
				segment.size(), cleanSegments.get(0).size());
		for (int i = 0; i < segment.size(); i++) {
			assertEquals("Points in segment are unchanged", segment.get(i),
					cleanSegments.get(0).get(i));
		}
	}

	@Test
	public void testFilterSegmentLength() {
		o.filterBySegmentLength = true;
		List<GPSSegment> cleanSegments;
		for (int max = 5; max < 30; max = max + 5) {
			for (int min = 0; min <= max; min = min + 2) {
				// System.out.println("min: " + min);
				// System.out.println("max: " + max);
				o.minSegmentLength = min;
				o.maxSegmentLength = max;
				// check
				cleanSegments = gpsCleaner.clean(segment);
				for (GPSSegment cleanSegment : cleanSegments) {
					// System.out.println("segment size: "
					// + cleanSegment.size());
					assertTrue("Segment length above limit",
							cleanSegment.size() >= min);
					assertTrue("Segment length below limit",
							cleanSegment.size() <= max);
				}
			}
		}
	}

	@Test
	public void testFilterEdgeLength() {
		o.filterByEdgeLength = true;
		List<GPSSegment> cleanSegments;
		for (double min = 0; min < 20; min = (min + 0.01) * 3) {
			for (double max = 5; max < 500; max = max * 4.15) {
				// System.out.println("min: " + min);
				// System.out.println("max: " + max);
				o.minEdgeLength = min;
				o.maxEdgeLength = max;
				// check
				cleanSegments = gpsCleaner.clean(segment);
				for (GPSSegment cleanSegment : cleanSegments) {
					for (int i = 0; i < cleanSegment.size() - 1; i++) {
						double distance = GPSCalc.getDistanceTwoPointsMeter(
								cleanSegment.get(i), cleanSegment.get(i + 1));
						// System.out.println("distance: " + distance);
						assertTrue("Edge length above limit", distance >= min);
						assertTrue("Edge length below limit", distance <= max);
					}
				}
			}
		}
	}

	@Test
	public void testFilterZigzag() {
		o.filterZigzag = true;
		o.maxZigzagAngle = 30;
		List<GPSSegment> cleanSegments = gpsCleaner.clean(segment);
		assertEquals("Point count after zigzag removal (30 degrees)",
				segment.size() - 6, getPointCount(cleanSegments));

		o.maxZigzagAngle = 5;
		cleanSegments = gpsCleaner.clean(segment);
		assertEquals("Point count after zigzag removal (5 degrees)",
				segment.size() - 2, getPointCount(cleanSegments));

		o.maxZigzagAngle = 0;
		cleanSegments = gpsCleaner.clean(segment);
		assertEquals("Point count after zigzag removal (0 degrees)",
				segment.size(), getPointCount(cleanSegments));
	}

	@Test
	public void testFilterFakeCircle() {
		o.filterFakeCircle = true;
		o.maxFakeCircleAngle = 30;
		List<GPSSegment> cleanSegments = gpsCleaner.clean(segment);
		System.out.println(segment.size() + " " + getPointCount(cleanSegments));
		assertEquals("Point count after fake circle removal (30 degree)",
				segment.size() - 3, getPointCount(cleanSegments));

		o.maxFakeCircleAngle = 12;
		cleanSegments = gpsCleaner.clean(segment);
		System.out.println(segment.size() + " " + getPointCount(cleanSegments));
		assertEquals("Point count after fake circle removal (12 degree)",
				segment.size() - 1, getPointCount(cleanSegments));

		o.maxFakeCircleAngle = 0;
		cleanSegments = gpsCleaner.clean(segment);
		System.out.println(segment.size() + " " + getPointCount(cleanSegments));
		assertEquals("Point count after fake circle removal (0 degree)",
				segment.size(), getPointCount(cleanSegments));
	}

	private long getPointCount(List<GPSSegment> list) {
		long sum = 0;
		for (GPSSegment segment : list) {
			sum += segment.size();
		}
		return sum;
	}
}
