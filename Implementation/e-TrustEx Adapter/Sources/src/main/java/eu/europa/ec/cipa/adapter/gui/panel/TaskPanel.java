package eu.europa.ec.cipa.adapter.gui.panel;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.task.Task;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.NullValidator;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import eu.europa.ec.cipa.adapter.gui.Controller;
import eu.europa.ec.cipa.adapter.gui.TaskSelectObserver;
import eu.europa.ec.cipa.adapter.gui.TaskUpdateObserver;
import eu.europa.ec.cipa.adapter.model.activiti.TaskWrapper;

public class TaskPanel extends Panel implements TaskSelectObserver {

	private Controller controller;
	
	public TaskPanel(Controller controller) {
		super();
		init();
		this.controller=controller;
	}

	private FieldGroup fieldGroup = new FieldGroup();
	private Button saveButton = new Button("Save");
	private ComboBox reasonSelect = new ComboBox("Decision");
	private TextArea errorTa = new TextArea("Error description");
	private Task task;
	
	public void init() {
		FormLayout form = new FormLayout();
		form.setWidth(800, Unit.PIXELS);
		setContent(form);

		TextField tfname = new TextField("Name");
		tfname.setWidth(200, Unit.PIXELS);
		tfname.setEnabled(false);
		fieldGroup.bind(tfname, "name");
		form.addComponent(tfname);

		TextField tfpi = new TextField("Process instance");
		tfpi.setWidth(200, Unit.PIXELS);
		tfpi.setEnabled(false);
		fieldGroup.bind(tfpi, "processInstanceId");
		form.addComponent(tfpi);

		DateField creationDate = new DateField("Creation Date");
		fieldGroup.bind(creationDate, "createTime");
		creationDate.setEnabled(false);
		form.addComponent(creationDate);

		
		errorTa.setWidth(800, Unit.PIXELS);
		errorTa.setWordwrap(true);
		errorTa.setRows(10);
		//fieldGroup.bind(errorTa, "errorMessage");
		errorTa.setEnabled(false);
		form.addComponent(errorTa);

		
		reasonSelect.setRequired(true);
		reasonSelect.addValidator(new NullValidator(
				"Cannot be null, a value must be chosen.", false));
		
		reasonSelect.addItem("retry");
		reasonSelect.addItem("cancel");
		reasonSelect.addItem("restart");

		form.addComponent(reasonSelect);

		form.addComponent(saveButton);
		saveButton.setDisableOnClick(true);
		saveButton.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				saveButton.setDisableOnClick(true);
				if (task==null){
					Notification notification = new Notification("Error", "No task selected",
			                Type.ERROR_MESSAGE, true);
			        notification.setDelayMsec(2500);
			        notification.show(Page.getCurrent());
				}
				controller.setVariable(task.getExecutionId(), "errorDecision", reasonSelect.getValue());
				controller.completeTask(task.getId());
				for (TaskUpdateObserver o : taskUpdateObservers){
					o.notifyTaskUpdate(task);
				}
			}
		});
		
	}

	private void setTask(Task task) {
		if (task==null){
			this.task=null;
			saveButton.setDisableOnClick(true);
			return;
		}
		
		this.task=task;
		BeanItem<TaskWrapper> bit = new BeanItem<TaskWrapper>(new TaskWrapper(
				task));
		fieldGroup.setItemDataSource(bit);
		
		errorTa.setValue((String)controller.getVariable(task.getExecutionId(), "errorMessage"));
		
		if ("Handle Send Doc Error".equals(task.getName())){
			if (reasonSelect.getItem("restart")==null){
				reasonSelect.addItem("restart");		
			}
		}else if (reasonSelect.getItem("restart")!=null){
			reasonSelect.removeItem("restart");
		}
		saveButton.setDisableOnClick(false);
	}


	@Override
	public void notifyTaskSelection(Task t) {
		setTask(t);
		
	}

	private List<TaskUpdateObserver> taskUpdateObservers = new ArrayList<TaskUpdateObserver>();
	
	public void addTaskUpdateObserver(TaskUpdateObserver o){
		taskUpdateObservers.add(o);
	}
	
}
