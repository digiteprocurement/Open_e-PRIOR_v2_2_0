package eu.europa.ec.cipa.adapter.gui;


import java.util.List;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;

import eu.europa.ec.cipa.adapter.services.DocumentService;
import eu.europa.ec.cipa.adapter.utils.Constants;

public class Controller {
	
	
	private DocumentService documentService;
	private RuntimeService runtimeService;
	private TaskService taskService;

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}
	
	public List<Task> getPendingTasks(){
		return documentService.getPendingTasks();
	}
	
	public boolean authenticate(String user, String password){
		if(Constants.AS2ADMIN.equals(user) && Constants.AS2ADMIN.equals(password)){
			return true;
		}
		return false;
	}

	public Object getVariable(String executionId, String variableName){
		return getRuntimeService().getVariable(executionId, variableName);
	}
	
	public void setVariable(String executionId, String variableName, Object variableValue){
		getRuntimeService().setVariableLocal(executionId, variableName, variableValue);
	}
	
	public void completeTask(String taskId){
		getTaskService().complete(taskId);
	}
	
	public RuntimeService getRuntimeService() {
		return runtimeService;
	}

	public void setRuntimeService(RuntimeService runtimeService) {
		this.runtimeService = runtimeService;
	}

	public TaskService getTaskService() {
		return taskService;
	}

	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
	

}
