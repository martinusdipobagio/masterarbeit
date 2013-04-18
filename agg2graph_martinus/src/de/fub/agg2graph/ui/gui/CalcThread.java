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

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.fub.agg2graph.graph.RamerDouglasPeuckerFilter;
import de.fub.agg2graph.input.GPSCleaner;
import de.fub.agg2graph.input.GPSFilter;
import de.fub.agg2graph.input.GPXReader;
import de.fub.agg2graph.management.MiniProfiler;
import de.fub.agg2graph.structs.GPSEdge;
import de.fub.agg2graph.structs.GPSSegment;
import de.fub.agg2graph.structs.frechet.FrechetDistance;
import de.fub.agg2graph.ui.FreeSpacePanel;

public class CalcThread extends Thread {
	private String task;
	private final TestUI parent;

	public static final Map<String, Integer> levels;
	static {
		levels = new HashMap<String, Integer>();
		levels.put("input", 0);
		levels.put("filter", 1);
		levels.put("clean", 2);
		levels.put("agg", 3);
		levels.put("road", 4);
		levels.put("osm", 5);
		levels.put("free",6);
	}

	public static String[] stepNames = new String[] { "Input", "Filter", "Clean",
			"Aggregation", "Road Gen", "OSM Export", "Free Space"};

	public CalcThread(TestUI parent) {
		this.parent = parent;
	}

	public void setTask(String task) {
		this.task = task;
	}

