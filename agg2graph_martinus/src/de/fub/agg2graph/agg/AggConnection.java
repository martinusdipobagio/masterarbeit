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

import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.tiling.DefaultCachingStrategy;
import de.fub.agg2graph.agg.tiling.Tile;
import de.fub.agg2graph.agg.tiling.TileCache;
import de.fub.agg2graph.agg.tiling.TileManager;
import de.fub.agg2graph.structs.AbstractEdge;
import de.fub.agg2graph.structs.AbstractLocation;
import de.fub.agg2graph.structs.ILocation;

/**
 * A connection between two {@link AggNode}s. The connection can have
 * annotations such as their weight.
 * 
 * @author Johannes Mitlmeier
 * 
 */
public class AggConnection extends AbstractEdge<AggNode> implements Comparable<AggConnection> {
	
	/**
	 * Constructor with given {@link AbstractLocation} and distance
	 * @param from
	 * @param to
	 * @param distance
	 */
	public AggConnection(AggNode from, AggNode to, float distance){
		this.from = from;
		this.to = to;
		this.distance = distance;
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param conn
	 */
	public AggConnection(AggConnection conn) {
		this(conn.getFrom(), conn.getTo(), conn.getAggContainer());
	}
	
	public AggConnection(AggNode from, AggNode to, AggContainer aggContainer) {
		this(from, to, aggContainer, false);
	}

	public AggConnection(AggNode from, AggNode to, AggContainer aggContainer,
			boolean virtual) {
		if (from == null || to == null) {
			return;
		}

		this.from = from;
		this.to = to;

		if (aggContainer != null) {
			setAggContainer(aggContainer);
		}

		if (!virtual) {
			if (from.getOut() != null) {
				from.addOut(this);
			}
			if (to.getIn() != null) {
				to.addIn(this);
			}
		}
	}
	
	public AggConnection(ILocation from, ILocation to, AggContainer aggContainer) {
		init(from.getLat(), from.getLon(), to.getLat(), to.getLon(), aggContainer);
		this.from.setRelevant(from.isRelevant());
	}

	/**
	 * Initialize AggConnection. Sets lat, lon and {@link AggContainer}.
	 * 
	 * @param ID
	 * @param lat
	 * @param lon
	 * @param aggContainer
	 */
	public void init(double fromLat, double fromLon, double toLat, double toLon, AggContainer aggContainer) {
		this.aggContainer = aggContainer;
		this.from = new AggNode(fromLat, fromLon, this.aggContainer);
		this.to = new AggNode(toLat, toLon, this.aggContainer);
		setID(ID);
	}
	
	public static List<AggNode> listToPoints(AggConnection conn) {
		if (conn == null) {
			return null;
		}
		List<AggNode> result = new ArrayList<AggNode>(2);
		result.add(conn.getFrom());
		result.add(conn.getTo());
		return result;
	}

	public static List<AggNode> listToPoints(List<AggConnection> conns) {
		ArrayList<AggNode> result = new ArrayList<AggNode>();
		AggNode lastNode = null, node = null;
		for (AggConnection conn : conns) {
			if (conn == null) {
				continue;
			}
			if (conn.getFrom() != null) {
				node = conn.getFrom();
				if (!node.equals(lastNode)) {
					result.add(node);
					lastNode = node;
				}
			}
			if (conn.getTo() != null) {
				node = conn.getTo();
				if (!node.equals(lastNode)) {
					result.add(node);
					lastNode = node;
				}
			}
		}
		return result;
	}

	// properties
	public double trackCounter = 0;
	private AggContainer aggContainer;
	private float weight = 1;
	private double avgDist = 0;

	/**
	 * Invisible {@link AggConnection}s might be used to hide them before
	 * generating a road network.
	 */
	private boolean visible = true;

	

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AggConnection other = (AggConnection) obj;
		return this.getFrom() != null && other.getFrom() != null
				&& this.getFrom().getID() != null
				&& other.getFrom().getID() != null
				&& this.getFrom().getID().equals(other.getFrom().getID())
				&& this.getTo() != null && other.getTo() != null
				&& this.getTo().getID() != null
				&& other.getTo().getID() != null
				&& this.getTo().getID().equals(other.getTo().getID());
	}

	public void fillFrom(AggNode from) {
		this.from = from;
		from.getOut().add(this);
	}

	public void fillTo(AggNode to) {
		this.to = to;
		to.getIn().add(this);
	}

	public AggContainer getAggContainer() {
		return aggContainer != null ? aggContainer : from.getAggContainer();
	}

	public double getAvgDist() {
		return avgDist;
	}

	@Override
	public AggNode getFrom() {
		return from;
	}

	@Override
	public AggNode getTo() {
		return to;
	}

	public float getWeight() {
		return weight;
	}

	@Override
	public int hashCode() {
		// return getFrom().getID().hashCode() ^ getTo().getID().hashCode();
		int a = 0, b = 0;
		if (getFrom() != null) {
			a = (int) (getFrom().getLat() + getFrom().getLon() * 1000);
			if (getFrom().getID() != null) {
				a = getFrom().getID().hashCode();
			}
		}
		if (getTo() != null) {
			b = (int) (getTo().getLat() + getTo().getLon() * 1000);
			if (getTo().getID() != null) {
				b = getTo().getID().hashCode();
			}
		}
		return a ^ b;
	}

