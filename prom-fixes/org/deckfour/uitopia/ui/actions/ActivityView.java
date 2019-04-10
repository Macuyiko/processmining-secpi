package org.deckfour.uitopia.ui.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.deckfour.uitopia.api.hub.TaskManager;
import org.deckfour.uitopia.api.model.Task;
import org.deckfour.uitopia.ui.components.ImageButton;
import org.deckfour.uitopia.ui.components.TiledPanel;
import org.deckfour.uitopia.ui.components.ViewHeaderBar;
import org.deckfour.uitopia.ui.util.ArrangementHelper;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.Tooltips;

import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerDecorator;


public class ActivityView extends JPanel {

	private static final long serialVersionUID = -8449309433547207349L;
	private static final int BEZEL_WIDTH = 700;

	private final TaskManager<?, ?> manager;
	private final List<Task<?>> tasks;
	private final List<TaskView> taskViews;
	private final ActionListener listener;

	private final JPanel listPanel;
	private final JScrollPane scrollPane;

	public ActivityView(final TaskManager<?, ?> manager, final ActionListener closeListener) {
		this.manager = manager;
		tasks = new ArrayList<Task<?>>();
		taskViews = new ArrayList<TaskView>();
		tasks.addAll(this.manager.getActiveTasks());
		listener = closeListener;
		setLayout(new BorderLayout());
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder());
		final ImageButton removeButton = new ImageButton(ImageLoader.load("remove_30x30_black.png"), new Color(80, 80,
		        80), new Color(140, 140, 140), 0);
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "closed"));
			}
		});
		removeButton.setToolTipText(Tooltips.ACTIONREMOVEBUTTON);
		final ViewHeaderBar header = new ViewHeaderBar("Activity");
		header.addComponent(removeButton);
		this.add(header, BorderLayout.NORTH);
		final JPanel contents = new TiledPanel(ImageLoader.load("tile_metal.jpg"));
		contents.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contents.setLayout(new BorderLayout());
		final RoundedPanel bezel = new RoundedPanel(20, 10, 20);
		bezel.setBackground(new Color(30, 30, 30));
		bezel.setLayout(new BorderLayout());
		bezel.setMinimumSize(new Dimension(BEZEL_WIDTH, 500));
		bezel.setMaximumSize(new Dimension(BEZEL_WIDTH, 1000));
		bezel.setPreferredSize(new Dimension(BEZEL_WIDTH, 800));
		final JLabel label = new JLabel("Tasks");
		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		label.setOpaque(false);
		label.setForeground(new Color(180, 180, 180));
		label.setFont(label.getFont().deriveFont(18f).deriveFont(Font.BOLD));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
		listPanel = new JPanel();
		listPanel.setOpaque(false);
		listPanel.setBorder(BorderFactory.createEmptyBorder());
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.add(Box.createVerticalGlue());
		scrollPane = new JScrollPane(listPanel);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(scrollPane.getVerticalScrollBar(), new Color(0, 0, 0, 0),
		        new Color(200, 200, 200), new Color(120, 120, 120));
		scrollPane.getVerticalScrollBar().setOpaque(false);
		bezel.add(ArrangementHelper.centerHorizontally(label), BorderLayout.NORTH);
		bezel.add(scrollPane, BorderLayout.CENTER);
		contents.add(ArrangementHelper.centerHorizontally(ArrangementHelper.centerVertically(bezel)));
		this.add(contents, BorderLayout.CENTER);
		update();
	}

	public synchronized void update() {
		final List<? extends Task<?>> activeTasks = manager.getActiveTasks();
		for (final TaskView view : taskViews) {
			view.updateState();
		}
		for (int i = activeTasks.size() - 1; i >= 0; i--) {
			final Task<?> current = activeTasks.get(i);
			if (tasks.contains(current) == false) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						TaskView view = createView(current);
						view.updateState();
					}
				});
			}
		}
	}

	private synchronized TaskView createView(final Task<?> task) {
		if (tasks.contains(task)) {
			return taskViews.get(tasks.indexOf(task));
		}
		tasks.add(0, task);
		final TaskView view = new TaskView(this, task);
		taskViews.add(0, view);
		listPanel.add(view, 0);
		scrollPane.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
		return view;
	}

	public synchronized void log(final Task<?> task, final String message) {
		if (!tasks.contains(task)) {
			update();
		}
		int i = tasks.indexOf(task);
		if (i < 0) {
			// Tasks potentially complete so fast that no view was created yet.
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					final TaskView view = createView(task);
					view.appendLog(message);
				}
			});
			i = 0;
		} else {
			final TaskView view = taskViews.get(i);
			view.appendLog(message);
		}
	}

	public TaskManager<?, ?> getManager() {
		return manager;
	}
}
