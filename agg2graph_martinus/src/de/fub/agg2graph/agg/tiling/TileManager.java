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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.fub.agg2graph.agg.AggConnection;
import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.agg.AggNode;
import de.fub.agg2graph.input.FileHandler;
import de.fub.agg2graph.structs.GPSCalc;
import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.IEdge;
import de.fub.agg2graph.structs.ILocation;
import de.fub.agg2graph.structs.projection.OsmProjection;

/**
 * Managing tiles: adding and removing nodes, proximity searches.
 * 
 * @author Johannes Mitlmeier
 * 
 * @param
 */
public class TileManager {
	private Tile<AggNode> root;
	public int splitFactor = 3;
	public boolean doMerge = false;
	public double mergeFactor = 0.7;
	public int maxElementsPerTile = 5000;
	public Rectangle2D.Double minimumSplitSize = new Rectangle2D.Double(0, 0,
			10e-3, 10e-3);
	public int nodeCounter = 0;
	private int connCounter = 0;
	private AggContainer agg;
	// lon = "width", first. lat = "height", last.
	public static Rectangle2D.Double WORLD = new Rectangle2D.Double(-160, -80,
			320, 160);
	private DefaultCachingStrategy dcs;

	public TileManager(DefaultCachingStrategy dcs) {
		this.dcs = dcs;
	}

	public TileManager(DefaultCachingStrategy dcs, AggContainer agg,
			int splitFactor) {
		this(dcs);
		this.agg = agg;
		this.splitFactor = splitFactor;
	}

	public Tile<AggNode> getTile(ILocation loc) {
		Tile<AggNode> currentTile = getRoot();
		while (true) {
			if (currentTile.isLeaf) {
				if (!((DefaultCachingStrategy) agg.getCachingStrategy())
						.getTc().isInMemory()
						&& !(new File(agg.getDataSource() + File.separator
								+ currentTile.getID() + ".xml")).exists()
						&& !isEmpty()) {
					currentTile.split();
				} else {
					assert currentTile.size.contains(new Point2D.Double(loc
							.getLat(), loc.getLon()));
					return currentTile;
				}
			}

			// where to go to for further checking?
			currentTile = currentTile.getSubTile(loc);
			if (currentTile == null) {
				return null;
			}
		}
	}

	public boolean isEmpty() {
		return agg == null || agg.getDataSource() == null
				|| agg.getDataSource().list(FileHandler.gpxFilter) == null
				|| agg.getDataSource().list(FileHandler.gpxFilter).length == 0;
	}

	public Tile<AggNode> getRoot() {
		return root;
	}

