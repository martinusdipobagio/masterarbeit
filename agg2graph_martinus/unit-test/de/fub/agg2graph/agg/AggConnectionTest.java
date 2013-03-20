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
import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggNode;

public class AggConnectionTest extends TestCase {

	public void testHashCodeAndEquals() {
		// equal
		AggNode nA = new AggNode("a", 1, 2, null);
		AggNode nB = new AggNode("b", 2, 1, null);
		AggNode nC = new AggNode("c", 1, 1, null);
		AggConnection a = new AggConnection(nA, nB, null);
		AggConnection b = new AggConnection(nA, nB, null);
		AggConnection c = new AggConnection(nC, nB, null);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
		// unequal
		assertFalse(a.hashCode() == c.hashCode());
		assertFalse(a.equals(c));
		assertFalse(c.equals(a));
	}

	public void testEquality() {
		AggNode a = new AggNode("a", 1, 1, null);
		AggNode b = new AggNode("b", 2, 2, null);
		AggConnection ab = new AggConnection(a, b, null);
		AggConnection ba = new AggConnection(b, a, null);

		assertEquals(ab, ab);
		assertEquals(ba, ba);
		assertTrue(!ba.equals(ab));
		assertTrue(!ab.equals(ba));
		assertTrue(!ba.equals("something"));
		assertTrue(!ab.equals("something"));
		assertTrue(!ba.equals(null));
		assertTrue(!ab.equals(null));
		assertEquals(ab, new AggConnection(a, b, null));
		assertEquals(new AggConnection(a, b, null), new AggConnection(a, b,
				null));
	}
}
