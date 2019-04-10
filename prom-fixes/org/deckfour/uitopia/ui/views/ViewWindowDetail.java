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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.components.ImageButton;
import org.deckfour.uitopia.ui.components.ImageToggleButton;
import org.deckfour.uitopia.ui.components.ViewHeaderBar;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.PrintUtils;
import org.deckfour.uitopia.ui.util.Tooltips;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ViewWindowDetail extends JPanel {

	private static final long serialVersionUID = 300142446308646603L;
	private static final Color COLOR_BUTTON_ACTIVE = new Color(40, 140, 40);
	private static final Color COLOR_BUTTON_PASSIVE = new Color(80, 80, 80);

	private final UITopiaController controller;
	private final ViewsView parent;
	private final View view;

	public ViewWindowDetail(ViewsView parent, View view) {
		this.controller = parent.getController();
		this.parent = parent;
		this.view = view;
		setupUI();
	}

	private void setupUI() {
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.setBorder(BorderFactory.createEmptyBorder());
		ViewHeaderBar header = new ViewHeaderBar(this.view.getCustomName());
		ImageToggleButton favoriteButton = new ImageToggleButton(ImageLoader
				.load("favorite_white_30x30.png"), new Color(140, 140, 20),
				COLOR_BUTTON_PASSIVE, COLOR_BUTTON_ACTIVE, 1);
		favoriteButton.setSelected(this.view.getResource().isFavorite());
		favoriteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.getResource()
						.setFavorite(!view.getResource().isFavorite());
				controller.getMainView().getWorkspaceView().updateResources();
			}
		});
		favoriteButton.setToolTipText(Tooltips.VIEWFAVORITEBUTTON);
		ImageButton refreshButton = new ImageButton(ImageLoader
				.load("refresh_white_30x30.png"), COLOR_BUTTON_PASSIVE,
				COLOR_BUTTON_ACTIVE, 1);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				view.refresh();
				revalidate();
			}
		});
		refreshButton.setToolTipText(Tooltips.VIEWREFRESHBUTTON);
		ImageButton printButton = new ImageButton(ImageLoader
				.load("print_white_30x30.png"), COLOR_BUTTON_PASSIVE,
				COLOR_BUTTON_ACTIVE, 1);
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PrintUtils.printView(view);
			}
		});
		printButton.setToolTipText(Tooltips.VIEWPRINTBUTTON);
		ImageButton actionButton = new ImageButton(ImageLoader
				.load("action_white_30x30.png"), COLOR_BUTTON_PASSIVE,
				COLOR_BUTTON_ACTIVE, 1);
		actionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controller.getMainView().showActionsView(view.getResource());
			}
		});
		actionButton.setToolTipText(Tooltips.VIEWACTIONBUTTON);
		header.addComponent(refreshButton);
		header.addComponent(printButton);
		header.addComponent(favoriteButton);
		header.addComponent(actionButton);
		this.add(header, BorderLayout.NORTH);
		this.add(view.getViewComponent(), BorderLayout.CENTER);
	}

	public void createView(Object obj) {
		if (obj instanceof ViewType) {
			ViewType type = (ViewType) obj;

			final View view = type.createView(this.view.getResource());
			Timer viewFinishedTimer = new Timer(100, new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (!view.isReady()) {
						return;
					}
					Timer timer = ((Timer) e.getSource());
					timer.stop();

					controller.getFrameworkHub().getViewManager().addView(view);
					parent.updateViews();
					parent.showFullScreen(view);
				}
			});
			viewFinishedTimer.start();

		}
	}

}
