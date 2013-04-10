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
package de.fub.agg2graph.agg;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import de.fub.agg2graph.agg.tiling.CachingStrategyFactory;

public class AggContainerTest extends TestCase {
	private AggContainer container = null;
	private final AggNode a = new AggNode("a", 1, 1, container);
	private final AggNode b = new AggNode("b", 2, 2, container);
	private final AggNode c = new AggNode("c", -3, -3, container);
	private final AggNode d = new AggNode("d", 0, 0, container);
	private final AggNode e = new AggNode("e", 10, -60, container);
	private AggConnection ab, ba, ac, bc, bd, cd;

	@Override
	protected void setUp() {
		container = AggContainer.createContainer(new File("test/agg/unittest"),
				AggregationStrategyFactory.getObject(),
				CachingStrategyFactory.getObject());
		container.clear();
		container.addNode(a);
		container.addNode(b);
		container.addNode(c);
		container.addNode(d);
		ab = container.connect(a, b);
		ba = container.connect(b, a);
		ac = container.connect(a, c);
		bc = container.connect(b, c);
		bd = container.connect(b, d);
		cd = container.connect(c, d);
	}

	public void testAggContainerAdd() {
		assertEquals(4, container.getCachingStrategy().getNodeCount());
		assertEquals(6, container.getCachingStrategy().getConnectionCount());
		verifyInOutCount(a, 1, 2);
		verifyInOutCount(b, 1, 3);
		verifyInOutCount(c, 2, 1);
		verifyInOutCount(d, 2, 0);

		container.addConnection(ab);
		container.addConnection(ac);
		container.addConnection(ba);
		container.addConnection(bc);
		container.addConnection(cd);
		container.addConnection(bd);

		assertEquals(4, container.getCachingStrategy().getNodeCount());
		assertEquals(6, container.getCachingStrategy().getConnectionCount());
		verifyInOutCount(a, 1, 2);
		verifyInOutCount(b, 1, 3);
		verifyInOutCount(c, 2, 1);
		verifyInOutCount(d, 2, 0);
	}

	public void testAggContainerMoveNode() {
		double oldLat = a.getLat();
		double oldLon = a.getLon();
		verifyNodeConnectionCount(4, 6);
		container.moveNodeTo(a, b);
		verifyNodeConnectionCount(4, 6);
		assertTrue("After move", a.getLat() == b.getLat());
		assertTrue("After move", a.getLon() == b.getLon());
		assertTrue("After move", a.getLat() != oldLat);
		assertTrue("After move", a.getLon() != oldLon);
	}

	public void testAggContainerChangeConnections() {
		// move a node around (keeping connections!)
		container.moveNodeTo(a, b);
		assertEquals(a.getLat(), b.getLat());
		assertEquals(a.getLon(), b.getLon());
		assertEquals(a.getX(), b.getX());
		assertEquals(a.getY(), b.getY());

		// change connection endpoints (a <=> b)
		int before = container.getCachingStrategy().getConnectionCount();
		ac = ac.setTo(d);
		assertEquals(before, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(a, ac.getFrom());
		assertEquals(d, ac.getTo());
		ac = ac.setTo(c);
		ac = ac.setFrom(b);
		assertEquals(before - 1, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(b, ac.getFrom());
		assertEquals(c, ac.getTo());
		ac = ac.setFrom(a);
		assertEquals(before - 1, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(a, ac.getFrom());
		assertEquals(c, ac.getTo());
	}

	public void testAggContainerMerge() {
		// merge nodes
		container.mergeNodes(b, c);
		verifyNodeConnectionCount(3, 3);
		verifyNodeConnectionCount(3, 3);
		verifyInOutCount(a, 1, 1);
		verifyInOutCount(c, 1, 2);
		verifyInOutCount(d, 1, 0);
	}

	public void testAggContainerExtract() {
		// extract node
		container.extractNode(c);
		verifyNodeConnectionCount(3, 4);
		verifyInOutCount(a, 1, 2);
		verifyInOutCount(b, 1, 2);
		verifyInOutCount(d, 2, 0);
	}

	public void testAggContainerInsertNode() {
		container.insertNode(e, a, b);
		verifyNodeConnectionCount(5, 8);
		verifyInOutCount(a, 1, 2);
		verifyInOutCount(b, 1, 3);
		verifyInOutCount(c, 2, 1);
		verifyInOutCount(d, 2, 0);
		verifyInOutCount(e, 2, 2);
	}

	public void testAggContainerRemove() {
		// remove connection
		container.removeConnection(ab);
		verifyNodeConnectionCount(4, 5);
		container.removeConnection(bc);
		verifyNodeConnectionCount(4, 4);

		// remove node
		container.deleteNode(b);
		verifyNodeConnectionCount(3, 2);
		container.deleteNode(a);
		container.deleteNode(c);
		container.deleteNode(d);
		// is it empty?
		verifyNodeConnectionCount(0, 0);
	}

	public void testAggContainerSplit() {
		verifyNodeConnectionCount(4, 6);
		List<AggConnection> result = container.splitConnection(ab, 1);
		verifyNodeConnectionCount(4, 6);
		assertEquals(1, result.size());

		result = container.splitConnection(ab, 3);
		assertEquals(3, result.size());
		verifyNodeConnectionCount(6, 8);
	}

	private void verifyNodeConnectionCount(int nodeCount, int connectionCount) {
		assertEquals(nodeCount, container.getCachingStrategy().getNodeCount());
		assertEquals(connectionCount, container.getCachingStrategy()
				.getConnectionCount());
	}

	private void verifyInOutCount(AggNode node, int inCount, int outCount) {
		assertEquals(inCount, node.getIn().size());
		assertEquals(outCount, node.getOut().size());
	}

}
