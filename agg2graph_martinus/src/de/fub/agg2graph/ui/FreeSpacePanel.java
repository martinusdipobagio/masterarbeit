package de.fub.agg2graph.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.fub.agg2graph.structs.frechet.FrechetDistance;
import de.fub.agg2graph.structs.frechet.FrechetDistance.Cell;


public class FreeSpacePanel extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 881447785476967488L;
	
	JFrame window = null;
//	private JLabel eSliderLabel;
	private JSlider eSlider;
	private JScrollPane scroll = new JScrollPane();

	
	private int startx;

	private int starty;
	
	private FrechetDistance f;
	
	public FreeSpacePanel(JFrame window, FrechetDistance f) {
		this.window = window;
		this.f = f;
//		eSliderLabel = new JLabel("epsilon");
//		eSliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		eSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 1000);
		eSlider.addChangeListener(this);
//		add(eSliderLabel);
		add(eSlider);
		scroll.setViewportView(this);
		this.setDoubleBuffered(true);

	}


	@Override
	protected void paintComponent(Graphics g) {

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D)g;
//		int tileSize = state.tileSize;
		int tileSize = 100;
		Rectangle r = g.getClipBounds();
		startx = r.x/tileSize;
		starty = r.y/tileSize;
		int endx = (r.width/tileSize) + startx + 1;
		int endy = (r.height/tileSize) + starty + 1;
		endx = (endx < f.getSizeQ() - 1) ? endx : f.getSizeQ() - 1;
		endy = (endy < f.getSizeP() - 1) ? endy : f.getSizeP() - 1;
		BufferedImage tile = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_RGB);
		Color color1 = new Color(100, 100, 100);
		Color color2 = new Color(200, 200, 200);
//		Color markerColor = new Color(50, 50, 50);
		
		for(int y =  starty ; y <= endy; ++y) {
			for(int x = startx; x <= endx; ++x) {
				Cell cell = f.getCell((f.getSizeP() - y - 1 ), x);
				if(cell == null)
					continue;
				Color color = ((x%2 + y%2)%2 == 0) ? color1 : color2; // that is for the caro pattern.
//				if(state.markedCelli == y && state.markedCellj == x) {
//					color = markerColor;
//				}
				g2.setColor(color);
				g2.fillRect(x*tileSize, y*tileSize, tileSize, tileSize);
				
				Composite savedComposite = g2.getComposite();
				g2.setComposite(AlphaComposite.SrcAtop.derive(0.6f));
					g2.drawImage(cell.getFreeSpace(tile, tileSize), x*tileSize, y*tileSize, this);
				g2.setComposite(AlphaComposite.SrcAtop.derive(0.4f));
				g2.drawImage(cell.getParameterMarks(tile, tileSize), x*tileSize, y*tileSize, this);
				
				tile = cell.getReachableMarks(tile, tileSize);
				g2.drawImage(tile, x*tileSize,  y*tileSize, this);
				g2.setComposite(savedComposite);
			}
		}
	}

	/** Listener **/
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
		double epsilon = 0.003 * (source.getValue() / 10000.0);
		f.setEpsilon(epsilon);
		repaint();
	}
}