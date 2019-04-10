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
package org.deckfour.uitopia.ui.workspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.components.ImageButton;
import org.deckfour.uitopia.ui.components.ImageLozengeButton;
import org.deckfour.uitopia.ui.components.ImageToggleButton;
import org.deckfour.uitopia.ui.util.ArrangementHelper;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.TimeUtils;
import org.deckfour.uitopia.ui.util.Tooltips;

import com.fluxicon.slickerbox.components.RoundedPanel;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ResourceView extends RoundedPanel {

	private static final long serialVersionUID = 7429267198567749852L;

	private final Resource resource;
	private final UITopiaController controller;

	private AbstractButton favoriteButton;
	private AbstractButton viewButton;
	private AbstractButton actionButton;
	private AbstractButton removeButton;
	private AbstractButton renameButton;
	private AbstractButton parentButton;
	private AbstractButton childrenButton;
	private AbstractButton exportButton;

	public ResourceView(Resource resource, UITopiaController controller) {
		super(20, 5, 15);
		this.resource = resource;
		this.controller = controller;
		setupUI();
	}

	private void setupUI() {
		this.setBackground(new Color(160, 160, 160));
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// assemble info panel
		JPanel infoPanel = new JPanel();
		infoPanel.setMaximumSize(new Dimension(500, 180));
		infoPanel.setOpaque(false);
		infoPanel.setLayout(new BorderLayout());
		Image icon = resource.getPreview(150, 150);
		JLabel preview = new JLabel(new ImageIcon(icon));
		preview.setSize(150, 150);
		preview.setOpaque(false);
		JPanel detailsPanel = new JPanel();
		detailsPanel.setOpaque(false);
		detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 15, 0));
		detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
		detailsPanel.add(styleLabel(resource.getName(), new Color(10, 10, 10),
				18));
		detailsPanel.add(Box.createVerticalStrut(3));
		detailsPanel.add(styleLabel(resource.getType().getTypeName(),
				new Color(30, 30, 30), 14));
		detailsPanel.add(Box.createVerticalStrut(12));
		long age = System.currentTimeMillis()
				- resource.getCreationTime().getTime();
		detailsPanel.add(styleLabel(TimeUtils.ageToString(age), new Color(60,
				60, 60), 12));
		detailsPanel.add(Box.createVerticalStrut(5));
		String text = "<html><i>";
		if (resource.getSourceAction() == null) {
			text += "imported";
		} else {
			text += "by " + resource.getSourceAction().getName();
		}
		text += "</i></html>";
		detailsPanel.add(styleLabel(text, new Color(60, 60, 60), 12));
		detailsPanel.add(Box.createVerticalGlue());
		infoPanel.add(preview, BorderLayout.WEST);
		infoPanel.add(detailsPanel, BorderLayout.CENTER);
		// assemble actions panel
		RoundedPanel actionsPanel = new RoundedPanel(50, 0, 0);
		actionsPanel.setBackground(new Color(80, 80, 80));
		actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.X_AXIS));
		actionsPanel.setMinimumSize(new Dimension(250, 50));
		actionsPanel.setMaximumSize(new Dimension(250, 50));
		actionsPanel.setPreferredSize(new Dimension(250, 50));
		actionsPanel.setBorder(BorderFactory.createEmptyBorder());

		boolean isActionable = controller.getFrameworkHub().getTaskManager()
				.isActionableResource(resource);

		favoriteButton = new ImageToggleButton(
				ImageLoader.load("favorite_30x30_black.png"));
		favoriteButton.setSelected(resource.isFavorite());
		favoriteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleFavorite();
			}
		});
		if (isActionable) {
			favoriteButton.setToolTipText(Tooltips.WORKSPACEFAVORITEBUTTON);
		} else {
			favoriteButton
					.setToolTipText(Tooltips.WORKSPACEFAVORITEDISABLEDBUTTON);
			favoriteButton.setEnabled(false);
		}
		viewButton = new ImageButton(ImageLoader.load("view_30x30_black.png"));
		viewButton.setEnabled(!controller.getFrameworkHub().getViewManager()
				.getViewTypes(resource).isEmpty());
		viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view();
			}
		});
		if (isActionable) {
			viewButton.setToolTipText(Tooltips.WORKSPACEVIEWBUTTON);
		} else {
			viewButton.setToolTipText(Tooltips.WORKSPACEVIEWDISABLEDBUTTON);
			viewButton.setEnabled(false);
		}
		actionButton = new ImageButton(
				ImageLoader.load("action_30x30_black.png"));
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action();
			}
		});
		if (isActionable) {
			actionButton.setToolTipText(Tooltips.WORKSPACEACTIONBUTTON);
		} else {
			actionButton.setToolTipText(Tooltips.WORKSPACEACTIONDISABLEDBUTTON);
			actionButton.setEnabled(false);
		}
		removeButton = new ImageButton(
				ImageLoader.load("remove_30x30_black.png"), new Color(140, 140,
						140), new Color(180, 20, 20), 2);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		removeButton.setToolTipText(Tooltips.WORKSPACEREMOVEBUTTON);
		renameButton = new ImageButton(
				ImageLoader.load("action_30x30_black.png"));
		renameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rename();
			}
		});
		if (isActionable) {
			renameButton.setToolTipText("Rename Resource");
		} else {
			renameButton.setToolTipText("Disabled");
			renameButton.setEnabled(false);
		}
		actionsPanel.add(Box.createHorizontalGlue());
		actionsPanel.add(favoriteButton);
		actionsPanel.add(Box.createHorizontalStrut(5));
		actionsPanel.add(viewButton);
		actionsPanel.add(Box.createHorizontalStrut(5));
		actionsPanel.add(actionButton);
		actionsPanel.add(Box.createHorizontalStrut(5));
		actionsPanel.add(removeButton);
		actionsPanel.add(Box.createHorizontalStrut(5));
		actionsPanel.add(renameButton);
		actionsPanel.add(Box.createHorizontalGlue());
		// assemble family panel
		RoundedPanel familyPanel = new RoundedPanel(50, 0, 0) {
			private static final long serialVersionUID = 6739005088069438989L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// add fancy arrowhead
				int yMid = getHeight() / 2;
				int x[] = { 15, 45, 42, 45 };
				int y[] = { yMid, yMid - 15, yMid, yMid + 15 };
				g.setColor(new Color(120, 120, 120));
				g.fillPolygon(x, y, 4);
			}
		};
		familyPanel.setBackground(new Color(80, 80, 80));
		familyPanel.setLayout(new BoxLayout(familyPanel, BoxLayout.Y_AXIS));
		familyPanel.setMinimumSize(new Dimension(220, 100));
		familyPanel.setMaximumSize(new Dimension(220, 100));
		familyPanel.setPreferredSize(new Dimension(220, 100));
		familyPanel.setBorder(BorderFactory.createEmptyBorder(5, 55, 5, 15));
		parentButton = new ImageLozengeButton(
				ImageLoader.load("parent_30x30_black.png"), "Show parents");
		parentButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showParents();
			}
		});
		parentButton.setToolTipText(Tooltips.WORKSPACEPARENTSBUTTON);
		childrenButton = new ImageLozengeButton(
				ImageLoader.load("children_30x30_black.png"), "Show children");
		childrenButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showChildren();
			}
		});
		childrenButton.setToolTipText(Tooltips.WORKSPACECHILDRENBUTTON);
		familyPanel.add(Box.createVerticalGlue());
		familyPanel.add(parentButton);
		familyPanel.add(Box.createVerticalStrut(5));
		familyPanel.add(childrenButton);
		familyPanel.add(Box.createVerticalGlue());
		exportButton = new ImageLozengeButton(
				ImageLoader.load("export_30x30_black.png"), "Export to disk",
				new Color(120, 120, 120), new Color(0, 120, 0), 2);
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		exportButton.setToolTipText(Tooltips.WORKSPACEEXPORTBUTTON);
		this.add(infoPanel);
		this.add(Box.createVerticalStrut(25));
		this.add(ArrangementHelper.pushLeft(actionsPanel));
		this.add(Box.createVerticalStrut(25));
		this.add(ArrangementHelper.pushLeft(familyPanel));
		this.add(Box.createVerticalStrut(25));
		this.add(ArrangementHelper.pushLeft(exportButton));
		this.add(Box.createVerticalGlue());
	}

	private void toggleFavorite() {
		resource.setFavorite(favoriteButton.isSelected());
		this.controller.getMainView().getWorkspaceView().updateResources();
	}

	private void view() {
		this.controller.getMainView().showViewsView(resource);
	}

	private void action() {
		this.controller.getMainView().showActionsView(resource);
	}
	
	private void rename() {
		String newName = JOptionPane.showInputDialog(null, "Enter new name:", "", 1);
		if (!newName.equals(""))
			resource.setName(newName);
	}

	private void remove() {
		List<View> views = new ArrayList<View>(this.controller
				.getFrameworkHub().getViewManager().getViews());
		for (View view : views) {
			if (view.getResource().equals(resource)) {
				this.controller.getFrameworkHub().getViewManager()
						.removeView(view);
			}
		}
		resource.destroy();
	}

	private void showParents() {
		this.controller.getMainView().getWorkspaceView()
				.showParentsOf(resource);
	}

	private void showChildren() {
		this.controller.getMainView().getWorkspaceView()
				.showChildrenOf(resource);
	}

	private void export() {
		try {
			this.controller.getFrameworkHub().getResourceManager()
					.exportResource(resource);
		} catch (IOException e) {
			// oops...
			e.printStackTrace();
		}
	}

	private JLabel styleLabel(String text, Color color, float size) {
		JLabel label = new JLabel(text);
		label.setOpaque(false);
		label.setForeground(color);
		label.setFont(label.getFont().deriveFont(size));
		return label;
	}

}