	@Override
	public void run() {

		parent.setLoading(true);
		System.out.println(MiniProfiler.print(String.format("before step %s",
				task)));
		UIStepStorage stepStorage = parent.uiStepStorage;
		if (task.equals("input")) {
			stepStorage.clear(levels.get(task));
			stepStorage.inputSegmentList = new ArrayList<GPSSegment>();

			File folder = (File) parent.sourceFolderCombo.getSelectedItem();
			File[] files = folder.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".gpx");
				}
			});
			Arrays.sort(files);

			for (File file : files) {
				if (parent.deselectedTraceFiles.contains(file)) {
					continue;
				}
				System.out.println("processing file " + file);
				List<GPSSegment> segments = GPXReader.getSegments(file);
				if (segments == null) {
					System.out.println("Bad file: " + file);
					continue;
				}
				for (GPSSegment segment : segments) {
					parent.parseDim(segment);
					segment.addIDs("I" + stepStorage.inputSegmentList.size());
					stepStorage.rawLayer.addObject(segment);
					stepStorage.inputSegmentList.add(segment);
				}
			}

			System.out.println(String.format("Loaded %d gps segments",
					stepStorage.inputSegmentList.size()));

			parent.getMainPanel().showArea(parent.dataBoundingBox);
			parent.getLayerManager().setSize(new Dimension(1, 1));
			repaintEverything();
		} else if (task.equals("filter")) {
			stepStorage.clear(levels.get(task));

//			// Filter data
			GPSFilter gpsFilter = stepStorage.getGpsFilter();
			GPSSegment firstSegment = stepStorage.inputSegmentList.get(0);
			GPSSegment filteredSegment = gpsFilter.filter(firstSegment);
			stepStorage.inputSegmentList.remove(0);
			stepStorage.inputSegmentList.add(0, filteredSegment);

			repaintEverything();
		} else if (task.equals("clean")) {
			stepStorage.clear(levels.get(task));
			stepStorage.cleanSegmentList = new ArrayList<GPSSegment>();

			// clean data
			GPSCleaner gpsCleaner = stepStorage.getGpsCleaner();
			RamerDouglasPeuckerFilter rdpf = stepStorage
					.getCleaningRamerDouglasPeuckerFilter();
			for (GPSSegment segment : stepStorage.inputSegmentList) {
				for (GPSSegment cleanSegment : gpsCleaner.clean(segment)) {
					// run through Douglas-Peucker here (slightly modified
					// perhaps to avoid too long edges)
					cleanSegment = rdpf.simplify(cleanSegment);
					stepStorage.cleanLayer.addObject(cleanSegment);
					stepStorage.cleanSegmentList.add(cleanSegment);

				}
			}					
			repaintEverything();
			

		} else if (task.equals("agg")) {
			stepStorage.clear(levels.get(task));
			int counter = 0;
			// use clean data if cleaning step has been performed
			if (stepStorage.levelReached >= 2) {
				for (GPSSegment cleanSegment : stepStorage.cleanSegmentList) {
					System.out.println(String.format("adding segment no. %d",
							++counter));
					stepStorage.getAggContainer().addSegment(cleanSegment);
				}
			} else {
				for (GPSSegment inputSegment : stepStorage.inputSegmentList) {
					System.out.println(String.format("adding segment no. %d",
							++counter));
					stepStorage.getAggContainer().addSegment(inputSegment);
				}
			}
			//Save the result
			
			repaintEverything();
		} else if (task.equals("road")) {
			stepStorage.clear(levels.get(task));
			stepStorage.getRoadNetwork().parse(stepStorage.getAggContainer(),
					stepStorage);
			repaintEverything();
		} else if (task.equals("osm")) {
			stepStorage.clear(levels.get(task));
			if (stepStorage.getExporter().getTargetFile() == null) {
				stepStorage.getExporter()
						.setTargetFile(new File("osm-out.xml"));
			}
			stepStorage.getExporter().export(stepStorage.getRoadNetwork());
			if (stepStorage.isOpenOsmExportFile()
					&& stepStorage.getExporter().getTargetFile().exists()) {
				System.out.println("opening file "
						+ stepStorage.getExporter().getTargetFile());
				try {
					Desktop.getDesktop().open(
							stepStorage.getExporter().getTargetFile());
				} catch (IOException e) {
					System.out.println(e.getLocalizedMessage());
					e.printStackTrace();
				}
			}
		} else if (task.equals("free")) {
			//TODO
			System.out.println(String.format("Loaded %d gps segments",
					stepStorage.cleanSegmentList.size()));
			//Get first Segment
			GPSSegment segment1 = stepStorage.cleanSegmentList.get(0);
			List<GPSEdge> path1 = new ArrayList<GPSEdge>();
			for(int i = 0; i < segment1.size() - 1; i++) {
				path1.add(new GPSEdge(segment1.get(i), segment1.get(i+1)));
			}
//			Vector<GPSEdge> edge1 = segment1.convertToEdges();
			//Get second Segment
			GPSSegment segment2 = stepStorage.cleanSegmentList.get(1);
			List<GPSEdge> path2 = new ArrayList<GPSEdge>();
			for(int i = 0; i < segment2.size() - 1; i++) {
				path2.add(new GPSEdge(segment2.get(i), segment2.get(i+1)));
			}
//			Vector<GPSEdge> edge2 = segment2.convertToEdges();
			//TODO Default 0.0003
			FrechetDistance f = new FrechetDistance(path1, path2, 0.0003);
			showFreeSpace(f);			
		} 
		stepStorage.levelReached = levels.get(task);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				parent.tabbedPane.setSelectedIndex((levels.get(task) + 1)
						% levels.size());
			}
		});
		System.out.println(MiniProfiler.print(String.format("after step %s",
				task)));
		parent.setLoading(false);
	}

	private void repaintEverything() {
		parent.setPainting(true);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				parent.getLayerManager().repaintAllLayers();
			}
		});
	}
	
	private void showFreeSpace(final FrechetDistance f) {
		javax.swing.SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				show();
				
			}

			private void show() {
				
				JFrame freeSpaceView = new JFrame();
				freeSpaceView.setTitle("Free Space Diagram");
				freeSpaceView.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				
				FreeSpacePanel freeSpace = new FreeSpacePanel(freeSpaceView, f);
				freeSpace.setPreferredSize(new Dimension (600, 800));

				freeSpaceView.getContentPane().add(freeSpace);
				freeSpaceView.pack();
				freeSpaceView.setVisible(true);
			}
			
		});
	}
}
