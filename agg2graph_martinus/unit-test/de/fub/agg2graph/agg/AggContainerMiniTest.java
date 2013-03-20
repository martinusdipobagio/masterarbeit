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

import junit.framework.TestCase;
import de.fub.agg2graph.agg.tiling.CachingStrategyFactory;

public class AggContainerMiniTest extends TestCase {
	private AggContainer container = null;
	private final AggNode a = new AggNode("a", 1, 1, container);
	private final AggNode b = new AggNode("b", 2, 2, container);
	private final AggNode c = new AggNode("c", -3, -3, container);
	private AggConnection ab;

	@Override
	protected void setUp() {
		container = AggContainer.createContainer(null,
				AggregationStrategyFactory.getObject(),
				CachingStrategyFactory.getObject());
		container.addNode(a);
		container.addNode(b);
		container.addNode(c);
		ab = container.connect(a, b);
		container.connect(b, c);
	}

	public void testAggContainerMove() {
		// change connection endpoints (a <=> b)
		// System.out.println(container);
		int before = container.getCachingStrategy().getConnectionCount();
		// System.out.println(before);
		ab = ab.setTo(c);
		// System.out.println(container);
		assertEquals(2, before);
		assertEquals(before, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(a, ab.getFrom());
		assertEquals(c, ab.getTo());
		ab = ab.setFrom(b);
		// System.out.println(container);
		assertEquals(before - 1, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(b, ab.getFrom());
		assertEquals(c, ab.getTo());
		ab = ab.setFrom(a);
		// System.out.println(container);
		assertEquals(before - 1, container.getCachingStrategy()
				.getConnectionCount());
		assertEquals(a, ab.getFrom());
		assertEquals(c, ab.getTo());
	}
}
