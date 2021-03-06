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
package de.fub.agg2graph.ui.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.fub.agg2graph.agg.AggContainer;
import de.fub.agg2graph.graph.RamerDouglasPeuckerFilter;
import de.fub.agg2graph.input.CleaningOptions;
import de.fub.agg2graph.input.GPSCleaner;
import de.fub.agg2graph.input.GPSFilter;
import de.fub.agg2graph.osm.ExporterFactory;
import de.fub.agg2graph.roadgen.RoadNetwork;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.ui.StepStorage;
import de.fub.agg2graph.ui.gui.RenderingOptions.RenderingType;

public class UIStepStorage extends StepStorage {
	// data
	public List<GPSSegment> inputSegmentList = new ArrayList<GPSSegment>();
	public List<GPSSegment> filterSegmentList = new ArrayList<GPSSegment>();
	public List<GPSSegment> cleanSegmentList = new ArrayList<GPSSegment>();

	// layers
	public Layer rawLayer;
	public Layer cleanLayer;
	public Layer aggLayer;
	public Layer intersectionLayer;
	public Layer roadLayer;
	private TestUI ui;
	public int levelReached = 1;

	public UIStepStorage(TestUI ui) {
		setGpsCleaner(new GPSCleaner());
		setGpsFilter(new GPSFilter());
		setCleaningRamerDouglasPeuckerFilter(new RamerDouglasPeuckerFilter(5));
		setExportRamerDouglasPeuckerFilter(new RamerDouglasPeuckerFilter(15));
		setRoadNetwork(new RoadNetwork());
		setExporter(ExporterFactory.getObject());

		this.ui = ui;
		LayerManager lm = ui.getLayerManager();

		// init layers
		RenderingOptions x = new RenderingOptions();
		x.color = new Color(97, 123, 228); // blue
		x.renderingType = RenderingType.ALL;
		x.zIndex = -1;
		x.opacity = 1;
		rawLayer = new Layer("input", "Raw gps data", x);
		lm.addLayerToPanel(rawLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(rawLayer, ui.getMainPanel());

		x = new RenderingOptions();
		x.color = new Color(39, 172, 88); // green
		x.renderingType = RenderingType.ALL;
		x.zIndex = 0;
		x.opacity = 1;
		cleanLayer = new Layer("clean", "Clean gps data", x);
		lm.addLayerToPanel(cleanLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(cleanLayer, ui.getMainPanel());

		x = new RenderingOptions();
		x.color = new Color(232, 23, 79); // red
		x.zIndex = 1;
		x.opacity = 0.7;
		Layer matchingLayer = new Layer("matching", "Matching", x);
		lm.addLayerToPanel(matchingLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(matchingLayer, ui.getMainPanel());

		x = new RenderingOptions();
//		x.color = new Color(240, 225, 17); // yellow/orange
		x.color = new Color(218, 165, 32); // yellow/orange
		x.zIndex = 2;
		x.opacity = 0.7;
		Layer mergingLayer = new Layer("merging", "Merging", x);
		lm.addLayerToPanel(mergingLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(mergingLayer, ui.getMainPanel());

		x = new RenderingOptions();
		x.color = new Color(38, 36, 5); // black
		x.renderingType = RenderingType.ALL;
		x.zIndex = 3;
		x.opacity = 1;
		aggLayer = new Layer("agg", "Aggregation", x);
		lm.addLayerToPanel(aggLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(aggLayer, ui.getMainPanel());

		x = new RenderingOptions();
		x.color = new Color(137, 0, 255); // dark blue, semi transparent!
		x.renderingType = RenderingType.POINTS;
		x.zIndex = 4;
		x.opacity = 0.5;
		x.strokeBaseWidthFactor = 25;
		intersectionLayer = new Layer("intersections", "Intersections", x);
		lm.addLayerToPanel(intersectionLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(intersectionLayer, ui.getMainPanel());

		x = new RenderingOptions();
		x.renderingType = RenderingType.ALL;
		x.zIndex = 5;
		x.opacity = 1;
		x.strokeBaseWidthFactor = 1.5f;
		roadLayer = new Layer("road", "Roads", x);
		lm.addLayerToPanel(roadLayer, new RenderingPanel(ui));
		lm.addLayerToPanel(roadLayer, ui.getMainPanel());

		// some initial values
		CleaningOptions o = getGpsCleaner().getCleaningOptions();
		o.filterBySegmentLength = true;
		o.minSegmentLength = 1;
		o.maxSegmentLength = 100;
		o.filterByEdgeLength = true;
		o.minEdgeLength = 0.3;
		o.maxEdgeLength = 750;
		o.filterZigzag = true;
		o.maxZigzagAngle = 20;
		o.filterFakeCircle = true;
		o.maxFakeCircleAngle = 50;
		o.filterOutliers = false;
		o.maxNumOutliers = 2;
		//TODO Tinus
		o.useMin = true;
		o.useMean = false;
		o.onlySame = false;
	}

	@Override
	public void setAggContainer(AggContainer aggContainer) {
		this.aggContainer = aggContainer;
		aggLayer.clear();
		aggLayer.addObject(aggContainer);
	}

	public void clear(int level) {
		if (level <= 3 && levelReached >= 3) {
			if (roadNetwork != null) {
				roadNetwork.clear();
				roadLayer.clear();
				intersectionLayer.clear();
			}
		}
		if (level <= 2 && levelReached >= 2) {
			if (aggContainer != null) {
				aggContainer.clear();
			}
			// debug stuff
			ui.getLayerManager().getLayer("matching").clear();
			ui.getLayerManager().getLayer("merging").clear();
		}
		if (level <= 1 && levelReached >= 1) {
			cleanLayer.clear();
		}
		if (level <= 0 && levelReached >= 0) {
			rawLayer.clear();
			ui.dataBoundingBox = null;
		}
	}
}
