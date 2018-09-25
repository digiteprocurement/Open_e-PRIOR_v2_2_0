package eu.europa.ec.cipa.adapter.gui;

import org.activiti.engine.task.Task;

public interface TaskSelectObserver {
	
	void notifyTaskSelection(Task t);

}
