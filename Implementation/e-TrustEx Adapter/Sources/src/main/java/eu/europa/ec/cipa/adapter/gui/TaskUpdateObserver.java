package eu.europa.ec.cipa.adapter.gui;

import org.activiti.engine.task.Task;

public interface TaskUpdateObserver {

	void notifyTaskUpdate(Task task);
	
}
