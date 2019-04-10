package org.deckfour.uitopia.ui.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.deckfour.uitopia.api.model.Task;
import org.deckfour.uitopia.ui.components.ImageButton;
import org.deckfour.uitopia.ui.util.ImageLoader;
import org.deckfour.uitopia.ui.util.Tooltips;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.ui.SlickerDarkProgressBarUI;
import com.fluxicon.slickerbox.util.ColorUtils;

public class TaskView extends JPanel {

	private static final long serialVersionUID = -5068169511564265642L;

	private static final Color BG_ACTIVE = new Color(30, 100, 30);
	private static final Color BG_PASSIVE = new Color(40, 40, 40);
	private static final int MIN_HEIGHT = 80;
	private static final int MAX_HEIGHT = 180;
	private static final Image ICON_REMOVE = ImageLoader
			.load("remove_30x30_black.png");

	private Task<?> task;

	private JPanel progressPanel;
	private JProgressBar progress;
	private ImageButton cancelButton;
	private JTextPane log;
	private JScrollPane scrollPane;
	private JLabel label;
	
	private long startTime = 0;
	private long stopTime = 0;
	
	private ActivityView parent;
	
	public TaskView(ActivityView parent, Task<?> task) {
		this.parent = parent;
		this.task = task;

		this.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
		this.setOpaque(false);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		startTime = System.currentTimeMillis();
		
		setupUI();
		setSize();
		revalidate();
		repaint();
	}

	private void setSize() {
		this.setMinimumSize(new Dimension(600, MIN_HEIGHT));
		this.setMaximumSize(new Dimension(600, MAX_HEIGHT));
		this.setPreferredSize(new Dimension(600, MIN_HEIGHT));
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
	}

	private void setupUI() {
		this.removeAll();
		label = new JLabel(task.getAction().getName());
		label.setOpaque(false);
		label.setForeground(new Color(180, 180, 180));
		label.setFont(label.getFont().deriveFont(14f));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		labelPanel.add(label);
		labelPanel.setOpaque(false);
		labelPanel.setAlignmentY(JLabel.LEFT_ALIGNMENT);

		progress = new JProgressBar();
		progress.setUI(new SlickerDarkProgressBarUI());
		progress.setMinimumSize(new Dimension(300, 20));
		progress.setMaximumSize(new Dimension(300, 20));
		progress.setPreferredSize(new Dimension(300, 20));
		progress.setMinimum(0);
		progress.setMaximum(1000);
		progress.setIndeterminate(true);
		cancelButton = new ImageButton(ICON_REMOVE, new Color(140, 140, 140),
				new Color(240, 240, 240), 0);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				task.destroy();
				revalidate();
			}
		});
		cancelButton.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		cancelButton.setToolTipText(Tooltips.ACTIONCANCELTASKBUTTON);

		progressPanel = new JPanel();
		progressPanel.setBorder(BorderFactory.createEmptyBorder());
		progressPanel.setLayout(new BorderLayout(10, 10));
		progressPanel.setOpaque(false);
		progressPanel.add(progress, BorderLayout.CENTER);
		progressPanel.add(cancelButton, BorderLayout.EAST);
		progressPanel.setAlignmentY(LEFT_ALIGNMENT);

		log = new JTextPane();
		log.setOpaque(false);
		log.setForeground(new Color(180, 180, 180));
		log.setEditable(false);
		scrollPane = new JScrollPane(log);
		scrollPane.setOpaque(false);
		scrollPane.setForeground(new Color(180, 180, 180));
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(
				scrollPane.getHorizontalScrollBar(), new Color(0, 0, 0, 0),
				new Color(40, 40, 40), new Color(80, 80, 80));
		scrollPane.getHorizontalScrollBar().setOpaque(false);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		SlickerDecorator.instance().decorate(scrollPane.getVerticalScrollBar(),
				new Color(0, 0, 0, 0), new Color(40, 40, 40),
				new Color(80, 80, 80));
		scrollPane.getVerticalScrollBar().setOpaque(false);

		this.add(labelPanel);
		this.add(Box.createVerticalStrut(5));
		this.add(progressPanel);
		this.add(Box.createVerticalStrut(5));
		this.add(scrollPane);
	}

	public void updateState() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				double prog = task.getProgress();
				if (prog >= 1.0) {
					remove(progressPanel);
					// setSize();
					// progress.setVisible(false);
					// cancelButton.setVisible(false);
				} else {
					int value = (int) (1000.0 * prog);
					if (progress.getValue() != value) {
						if (progress.isIndeterminate()) {
							progress.setIndeterminate(false);
							progress.revalidate();
						}
						progress.setValue(value);
					}
				}
				final List<? extends Task<?>> activeTasks = parent.getManager().getActiveTasks();
				if (!activeTasks.contains(task) && stopTime == 0) {
					stopTime = System.currentTimeMillis();
					double duration = (double)(stopTime - startTime) / 1000D;
					label.setText(label.getText() + " (time taken: "+duration+" sec.)");
				}
			}
		});
		Thread.yield();
	}

	private int currentHeight = 0;

	public void appendLog(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				log.setText(log.getText() + message + "\n");
				final int height = MIN_HEIGHT
						+ log.getPreferredSize().height
						+ (scrollPane.getHorizontalScrollBar().isVisible() ? scrollPane
								.getHorizontalScrollBar().getPreferredSize().height
								: 0);
				if (height != currentHeight) {
					currentHeight = Math.min(height, MAX_HEIGHT);
					setPreferredSize(new Dimension(getPreferredSize().width,
							currentHeight));
					revalidate();
					repaint();
				}
			}
		});
		Thread.yield();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int width = this.getWidth();
		int height = this.getHeight();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Color bg = BG_PASSIVE;
		if (task.getProgress() < 1.0) {
			bg = BG_ACTIVE;
		}
		g2d.setPaint(new GradientPaint(0, 20, bg, 0, height, ColorUtils.darken(
				bg, 30)));
		g2d.fillRect(0, 0, width, height);
		g2d.dispose();
	}

}
