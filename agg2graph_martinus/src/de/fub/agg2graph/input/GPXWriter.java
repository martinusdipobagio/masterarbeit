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
package de.fub.agg2graph.input;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.fub.agg2graph.structs.GPSPoint;
import de.fub.agg2graph.structs.GPSSegment;

/**
 * Save {@link GPSSegment} data to a gpx file.
 * 
 * @author Johannes Mitlmeier
 * 
 */
public class GPXWriter {
	private static final String fileTemplateOneSegment = "<?xml version=\"1.0\"?>\n<gpx version=\"1.1\" creator=\"GPXWriter\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n<trk><trkseg>\n%s</trkseg></trk></gpx>";
	private static final String fileTemplateMoreSegments = "<?xml version=\"1.0\"?>\n<gpx version=\"1.1\" creator=\"GPXWriter\" xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">\n<trk>\n%s</trk></gpx>";
	// private static final String pointTemplate =
	// "<trkpt lat=\"%s\" lon=\"%s\" />";
	private static final String segmentTemplate = "<trkseg>\n%s</trkseg>";
	private static final String pointTemplate = "<trkpt lat=\"%s\" lon=\"%s\" k=\"%s\" />";

	public static void writeSegment(File targetFile, GPSSegment segment)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		
		for (GPSPoint point : segment) {
			if(point.getK() < 1) {
				sb.append(
						String.format(pointTemplate, point.getLat(), point.getLon(), 1))
						.append("\n");

			} else {
				sb.append(
						String.format(pointTemplate, point.getLat(), point.getLon(), 1))
						.append("\n");
			}
			
		} 
		FileWriter fstream = new FileWriter(targetFile);
		fstream.write(String.format(fileTemplateOneSegment, sb.toString()));
		fstream.close();
	}
	
	public static void writeSegments(File targetFile, List<GPSSegment> segments)
			throws IOException {
		StringBuilder segmentBuilder = new StringBuilder();
		
		for(GPSSegment segment : segments) {
			StringBuilder pointBuilder = new StringBuilder();
			for (GPSPoint point : segment) {
				if(point.getK() < 1) {
					pointBuilder.append(
							String.format(pointTemplate, point.getLat(), point.getLon(), 1))
							.append("\n");
				} else {
					pointBuilder.append(
							String.format(pointTemplate, point.getLat(), point.getLon(), point.getK()))
							.append("\n");
				}				
			}
			segmentBuilder.append(String.format(segmentTemplate, pointBuilder.toString())).
					append("\n");
		}
		
		FileWriter fstream = new FileWriter(targetFile, false);
		fstream.write(String.format(fileTemplateMoreSegments, segmentBuilder.toString()));
		fstream.close();
	}
}