	public void inheritPropertiesFrom(AggConnection conn) {
		setWeight(conn.getWeight());
		setAvgDist(conn.getAvgDist());
	}

	public boolean isComplete() {
		return !ShallowAggNode.class.isInstance(from)
				&& !ShallowAggNode.class.isInstance(to);
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public void makeComplete() {
	}

	public void setAggContainer(AggContainer aggContainer) {
		this.aggContainer = aggContainer;
	}

	public void setAvgDist(double avgDist) {
		this.avgDist = avgDist;
	}

	/**
	 * Change the AggNode the connection originates from. The old from node is
	 * diconnected before the new one is put in place. If the new connection
	 * already exists, both connections are merged via
	 * {@link IAggregationStrategy#mergeConnections(AggConnection, AggConnection)}
	 * .
	 * 
	 * @param from
	 * @return
	 */
	@Override
	public AggConnection setFrom(AggNode from) {
		// disconnect old
		AggConnection changedConn = new AggConnection(from, getTo(),
				aggContainer, true);
		AggConnection oldConn = null;
		for (AggConnection conn : from.getOut()) {
			if (changedConn.equals(conn)) {
				oldConn = conn;
				break;
			}
		}
//		if(this.from != null)
			this.from.getOut().remove(this);
//		if(this.to != null)
			this.to.getIn().remove(this);
		this.from = from;
		if (oldConn == null) {
			// set new
			this.to.addIn(this);
			this.from.addOut(this);
			return this;
		} else {
			oldConn.getFrom().getOut().remove(oldConn);
			oldConn.getTo().getIn().remove(oldConn);
			AggContainer container = getAggContainer();
			AggConnection mergedConn = container.getAggregationStrategy()
					.mergeConnections(changedConn, oldConn);
			container.removeConnection(oldConn);
			// set new
			this.to.getIn().add(mergedConn);
			this.from.getOut().add(mergedConn);
			return changedConn;
		}
	}

	/**
	 * @see AggConnection#setFrom(AggNode)
	 * @param to
	 * @return
	 */
	@Override
	public AggConnection setTo(AggNode to) {
		// disconnect old
		AggConnection changedConn = new AggConnection(getFrom(), to,
				aggContainer, true);
		AggConnection oldConn = null;
		for (AggConnection conn : to.getIn()) {
			if (changedConn.equals(conn)) {
				oldConn = conn;
				break;
			}
		}
		if (this.from != null) {
			this.from.getOut().remove(this);
		}
		if (this.to != null) {
			this.to.getIn().remove(this);
		}
		this.to = to;
		if (oldConn == null) {
			// set new
			this.from.getOut().add(this);
			this.to.addIn(this);
			return this;
		} else {
			oldConn.getFrom().getOut().remove(oldConn);
			oldConn.getTo().getIn().remove(oldConn);
			AggContainer container = getAggContainer();
			AggConnection mergedConn = container.getAggregationStrategy()
					.mergeConnections(changedConn, oldConn);
			container.removeConnection(oldConn);
			// set new
			this.from.addOut(mergedConn);
			this.to.addIn(mergedConn);
			return mergedConn;
		}
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "AggConnection [from=" + getFrom() + ", to=" + getTo() + "]";
	}

	public void tryToFill() {
		if (isComplete()) {
			return;
		}

		TileCache tc = ((DefaultCachingStrategy) aggContainer
				.getCachingStrategy()).getTc();
		TileManager tm = ((DefaultCachingStrategy) aggContainer
				.getCachingStrategy()).getTm();

		if (ShallowAggNode.class.isInstance(from)) {
			// incoming connection for to-node -> shallow connection
			// Tile<AggNode> tile = tm.getTile(from);
			// if (tc.isTileLoaded(tile)) {
			// for (AggNode node : tile.elements) {
			// if (node.equals(from)) {
			// aggContainer.removeConnection(this);
			// aggContainer.addConnection(new AggConnection(node, to));
			// return;
			// }
			// }
			// }
		} else {
			// outgoing connection for from-node -> this should already have
			// options loaded
			Tile<AggNode> tile = tm.getTile(to);
			if (tc.isTileLoaded(tile)) {
				for (AggNode node : tile.elements) {
					if (node.equals(to)) {
						fillTo(node);
						return;
					}
				}
			}
		}
	}

	public void unloadFrom() {
	}

	public void unloadTo() {
	}
	
	public AggConnection[] divide(AggNode middle) {
		AggConnection[] ret = new AggConnection[2];
		//TODO check whether middle is in line
		setTo(middle);
		ret[0] = this;
		ret[1] = new AggConnection(middle, to, this.aggContainer);
		return ret;
	}

	public AggNode at(double t) {
		return new AggNode((1 - t) * from.getLat() + t * to.getLat(), (1 - t)
				* from.getLon() + t * to.getLon(), aggContainer);
	}

	@Override
	public int compareTo(AggConnection o) {
		if (o == null)
			throw new NullPointerException();

		int r = getFrom().compareTo( o.getFrom());
		if (r == 0) {
			return getTo().compareTo( o.getTo());
		} else {
			return r;
		}
	}
}
