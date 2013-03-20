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
package de.fub.agg2graph.graph;

import java.util.List;

import junit.framework.TestCase;
import de.fub.agg2graph.input.FileHandler;
import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.ILocation;

public class RamerDouglasPeuckerFilterTest extends TestCase {
	private List<? extends ILocation> input;

	@Override
	public void setUp() {
		List<GPSSegment> seg = GPXReader.getSegments(FileHandler
				.getFile("test/input/unit-test/rdpf/RamerDouglasPeucker.gpx"));
		input = seg.get(0);
	}

	public void testRamerDouglasPeucker() {
		int[] expected = new int[] { 3, 6, 14, 22, 31, 42, 55, 62, 65, 67 };
		RamerDouglasPeuckerFilter rdpf = new RamerDouglasPeuckerFilter(10e-10);
		double e = 10e-4;
		for (int i = 0; i < 10; i++) {
			e = (e + 0.1) * 2.0;
			rdpf.setEpsilon(e);
			assertEquals(String.format("Point count for eps=%f", e),
					expected[i], rdpf.getRemovablePoints(input).size());
		}
	}
}