	/**
	 * Get a node when having only its ID. This method first determines in which
	 * tile the node would have to be and then searches that tile.
	 * 
	 * @param fullID
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public AggNode getNodeByFullID(String fullID)
			throws ParserConfigurationException, SAXException, IOException {
		String[] parts = fullID.split(":");
		GPSPoint searchPoint = new GPSPoint(Double.parseDouble(parts[0]),
				Double.parseDouble(parts[1]));
		Tile<AggNode> tile = getTile(searchPoint);
		dcs.tc.loadTile(tile);
		for (AggNode elem : tile.elements) {
			if (parts[2].equals(elem.getID())) {
				return elem;
			}
		}
		return null;
	}

	public Tile<AggNode> addElement(AggNode elem) {
		Tile<AggNode> targetTile = getTile(elem);
		if (targetTile == null) {
			return null;
		}
		try {
			dcs.getTc().loadTile(targetTile);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		targetTile.elements.add(elem);
		nodeCounter++;
		// if (AggNode.class.isInstance(elem) && elem.getOut() != null) {
		// connCounter += elem.getOut().size();
		// }

		// do we need to split?
		if (targetTile != null
				&& targetTile.getElemCount() > maxElementsPerTile
				&& targetTile.size.getWidth() >= minimumSplitSize.getWidth()
				&& targetTile.size.getHeight() >= minimumSplitSize.getHeight()) {
			targetTile.split();
		}
		return targetTile;
	}

	public void removeElement(AggNode elem) {
		Tile<AggNode> targetTile = getTile(elem);
		targetTile.elements.remove(elem);
		nodeCounter--;
		// do we need to merge?
		if (doMerge
				&& targetTile.parent != null
				&& targetTile.parent.getElemCount() < maxElementsPerTile
						* mergeFactor) {
			mergeTile(targetTile.parent);
		}
	}

	public void addConnection(AggConnection conn) {
		connCounter++;
	}

	public void removeConnection(AggConnection conn) {
		connCounter--;
	}

	public List<AggNode> clipRegionProjected(Rectangle2D.Double projectedRect) {
		GPSPoint topLeftProjNode = OsmProjection.cartesianToGps(
				projectedRect.getMinX(), projectedRect.getMinY());
		GPSPoint bottomRightProjNode = OsmProjection.cartesianToGps(
				projectedRect.getMaxX(), projectedRect.getMaxY());
		return clipRegion(new Rectangle2D.Double(topLeftProjNode.getLat(),
				topLeftProjNode.getLon(), bottomRightProjNode.getLat()
						- topLeftProjNode.getLat(),
				bottomRightProjNode.getLon() - topLeftProjNode.getLon()));
	}

	/**
	 * Get all nodes in a specified region.
	 * 
	 * @param gpsRect
	 * @return
	 */
	public List<AggNode> clipRegion(Rectangle2D.Double gpsRect) {
		Queue<Tile<AggNode>> tileQueue = new LinkedList<Tile<AggNode>>();
		tileQueue.add(getRoot());
		Tile<AggNode> currentTile;
		List<AggNode> result = new ArrayList<AggNode>(maxElementsPerTile);
		while (tileQueue.size() > 0) {
			currentTile = tileQueue.poll();
			if (!currentTile.isLeaf) {
				Rectangle2D.Double overlap = new Rectangle2D.Double();
				for (Tile<AggNode> subTile : currentTile.children) {
					Rectangle2D.Double.intersect(gpsRect, currentTile.size,
							overlap);
					// full overlap?
					if (overlap.equals(currentTile.size)) {
						try {
							dcs.getTc().loadTile(subTile);
						} catch (ParserConfigurationException e) {
							e.printStackTrace();
						} catch (SAXException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						result.addAll(subTile.getInnerNodes());
					} else
					// partial overlap?
					if (overlap.getWidth() > 0) {
						try {
							dcs.getTc().loadTile(subTile);
						} catch (ParserConfigurationException e) {
							e.printStackTrace();
						} catch (SAXException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						tileQueue.add(subTile);
					} else
					// no overlap
					{
						// nothing ;)
					}
				}
			} else {
				Rectangle2D.Double overlap = new Rectangle2D.Double();
				Rectangle2D.Double
						.intersect(gpsRect, currentTile.size, overlap);
				// full overlap?
				if (overlap.equals(currentTile.size)) {
					result.addAll(currentTile.elements);
				} else
				// partial overlap?
				if (overlap.getWidth() > 0) {
					// check all points
					for (AggNode elem : currentTile.elements) {
						if (gpsRect.contains(new Point2D.Double(elem.getLat(),
								elem.getLon()))) {
							result.add(elem);
						}
					}
				}
			}
		}
		return result;
	}

	public void mergeTile(Tile<AggNode> tile) {
		tile.elements = tile.getInnerNodes();
		tile.isLeaf = true;
		tile.children = null;
	}

	/**
	 * Get all nodes which are close to one node. Attention: This method may be
	 * a little inaccurate for performance reasons.
	 * 
	 * @param loc
	 * @param maxDist
	 * @return
	 */
	public Set<AggNode> getCloseElements(ILocation loc, double maxDist) {
		Set<AggNode> result = new HashSet<AggNode>(10);
		Set<AggNode> candidates = new HashSet<AggNode>(10);
		// drill down...
		Tile<AggNode> currentTile = getTile(loc);
		// point searched was outside the world or otherwise inaccessible
		if (currentTile == null) {
			return result;
		}
		// and search up!
		while (true) {
			// TODO this can be optimized by taking directions into account, but
			// does it help that much?
			Rectangle2D.Double size = currentTile.getSize();
			double minBorderDist = Double.MAX_VALUE;
			double maxBorderDist = 0;
			ILocation top = new GPSPoint(size.getMinY(), loc.getLon());
			ILocation bottom = new GPSPoint(size.getMaxY(), loc.getLon());
			ILocation left = new GPSPoint(loc.getLat(), size.getMinX());
			ILocation right = new GPSPoint(loc.getLat(), size.getMaxX());
			double[] borderDistances = new double[] {
					GPSCalc.getDistanceTwoPointsMeter(loc, top),
					GPSCalc.getDistanceTwoPointsMeter(loc, bottom),
					GPSCalc.getDistanceTwoPointsMeter(loc, right),
					GPSCalc.getDistanceTwoPointsMeter(loc, left) };
			for (double dist : borderDistances) {
				minBorderDist = Math.min(minBorderDist, dist);
				maxBorderDist = Math.max(maxBorderDist, dist);
			}
			double minCornerDist = Double.MAX_VALUE;
			double maxCornerDist = 0;
			ILocation topLeft = new GPSPoint(size.getMinY(), size.getMinX());
			ILocation bottomLeft = new GPSPoint(size.getMinY(), size.getMaxX());
			ILocation topRight = new GPSPoint(size.getMaxY(), size.getMinX());
			ILocation bottomRight = new GPSPoint(size.getMaxY(), size.getMaxX());
			double[] cornerDistances = new double[] {
					GPSCalc.getDistanceTwoPointsMeter(loc, topLeft),
					GPSCalc.getDistanceTwoPointsMeter(loc, bottomLeft),
					GPSCalc.getDistanceTwoPointsMeter(loc, topRight),
					GPSCalc.getDistanceTwoPointsMeter(loc, bottomRight) };
			for (double dist : cornerDistances) {
				minCornerDist = Math.min(minCornerDist, dist);
				maxCornerDist = Math.max(maxCornerDist, dist);
			}
			// all borders are further -> no more interesting points
			if (minCornerDist > maxDist) {
				candidates.addAll(currentTile.getInnerNodes());
				break;
			} else
			// all borders closer than maxDist -> all points good
			if (maxBorderDist < maxDist) {
				// result.addAll(currentTile.getInnerNodes());
			} else {
				candidates.addAll(currentTile.getInnerNodes());
			}
			currentTile = currentTile.parent;
			if (currentTile == null) {
				break;
			}
		}
		// closely check all candidates
		for (AggNode candidateElem : candidates) {
			if (!result.contains(candidateElem) && candidateElem != loc) {
				double calcDist = GPSCalc.getDistanceTwoPointsMeter(candidateElem, loc);
				if (calcDist < maxDist) {
					result.add(candidateElem);
				}
			}
		}
		return result;
	}

	public int getNodeCount() {
		return nodeCounter;
	}

	@Override
	public String toString() {
		return root.toString();
	}

	public String toDebugString() {
		return root.toDebugString();
	}

	public void clear() {
		nodeCounter = 0;
		connCounter = 0;
		dcs.getTc().clear();
		root = new Tile<AggNode>(this, dcs.getTc(), null, WORLD);
		root.setID("0");
		if (dcs.getTc().isInMemory()) {
			try {
				dcs.getTc().loadTile(root);
			} catch (ParserConfigurationException e) {
			} catch (SAXException e) {
			} catch (IOException e) {
			}
		}
	}

	public int getConnectionCount() {
		return connCounter;
	}

	public void setAggContainer(AggContainer agg) {
		this.agg = agg;
	}

	public List<Tile<AggNode>> clipTilesProjected(Rectangle2D.Double clipArea) {
		GPSPoint topLeftProjNode = OsmProjection.cartesianToGps(
				clipArea.getMinX(), clipArea.getMinY());
		GPSPoint bottomRightProjNode = OsmProjection.cartesianToGps(
				clipArea.getMaxX(), clipArea.getMaxY());
		return clipTiles(new Rectangle2D.Double(topLeftProjNode.getLat(),
				topLeftProjNode.getLon(), bottomRightProjNode.getLat()
						- topLeftProjNode.getLat(),
				bottomRightProjNode.getLon() - topLeftProjNode.getLon()));
	}

	/**
	 * Get all tiles overlapping an area.
	 * 
	 * @param clipArea
	 * @return
	 */
	public List<Tile<AggNode>> clipTiles(Rectangle2D.Double clipArea) {
		List<Tile<AggNode>> result = new ArrayList<Tile<AggNode>>();
		Queue<Tile<AggNode>> tileQueue = new LinkedList<Tile<AggNode>>();
		tileQueue.add(getRoot());
		Tile<AggNode> currentTile;
		while (tileQueue.size() > 0) {
			currentTile = tileQueue.poll();
			if (currentTile.isLeaf) {
				result.add(currentTile);
			} else {
				Rectangle2D.Double overlap = new Rectangle2D.Double();
				for (Tile<AggNode> subTile : currentTile.children) {
					Rectangle2D.Double.intersect(clipArea, subTile.size,
							overlap);
					// full overlap?
					if (overlap.equals(subTile.size)) {
						result.add(subTile);
					} else if (overlap.width > 0 && overlap.height > 0) {
						// partial overlap
						tileQueue.add(subTile);
					} else {
						// no overlap
						// do nothing!
					}
				}
			}
		}
		return result;
	}

	public Set<AggConnection> getCloseConnections(
			IEdge<? extends ILocation> edge, double maxDist) {
		Set<AggNode> setFrom = this.getCloseElements(edge.getFrom(), maxDist);
		Set<AggNode> setTo = this.getCloseElements(edge.getTo(), maxDist);
		Set<AggConnection> setClose = new HashSet<AggConnection>();
		Iterator<AggNode> itFrom = setFrom.iterator();
		while (itFrom.hasNext()) {
			AggNode from = itFrom.next();
			Iterator<AggConnection> itOut = from.getOut().iterator();
			while (itOut.hasNext()) {
				AggConnection conn = itOut.next();
				if (setTo.contains(conn.getTo())) {
					setClose.add(conn);
				}
			}
		}
		return setClose;
	}

	public void addConnectionCounter(int i) {
		connCounter += i;
	}
}
