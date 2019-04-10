/*
 * Copyright (c) 2009 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */
package org.deckfour.uitopia.ui.views;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.deckfour.uitopia.api.event.UpdateListener;
import org.deckfour.uitopia.api.hub.ViewManager;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.View;
import org.deckfour.uitopia.api.model.ViewType;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.main.Viewable;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ViewsView extends JPanel implements Viewable {

	private static final long serialVersionUID = 5946573782649186241L;

	private UITopiaController controller;

	private ViewGrid grid;

	private View view = null;

	private ViewDetail detail;
	
	private Map<View, JFrame> openWindows;

	public ViewsView(UITopiaController controller) {
		this.controller = controller;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(true);
		this.grid = new ViewGrid(controller.getFrameworkHub().getViewManager(), this);
		this.add(grid, BorderLayout.CENTER);
		this.openWindows = new HashMap<View, JFrame>();
		// add update listener to view manager
		ViewManager vm = controller.getFrameworkHub().getViewManager();
		vm.addListener(new UpdateListener() {
			public void updated() {
				updateViews();
			}
		});
	}

	public void showInWindow(final View view) {
		this.view = view;
		if (!openWindows.containsKey(view) || openWindows.get(view) == null) {
			JFrame newFrame = new JFrame();
			newFrame.setSize(600, 480);
			newFrame.setTitle("VIEW: " + view.getResource().getName());
			newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			
			ViewWindowDetail frameDetail = new ViewWindowDetail(ViewsView.this, view);
			newFrame.add(frameDetail, BorderLayout.CENTER);
			frameDetail.addComponentListener(new ComponentListener() {
				public void componentHidden(ComponentEvent e) {
					view.captureNow();
				}

				public void componentMoved(ComponentEvent e) {
				}

				public void componentResized(ComponentEvent e) {
					view.captureNow();
				}

				public void componentShown(ComponentEvent e) {
					view.captureNow();
				}
			});
			
			openWindows.put(view, newFrame);
		}
		detail = null;
		showOverview();
		JFrame frameToShow = openWindows.get(view);
		frameToShow.setVisible(true);
		frameToShow.repaint();
	}

	public void showFullScreen(final View view) {
		this.view = view;
		if (openWindows.containsKey(view) && openWindows.get(view) != null) {
			openWindows.get(view).dispose();
		}
		openWindows.remove(view);
			
		detail = new ViewDetail(ViewsView.this, view);
		synchronized (view) {
			removeAll();
		}
		add(detail, BorderLayout.CENTER);
		revalidate();
		repaint();
		detail.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent e) {
				view.captureNow();
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				view.captureNow();
			}

			public void componentShown(ComponentEvent e) {
				view.captureNow();
			}
		});
	}

	public void showOverview() {
		this.removeAll();
		this.add(grid, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
		grid.updateItems();
		this.view = null;
	}

	public void removeView(View view) {
		controller.getFrameworkHub().getViewManager().removeView(view);
		updateViews();
	}
	
	public void removeViewFrame(View view) {
		this.openWindows.remove(view);
	}

	public void showResource(final Resource resource, boolean updateIfShown) {
		final ViewManager vm = controller.getFrameworkHub().getViewManager();
		for (View view : vm.getViews()) {
			if (view.getResource().equals(resource)) {
				if (updateIfShown) {
					view.refresh();
				}
				showFullScreen(view);
				return;
			}
		}
		final List<ViewType> vt = vm.getViewTypes(resource);
		if (vt.isEmpty()) {
			return;
		}
		// add new view
		final View view = vt.get(0).createView(resource);

		SwingWorker<?, ?> worker = new SwingWorker<Object, Object>() {

			@Override
			protected Object doInBackground() throws Exception {
				synchronized (view) {
					while (!view.isReady()) {
						try {
							view.wait();
						} catch (InterruptedException e) {
							// try again
						}
					}
				}
				return null;
			}

			@Override
			protected void done() {
				vm.addView(view);
				updateViews();
				showFullScreen(view);
			}

		};
		worker.execute();
		//		Thread thread = new Thread(new Runnable() {
		//
		//			public void run() {
		//				synchronized (view) {
		//					while (!view.isReady()) {
		//						try {
		//							view.wait();
		//						} catch (InterruptedException e) {
		//							// try again
		//						}
		//					}
		//
		//					vm.addView(view);
		//					updateViews();
		//					showFullScreen(view);
		//				}
		//
		//			}
		//
		//		});
		//		thread.start();
	}

	public void updateViews() {
		this.grid.updateItems();
	}

	public UITopiaController getController() {
		return this.controller;
	}

	public void viewFocusGained() {
	}

	public void viewFocusLost() {
		if (view != null) {
			view.captureNow();
		}
	}
}
