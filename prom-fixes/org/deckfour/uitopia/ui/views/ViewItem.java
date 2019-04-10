/*
 * This file is part of the project UItopia
 *
 * Copyright (c) 2009 by 
 *     Fluxicon Process Laboratories
 *     Horsten 1
 *     5612 AX Eindhoven
 *     The Netherlands
 *
 * created by Christian W. Guenther (christian@fluxicon.com)
 *
 * WARNING:
 * This contents of this file are protected by international copyright and
 * intellectual property legislation. You are NOT allowed to use it, neither in
 * full nor in parts / excerpts of any extent, for neither commercial nor
 * non-commercial purposes. Any use requires an EXPLICIT WRITTEN PERMISSION by
 * Fluxicon. For licensing or further requests, please contact us via email at
 * info@fluxicon.com.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.deckfour.uitopia.ui.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.ui.components.ImageButton;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.Tooltips;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 *
 */
public class ViewItem extends JComponent {
	
	private static final long serialVersionUID = -3380025203180010319L;
	private static final Color COLOR_TOP = new Color(255, 255, 255);
	private static final Color COLOR_BOTTOM = new Color(180, 180, 180);
	private static final Color COLOR_SHADOW = new Color(40, 40, 40, 120);
	private static final Color COLOR_TEXT = new Color(20, 20, 20);
	private static final Color COLOR_BUTTON_PASSIVE = new Color(0, 0, 0, 0);
	private static final Color COLOR_BUTTON_ACTIVE = new Color(60, 180, 60);
	
	private static final int WIDTH = 160;
	private static final int HEIGHT = 160;
	private static final int BORDER_OUT = 10;
	private static final int BORDER_IN = 8;
	private static final int SHADOW_X = 4;
	private static final int SHADOW_Y = 5;
	
	private static final Image ICON_VIEW = ImageLoader.load("view_black_20x20.png");
	private static final Image ICON_REMOVE = ImageLoader.load("remove_black_20x20.png");
	
	private ViewsView parent;
	private View view;
	
	private ImageButton viewWindowButton;
	private ImageButton viewButton;
	private ImageButton removeButton;
	private JLabel thumbnail;
	private JLabel label;
	
	public ViewItem(ViewsView parent, View view) {
		this.parent = parent;
		this.view = view;
		setupUI();
	}
	
	public void updateSize(int width, int height) {
		Dimension dim = new Dimension(width + BORDER_IN + BORDER_OUT + SHADOW_X,
				height + BORDER_IN + BORDER_OUT + SHADOW_Y);
		this.thumbnail.setIcon(new ImageIcon(view.getPreview(width, width)));
		this.setMinimumSize(dim);
		this.setMaximumSize(dim);
		this.setPreferredSize(dim);
		this.setSize(dim);
		this.revalidate();
		this.repaint();
	}
	
	private void setupUI() {
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(
				BORDER_IN + BORDER_OUT,
				BORDER_IN + BORDER_OUT,
				BORDER_IN + BORDER_OUT + SHADOW_Y,
				BORDER_IN + BORDER_OUT + SHADOW_X));
		JPanel header = new JPanel();
		header.setOpaque(false);
		header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
		header.setMinimumSize(new Dimension(30, 30));
		header.setMaximumSize(new Dimension(1000, 30));
		header.setPreferredSize(new Dimension(200, 30));
		this.viewWindowButton = new ImageButton(ICON_VIEW, COLOR_BUTTON_PASSIVE, COLOR_BUTTON_ACTIVE, 2);
		this.viewWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showInWindow(view);
			}
		});
		this.viewWindowButton.setToolTipText("Open In New Window");
		this.viewButton = new ImageButton(ICON_VIEW, COLOR_BUTTON_PASSIVE, COLOR_BUTTON_ACTIVE, 2);
		this.viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.showFullScreen(view);
			}
		});
		this.viewButton.setToolTipText(Tooltips.VIEWVIEWBUTTON);
		this.removeButton = new ImageButton(ICON_REMOVE, COLOR_BUTTON_PASSIVE, COLOR_BUTTON_ACTIVE, 2);
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.removeView(view);
			}
		});
		this.removeButton.setToolTipText(Tooltips.VIEWREMOVEBUTTON);
		header.add(Box.createHorizontalGlue());
		header.add(this.viewWindowButton);
		header.add(this.viewButton);
		header.add(Box.createHorizontalStrut(5));
		header.add(this.removeButton);
		this.thumbnail = new JLabel(new ImageIcon(this.view.getPreview(WIDTH, WIDTH)));
		this.thumbnail.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.thumbnail.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if((e.getButton() == MouseEvent.BUTTON1)
						&& (e.getClickCount() > 1)) {
					parent.showFullScreen(view);
				}
			}
		});
		this.label = new JLabel(view.getCustomName());
		this.label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		this.label.setOpaque(false);
		this.label.setForeground(COLOR_TEXT);
		this.label.setFont(this.label.getFont().deriveFont(12f));
		// compose component
		this.add(header);
		this.add(thumbnail);
		this.add(Box.createVerticalStrut(6));
		this.add(label);
		this.add(Box.createVerticalGlue());
		updateSize(WIDTH, HEIGHT);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		int boxWidth = width - BORDER_OUT - BORDER_OUT - SHADOW_X;
		int boxHeight = height - BORDER_OUT - BORDER_OUT - SHADOW_Y;
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// paint shadow
		g2d.setColor(COLOR_SHADOW);
		g2d.fillRect(BORDER_OUT + SHADOW_X, BORDER_OUT + SHADOW_Y, boxWidth, boxHeight);
		// paint polaroid box
		GradientPaint gradient = new GradientPaint(
				0, BORDER_OUT, COLOR_TOP, 
				0, BORDER_OUT + boxHeight, COLOR_BOTTOM);
		g2d.setPaint(gradient);
		g2d.fillRect(BORDER_OUT, BORDER_OUT, boxWidth, boxHeight);
	}
	

}
