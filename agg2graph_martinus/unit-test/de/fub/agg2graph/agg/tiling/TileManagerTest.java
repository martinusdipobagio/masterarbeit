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
package de.fub.agg2graph.agg.tiling;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.agg.AggregationStrategyFactory;
import de.fub.agg2graph.agg.IAggregationStrategy;
import de.fub.agg2graph.structs.GPSCalc;

public class TileManagerTest extends TestCase {
	private TileManager tm;
	private AggContainer agg;

	@Override
	public void setUp() {
		IAggregationStrategy aggStrat = AggregationStrategyFactory.getObject();
		DefaultCachingStrategy cacheStrat = new DefaultCachingStrategy();
		agg = AggContainer.createContainer(new File(
				"test/junit/TileManagerTest"), aggStrat, cacheStrat);
		tm = cacheStrat.getTm();
	}

	public void testInit() {
		assertEquals("Inital world being one (empty) tile", true,
				tm.getRoot().isLeaf);
		assertEquals("Inital world being one (empty) tile", 0, tm.getRoot()
				.getElemCount());
		assertEquals("Inital world size when being one (empty) tile",
				TileManager.WORLD.getMinX(), tm.getRoot().getSize().getMinX());
		assertEquals("Inital world size when being one (empty) tile",
				TileManager.WORLD.getMaxX(), tm.getRoot().getSize().getMaxX());
		assertEquals("Inital world size when being one (empty) tile",
				TileManager.WORLD.getMinY(), tm.getRoot().getSize().getMinY());
		assertEquals("Inital world size when being one (empty) tile",
				TileManager.WORLD.getMaxY(), tm.getRoot().getSize().getMaxY());
	}

	public void testAdd() {
		int counter = 0;
		for (double i = -20.001; i < 20; i = i + 2) {
			tm.addElement(new AggNode(i, i, agg));
			tm.addElement(new AggNode(i, -i, agg));
			counter += 2;
			assertEquals("Number of elements after insertion", counter, tm
					.getRoot().getElemCount());
		}
	}

	public void testAddClose() {
		tm.maxElementsPerTile = 10;
		for (double lat = 52.12345678; lat < 52.1235; lat += 1E-6) {
			for (double lon = 13.23456789; lon < 13.2346; lon += 1E-6) {
				tm.addElement(new AggNode(lat, lon, agg));
			}
		}
		// the splits might fail and thus the test
	}

	public void testMoveNode() {
		List<AggNode> nodes = new ArrayList<AggNode>(100);
		tm.maxElementsPerTile = 10; // make sure it splits!
		tm.doMerge = true;
		tm.mergeFactor = 0.5;
		// create nodes
		for (double lat = 52.0; lat < 62.0; lat += 1) {
			for (double lon = 13.0; lon < 23.0; lon += 1) {
				AggNode node = new AggNode(lat, lon, agg);
				nodes.add(node);
				tm.addElement(node);
			}
		}
		// System.out.println(tm);
		// move the nodes to the other end of the world (by GPS)
		// tests
		Tile<AggNode> tileBefore = null;
		Tile<AggNode> tileAfter = null;
		for (AggNode node : nodes) {
			tileBefore = tm.getTile(node);
			node.setLatLon(-node.getLat(), -node.getLon());
			tileAfter = tm.getTile(node);
			assertTrue(
					"Node has been moved to another tile by changing its latlon pair",
					!tileBefore.equals(tileAfter));
		}
		// System.out.println(tm);
		assertEquals("Old tile empty", 0, tileBefore.elements.size());
		assertTrue("New tile non-empty", tileAfter.elements.size() > 0);
	}

	public void testSplit() {
		tm.maxElementsPerTile = 10;
		for (int i = -5; i < 5; i++) {
			tm.addElement(new AggNode(i, i, agg));
		}
		// no split yet!
		assertEquals("Full tile not split yet", true, tm.getRoot().isLeaf);
		assertEquals("Full tile", tm.maxElementsPerTile, tm.getRoot()
				.getElemCount());
		// System.out.println(tm);
		// dit it split now?
		tm.addElement(new AggNode(0.15, 5.105, agg));
		assertEquals("Overfull tile split", false, tm.getRoot().isLeaf);
		assertEquals("Overfull tile element count still correct",
				tm.maxElementsPerTile + 1, tm.getRoot().getElemCount());
		// System.out.println(tm);
	}

