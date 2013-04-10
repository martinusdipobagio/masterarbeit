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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

public class RenderingOptions {
	public enum RenderingType {
		ALL, INTELLIGENT_ALL, POINTS, LINES, TEXT, NONE
	};

	public enum LabelRenderingType {
		ALWAYS, INTELLIGENT, NEVER
	};

	public int zIndex = 0;
	public Color color = Color.BLACK;
	public float strokeBaseWidthFactor = 1;
	public static final BasicStroke basicStroke = new BasicStroke(3.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	public LabelRenderingType labelRenderingType = LabelRenderingType.NEVER;
	public double opacity = 1;
	public RenderingType renderingType = RenderingType.INTELLIGENT_ALL;
	private Map<Float, Stroke> strokes = new HashMap<Float, Stroke>();

	@Override
	public String toString() {
		return "RenderingOptions [color=" + color + ", stroke=" + basicStroke
				+ ", opacity=" + opacity + "]";
	}

	public Stroke getStroke(float weightFactor) {
		// a little cache for the strokes
		if (strokes.get(weightFactor) != null) {
			return strokes.get(weightFactor);
		}
		BasicStroke newStroke = new BasicStroke(basicStroke.getLineWidth()
				* strokeBaseWidthFactor * weightFactor,
				basicStroke.getEndCap(), basicStroke.getLineJoin(),
				basicStroke.getMiterLimit(), basicStroke.getDashArray(),
				basicStroke.getDashPhase());
		// CompoundStroke finalStroke = new CompoundStroke(newStroke,
		// borderStroke, CompoundStroke.ADD);
		strokes.put(weightFactor, newStroke);
		return newStroke;
	}

	public RenderingOptions getCopy() {
		RenderingOptions result = new RenderingOptions();
		result.zIndex = zIndex;
		result.color = color;
		result.strokeBaseWidthFactor = strokeBaseWidthFactor;
		result.labelRenderingType = labelRenderingType;
		result.opacity = opacity;
		result.renderingType = renderingType;
		return result;
	}
}
