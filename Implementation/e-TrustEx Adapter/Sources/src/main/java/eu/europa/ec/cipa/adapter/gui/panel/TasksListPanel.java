package eu.europa.ec.cipa.adapter.gui.panel;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.task.Task;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;

import eu.europa.ec.cipa.adapter.gui.Controller;
import eu.europa.ec.cipa.adapter.gui.TaskSelectObserver;
import eu.europa.ec.cipa.adapter.gui.TaskUpdateObserver;

public class TasksListPanel extends Panel implements TaskUpdateObserver {

	private static final String TASKS_LIST = "Tasks list";
	private ListSelect select = new ListSelect(TASKS_LIST);
	private Button refreshButton;
	
	private Controller controller;

	public TasksListPanel(Controller controller) {
		super();
		init();
		this.controller=controller;
	}

	private void init() {
		FormLayout form = new FormLayout();
		setContent(form);

		refreshButton = new Button("Refresh");
		
		refreshButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				updateTaskList();

			}
		});
		
		form.addComponent(refreshButton);

		select.setNullSelectionAllowed(false);
		select.setMultiSelect(false);
		select.setRows(10);
		
		select.addValueChangeListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				Object p = event.getProperty().getValue();
				for (TaskSelectObserver o : taskSelectObservers){
					o.notifyTaskSelection((Task) p);
				}
			}
		});

		form.addComponent(select);

	}

	public void updateTaskList() {
		
		select.clear();
		select.removeAllItems();

		for (Task t : controller.getPendingTasks()) {
			Item item = select.addItem(t);
			select.setItemCaption(
					item,
					"Task " + t.getId() + ", " + t.getName() + ", "
							+ t.getProcessInstanceId() + ","
							+ t.getCreateTime());
		}
	}
	
	private List<TaskSelectObserver> taskSelectObservers= new ArrayList<TaskSelectObserver>();
	
	public void addTaskSelectObserver(TaskSelectObserver o){
		taskSelectObservers.add(o);
	}

	@Override
	public void notifyTaskUpdate(Task task) {
		updateTaskList();
		
	}
	
}