	public void testFindTile() {
		tm.maxElementsPerTile = 5;
		List<AggNode> nodes = new ArrayList<AggNode>();
		for (double i = -30; i < 30; i = i + 0.1) {
			AggNode node = new AggNode(i, i, agg);
			nodes.add(node);
			tm.addElement(node);
			tm.addElement(new AggNode(i, -i, agg));
		}
		// tests
		for (AggNode searchNode : nodes) {
			Tile<AggNode> tile = tm.getTile(searchNode);
			// some points are outside of the world
			if (tile == null) {
				continue;
			}
			assertTrue("Node is really supposed to be in this tile",
					searchNode.getLat() >= tile.getSize().getMinX());
			assertTrue("Node is really supposed to be in this tile",
					searchNode.getLat() <= tile.getSize().getMaxX());
			assertTrue("Node is really supposed to be in this tile",
					searchNode.getLon() >= tile.getSize().getMinY());
			assertTrue("Node is really supposed to be in this tile",
					searchNode.getLon() <= tile.getSize().getMaxY());
		}
	}

	public void testDistance() {
		tm.maxElementsPerTile = 10;
		for (double i = -30; i < 30; i = i + 0.1) {
			tm.addElement(new AggNode(i, i, agg));
			tm.addElement(new AggNode(i, -i, agg));
		}
		// tests
		assertEquals("Empty result set for 0 distance on non-existing node", 0,
				tm.getCloseElements(new AggNode(0.001, 0.001, agg), 0).size());

		for (double i = -30; i < 30; i = i + 3.34) {
			AggNode node = new AggNode(i, i, null);
			double maxDist = Math.abs(i) * 1000;
			Set<AggNode> result = tm.getCloseElements(node, maxDist);
			for (AggNode resultNode : result) {
				assertTrue(
						String.format(
								"Returned node's distance is in maxDist range to target node (%f <= %f)",
								GPSCalc.getDistanceTwoPointsMeter(node, resultNode), maxDist),
						GPSCalc.getDistanceTwoPointsMeter(node, resultNode) <= maxDist);
			}
		}
	}

	public void testClip() {
		tm.maxElementsPerTile = 17;
		tm.splitFactor = 2;
		for (double i = -30.001; i < 30; i = i + 0.1) {
			tm.addElement(new AggNode(i, i, agg));
			tm.addElement(new AggNode(i, -i, agg));
		}
		// tests
		int counter = 2;
		for (double i = 0.01; i < 15.1; i = i + 0.1) {
			Rectangle2D.Double region = new Rectangle2D.Double(-i, -i, i * 2,
					i * 2);
			List<AggNode> result = tm.clipRegion(region);
			assertEquals("Clipping with spanning multiple tiles", counter,
					result.size());
			counter += 4;
		}
	}

	public void testRemoveMerge() {
		tm.maxElementsPerTile = 10;
		tm.splitFactor = 4;
		tm.doMerge = true;
		tm.mergeFactor = 0.5;
		List<AggNode> nodes = new ArrayList<AggNode>(50);
		AggNode nodeA;
		AggNode nodeB;
		for (double i = -5; i < 5; i = i + 0.7) {
			nodeA = new AggNode(i / 5, i, agg);
			nodeB = new AggNode(i / 7, -i, agg);
			nodes.add(nodeA);
			nodes.add(nodeB);
			tm.addElement(nodeA);
			tm.addElement(nodeB);
		}
		// tests
		int sum = tm.getNodeCount();
		for (AggNode node : nodes) {
			tm.removeElement(node);
			sum--;
			assertEquals(
					"Node count decreased by one after removing an element",
					sum, tm.getNodeCount());
			assertEquals(
					"Real number of nodes decreased by one after removing an element",
					sum, tm.getRoot().getInnerNodes().size());
		}
		assertEquals(
				"After removing all nodes, the root tile is the world because of merges",
				tm.getRoot().getSize(), TileManager.WORLD);
	}

}
