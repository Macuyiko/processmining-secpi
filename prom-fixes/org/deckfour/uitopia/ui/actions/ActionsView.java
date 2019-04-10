
package org.deckfour.uitopia.ui.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.deckfour.uitopia.api.hub.ActionManager;
import org.deckfour.uitopia.api.hub.ResourceManager;
import org.deckfour.uitopia.api.model.Action;
import org.deckfour.uitopia.api.model.Resource;
import org.deckfour.uitopia.api.model.Task;
import org.deckfour.uitopia.ui.UITopiaController;
import org.deckfour.uitopia.ui.components.ActivityButton;
import org.deckfour.uitopia.ui.components.TiledPanel;
import org.deckfour.uitopia.ui.components.ViewHeaderBar;
import org.deckfour.uitopia.ui.main.Viewable;
import org.deckfour.uitopia.ui.util.ImageLoader;

public class ActionsView extends JPanel implements Viewable {

	private static final long serialVersionUID = 7735337852528303426L;

	private UITopiaController controller;

	private ActionsBrowser actionsBrowser;
	private ActionsInputBrowser inputBrowser;
	private ActionsOutputBrowser outputBrowser;

	private ActivityView activityView;

	private JPanel actionsView;
	private boolean activityViewShown;

	private ActivityButton activityButton;

	private ViewHeaderBar header;

	public ActionsView(UITopiaController controller) {
		this.controller = controller;
		this.actionsBrowser = new ActionsBrowser(this);
		this.inputBrowser = new ActionsInputBrowser(this);
		this.outputBrowser = new ActionsOutputBrowser(this);
		this.activityButton = new ActivityButton(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showActivityView();
			}
		});
		this.activityButton.setSpinning(false);

		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.setBorder(BorderFactory.createEmptyBorder());
		actionsView = new JPanel();
		actionsView.setLayout(new BorderLayout());
		actionsView.setOpaque(true);
		actionsView.setBorder(BorderFactory.createEmptyBorder());
		header = new ViewHeaderBar("Actions");
		header.addComponent(this.activityButton);
		actionsView.add(header, BorderLayout.NORTH);
		JPanel contents = new TiledPanel(ImageLoader.load("tile_metal.jpg"));
		contents.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contents.setLayout(new BoxLayout(contents, BoxLayout.X_AXIS));
		contents.add(Box.createHorizontalGlue());
		contents.add(inputBrowser);
		contents.add(actionsBrowser);
		contents.add(outputBrowser);
		contents.add(Box.createHorizontalGlue());
		actionsView.add(contents, BorderLayout.CENTER);
		activityViewShown = false;
		this.add(actionsView, BorderLayout.CENTER);
		this.actionsBrowser.updateFilter();
		this.activityView = new ActivityView(controller.getFrameworkHub()
				.getTaskManager(), new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showActionsView();
			}
		});

	}

	public ViewHeaderBar getHeader() {
		return header;
	}

	public void showActivityView() {
		updateActivity();
		this.removeAll();
		this.add(activityView, BorderLayout.CENTER);
		activityViewShown = true;
		activityView.update();
		this.revalidate();
		this.repaint();
	}

	public void showActionsView() {
		this.removeAll();
		this.add(actionsView, BorderLayout.CENTER);
		activityViewShown = false;
		this.revalidate();
		this.repaint();
	}

	public void setInputResource(Resource input) {
		inputBrowser.reset();
		outputBrowser.reset();
		actionsBrowser.reset();
		inputBrowser.setConstraint(input);
		inputConstraintsUpdated();
	}

	public void reset() {
		inputBrowser.reset();
		outputBrowser.reset();
		actionsBrowser.reset();
	}

	public void actionSelected(Action action) {
		if (action != null) {
			inputBrowser.setParameters(action.getInput());
			outputBrowser.setParameters(action.getOutput());
		}
	}

	public void inputParametersUpdated() {
		actionsBrowser.checkExecutability();
	}

	public void inputConstraintsUpdated() {
		actionsBrowser.updateFilter();
	}

	public void outputConstraintsUpdated() {
		actionsBrowser.updateFilter();
	}

	public ActionsBrowser getActionsBrowser() {
		return this.actionsBrowser;
	}

	public ActionsInputBrowser getInputBrowser() {
		return this.inputBrowser;
	}

	public ActionsOutputBrowser getOutputBrowser() {
		return this.outputBrowser;
	}

	public UITopiaController getController() {
		return this.controller;
	}

	public ActionManager<? extends Action> getActionManager() {
		return this.controller.getFrameworkHub().getActionManager();
	}

	public ResourceManager<? extends Resource> getResourceManager() {
		return this.controller.getFrameworkHub().getResourceManager();
	}

	public void updateActivity() {
		if (this.controller.getFrameworkHub().getTaskManager().getActiveTasks()
				.size() > 0) {
			this.activityButton.setSpinning(true);
		} else {
			this.activityButton.setSpinning(false);
		}
		this.activityView.update();
	}

	public void updateTaskLog(Task<?> task, String message) {
		this.activityView.log(task, message);
	}

	public void viewFocusGained() {
		inputBrowser.updateFields();
		actionsBrowser.setFocus();
	}

	public void viewFocusLost() {
		if (activityViewShown) {
			showActionsView();
		}
	}

}
