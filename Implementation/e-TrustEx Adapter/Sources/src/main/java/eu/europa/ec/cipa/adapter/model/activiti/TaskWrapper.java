package eu.europa.ec.cipa.adapter.model.activiti;

import java.util.Date;

import org.activiti.engine.task.Task;

import eu.europa.ec.cipa.adapter.services.DocumentService;

public class TaskWrapper {

	private Task task;

	public TaskWrapper(Task task) {
		this.task = task;
	}

	public String getProcessInstanceId() {
		String v = "";
		if (task != null)
			v = task.getProcessInstanceId();
		return v;
	}

	public String getName() {
		String v = "";
		if (task != null)
			v = task.getName();
		return v;
	}

	public Date getCreateTime() {
		Date v = null;
		if (task != null)
			v = task.getCreateTime();
		return v;
	}

	public String getErrorMessage() {

		String error = "";
		if (task != null) {
			error = (String) task.getProcessVariables().get(
					DocumentService.PROCESS_PARAM_ERROR_MESSAGE);
		}
		if (error == null) {
			error = "No error message available";
		}
		return error;
	}

}
