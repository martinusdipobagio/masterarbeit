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

public class AggNodeTest extends TestCase {

	public void testHashCodeAndEquals() {
		// equal
		AggNode a = new AggNode("a", 1, 2, null);
		AggNode b = new AggNode("a", 1, 2, null);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		// unequal
		b.setID("b");
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		// equal
		b.setID("a");
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		// unequal
		a.setLat(18);
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		// equal
		a.setLat(1);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		// unequal
		b.setLon(18);
		assertFalse(a.hashCode() == b.hashCode());
		assertFalse(a.equals(b));
		assertFalse(b.equals(a));
		// equal
		b.setLon(2);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}

	public void testEquality() {
		AggNode a = new AggNode("a", 1, 1, null);
		AggNode b = new AggNode("b", 2, 2, null);

		assertEquals(a, a);
		assertEquals(b, b);
		assertTrue(!b.equals(a));
		assertTrue(!a.equals(b));
		assertTrue(!b.equals("something"));
		assertTrue(!a.equals("something"));
		assertTrue(!b.equals(null));
		assertTrue(!a.equals(null));
		assertEquals(a, new AggNode("a", 1, 1, null));
		assertTrue(!a.equals(new AggNode("a", 2, 2, null)));
		assertEquals(new AggNode(null, 1, 1, null), new AggNode(null, 1, 1,
				null));
	}
}
