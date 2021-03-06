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

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class LayerManager {
	private final List<Layer> layers = new ArrayList<Layer>();
	private final List<IRenderingPanel> panels = new ArrayList<IRenderingPanel>();
	private MainRenderingPanel mainPanel;
	private TestUI ui;
	/*
	 * visible rect in projection
	 */
	private Rectangle2D.Double projectionArea;
	/*
	 * visible rect in gps coordinates
	 */
	private Rectangle2D.Double gpsArea;
	/*
	 * pixels x pixels
	 */
	private Dimension size;
	public float thicknessFactor = 0.4f;
	public boolean renderWeight = true; // false = renderAvgDist

	public LayerManager(TestUI testUI) {
		this.ui = testUI;
	}

	public TestUI getUi() {
		return ui;
	}

	public void setUi(TestUI ui) {
		this.ui = ui;
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public Rectangle2D.Double getProjectionArea() {
		return projectionArea;
	}

	public void setArea(Rectangle2D.Double gpsArea,
			Rectangle2D.Double projectionArea) {
		if (gpsArea == null) {
			this.gpsArea = null;
		} else {
			this.gpsArea = (Rectangle2D.Double) gpsArea.clone();
		}
		if (projectionArea == null) {
			this.projectionArea = null;
		} else {
			this.projectionArea = (Rectangle2D.Double) projectionArea.clone();
		}
	}

	public Rectangle2D.Double getGpsArea() {
		return gpsArea;
	}

	public MainRenderingPanel getMainPanel() {
		return mainPanel;
	}

	public void setMainPanel(MainRenderingPanel mainPanel) {
		this.mainPanel = mainPanel;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public Layer getLayer(String name) {
		for (Layer layer : getLayers()) {
			if (layer.getName().equals(name)) {
				return layer;
			}
		}
		return null;
	}

	public void addLayerToPanel(Layer layer, IRenderingPanel panel) {
		if (panel == null || layer == null) {
			return;
		}
		layer.setLayerManager(this);
		layer.addPanel(panel);
		panel.addLayer(layer);

		if (!layers.contains(layer)) {
			layers.add(layer);
		}
		if (!panels.contains(panel)) {
			panels.add(panel);
			// add to UI
			ui.addSidePanel((JPanel) panel);
		}
	}

	public void mirrorToSmallPanels() {
		// System.out.println("mirror");
		for (IRenderingPanel panel : panels) {
			if (!(panel instanceof MainRenderingPanel)) {
				((JPanel) panel).repaint();
			}
		}
	}

	public void repaintAllLayers() {
		ui.setPainting(true);
		for (Layer layer : layers) {
			repaintLayer(layer);
		}
		ui.setPainting(false);
	}

	public void repaintLayer(Layer layer) {
		if (!layers.contains(layer)) {
			return;
		}

		// paint
		layer.repaint();
		for (IRenderingPanel panel : layer.getPanels()) {
			((JPanel) panel).repaint();
		}
	}

	public void saveOutput(File targetFolder) {
		repaintAllLayers();
		targetFolder.mkdirs();

		Collections.sort(layers, new Comparator<Layer>() {

			@Override
			public int compare(Layer o1, Layer o2) {
				if (o1 == null || o1.getOptions() == null) {
					return 1;
				}
				if (o2 == null || o2.getOptions() == null) {
					return -1;
				}
				return o1.getOptions().zIndex - o2.getOptions().zIndex;
			}
		});

		int counter = 0;
		for (Layer layer : layers) {
			BufferedImage image = null;
			try {
				File outputFile = new File(String.format("%s%sL%02d_%s.png",
						targetFolder.getAbsoluteFile(), File.separator,
						++counter, layer.getName()));
				while ((image = layer.getImage()) == null) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				System.out.println("Writing to file: " + outputFile);
				ImageIO.write(image, "png", outputFile);
			} catch (IOException e) {
				System.out.println("unable to write to output files");
			}
		}
	}
}
