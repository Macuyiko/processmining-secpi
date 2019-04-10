/*
 * Copyright (c) 2009 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.deckfour.uitopia.ui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.ui.main.MainView.View;
import org.deckfour.uitopia.ui.util.ArrangementHelper;
import org.deckfour.uitopia.ui.util.ImageLoader;


/**
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public class MainToolbar extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static final int HEIGHT = 45;
	private static final Color COLOR_TOP = new Color(200, 200, 200);
	private static final Color COLOR_BOTTOM = new Color(160, 160, 160);
	private Image appIcon;
	//private Image attributionIcon;
	
	private MainView mainView;
	private MainTabBar tabBar;
	//private SlickerSearchField searchField;
	
	public MainToolbar(MainView mainView) {
		this.mainView = mainView;
		this.setOpaque(true);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setMinimumSize(new Dimension(HEIGHT, HEIGHT));
		this.setMaximumSize(new Dimension(8000, HEIGHT));
		this.setPreferredSize(new Dimension(4000, HEIGHT));
		this.setup();
	}
	
	public void setEnabled(boolean enabled) {
		this.tabBar.setEnabled(enabled);
		//this.searchField.setEnabled(enabled);
	}
	
	private void setup() {
		this.tabBar = new MainTabBar(this.mainView);
		this.appIcon = ImageLoader.load("prom_logo_130x40.png");
		//this.attributionIcon = ImageLoader.load("fluxicon_logo_130x40.png");
		this.setLayout(new BorderLayout());
		JLabel logoLabel = new JLabel(new ImageIcon(this.appIcon));
		logoLabel.setOpaque(false);
		logoLabel.setBorder(BorderFactory.createEmptyBorder());
		//LinkLabel attributionLabel = new LinkLabel(this.attributionIcon, "http://www.fluxicon.com/");
		
		//attributionLabel.setOpaque(false);
		//attributionLabel.setBorder(BorderFactory.createEmptyBorder());
		/*
		JPanel searchPanel = new JPanel();
		searchPanel.setOpaque(false);
		searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		searchPanel.setLayout(new BorderLayout());
		this.searchField = new SlickerSearchField(140, 23, new Color(240, 240, 240), new Color(80, 80, 80), new Color(40, 40, 40), new Color(20, 20, 20));
		searchPanel.add(ArrangementHelper.centerVertically(this.searchField), BorderLayout.CENTER);
		*/
		this.add(logoLabel, BorderLayout.WEST);
		this.add(ArrangementHelper.centerHorizontally(ArrangementHelper.pushDown(this.tabBar)), BorderLayout.CENTER);
		//this.add(attributionLabel, BorderLayout.EAST);
	}
	
	public void activateTab(View view) {
		if(view.equals(View.WORKSPACE)) {
			tabBar.setActiveTab(0, false);
		} else if(view.equals(View.ACTIONS)) {
			tabBar.setActiveTab(1, false);
		} else if(view.equals(View.VIEWS)) {
			tabBar.setActiveTab(2, false);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// draw background gradient
		GradientPaint gradient = new GradientPaint(20, height / 3, COLOR_TOP, 20, height, COLOR_BOTTOM, false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, width, height);
		gradient = new GradientPaint(20, height - 10, new Color(0, 0, 0, 0), 20, height, new Color(0, 0, 0, 40), false);
		g2d.setPaint(gradient);
		g2d.fillRect(0, height - 10, width, 10);
	}
	
	

}
