package org.processmining.plugins.clusterexplainer.ui.plotting;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class ColoredDotPlot extends JPanel {

	private static final long serialVersionUID = -9160776563156902287L;
	private double minX, maxX, minY, maxY;
	private List<DrawPoint> points;
	private int padding = 10;

	public ColoredDotPlot() {
		this.clearPoints();
		this.setPreferredSize(new Dimension(400, 400));
		
	}
	
	public void paint(Graphics g)  {
		super.paint(g);
		resetPlot(g, minX, maxX, minY, maxY);
		drawPoints(g);
	}
	
	public void resetPlot(Graphics graphics, double minX, double maxX, double minY, double maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		
		if (graphics == null)
			return;
			
		Graphics2D g = (Graphics2D) graphics;
		Dimension s = this.getSize();
		g.setBackground(Color.lightGray);
		g.clearRect(0, 0, s.width, s.height);
		
		// Draw axes
		Point pOrigin = getCoordToPosition(0, 0);
		Point pNorth = getCoordToPosition(0, maxY);
		Point pSouth = getCoordToPosition(0, minY);
		Point pEast = getCoordToPosition(maxX, 0);
		Point pWest = getCoordToPosition(minX, 0);
		
		g.setColor(Color.black);
		g.drawLine(pWest.x, pWest.y, pEast.x, pEast.y);
		drawArrowHead(g, pEast, pWest, Color.black);
		g.drawLine(pSouth.x, pSouth.y, pNorth.x, pNorth.y);
		drawArrowHead(g, pNorth, pSouth, Color.black);
		g.drawOval(pOrigin.x-2, pOrigin.y-2, 4, 4);
		
		// Draw labels
		for (double xl = Math.ceil(minX); xl <= Math.floor(maxX); xl+=1) {
			Point pLabel = getCoordToPosition(xl, 0);
			g.drawString(""+xl, pLabel.x, pLabel.y);
		}
		for (double yl = Math.ceil(minY); yl <= Math.floor(maxY); yl+=1) {
			Point pLabel = getCoordToPosition(0, yl);
			g.drawString(""+yl, pLabel.x, pLabel.y);
		}
				
	}

	public void clearPoints() {
		this.points = new ArrayList<DrawPoint>();
	}

	public void addPoint(double x, double y, Color color, String label) {
		DrawPoint dp = new DrawPoint();
		dp.x = x;
		dp.y = y;
		dp.c = color;
		dp.l = label;
		
		this.addPoint(dp);
	}
	
	public void addPoint(DrawPoint dp) {
		this.points.add(dp);
		this.drawPoint(this.getGraphics(), dp);
	}
	
	private void drawPoints(Graphics graphics) {
		for (DrawPoint dp : this.points)
			this.drawPoint(graphics, dp);
	}
	
	private void drawPoint(Graphics graphics, DrawPoint dp) {
		Graphics2D g = (Graphics2D) graphics;
		Point pPoint = getCoordToPosition(dp.x, dp.y);
		g.setColor(dp.c);
		g.fillRect(pPoint.x-3, pPoint.y-3, 6, 6);
		if (dp.l != null && !dp.l.equals(""))
			g.drawString(dp.l, pPoint.x+3, pPoint.y);
	}
	
	private void drawArrowHead(Graphics2D g, Point tip, Point tail, Color color) {
		int barb = 10;
		double phi = Math.toRadians(40);
		double dy = tip.y - tail.y;
		double dx = tip.x - tail.x;
		double theta = Math.atan2(dy, dx);
		double x, y, rho = theta + phi;
		for (int j = 0; j < 2; j++) {
			x = tip.x - barb * Math.cos(rho);
			y = tip.y - barb * Math.sin(rho);
			g.draw(new Line2D.Double(tip.x, tip.y, x, y));
			rho = theta - phi;
		}
	}

	private Point getCoordToPosition(double x, double y) {
		
		Point p = new Point();
		Dimension s = this.getSize();
		
		// Normalized position (between zero and one)
		double relativeX = (x - minX) / (maxX - minX);
		double relativeY = (y - minY) / (maxY - minY);
		
		p.x = (int) ((double)padding + ((double)s.width  - 2*(double)padding)*relativeX);
		p.y = (int) ((double)s.height - (double)padding - ((double)s.height - 2*(double)padding)*relativeY);
		
		return p;
	}
	
	private class DrawPoint {
		public double x, y;
		public Color c;
		public String l;
	}

}
