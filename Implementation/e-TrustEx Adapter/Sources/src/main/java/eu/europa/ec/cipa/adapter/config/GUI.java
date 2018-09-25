package eu.europa.ec.cipa.adapter.config;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.europa.ec.cipa.adapter.gui.Controller;
import eu.europa.ec.cipa.adapter.services.DocumentService;

@Configuration
public class GUI {

	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	@Autowired
	private TaskService taskService;
	
	@Bean
	public Controller controller() {
		Controller c = new Controller();
		
		c.setDocumentService(documentService);
		c.setRuntimeService(runtimeService);
		c.setTaskService(taskService); 
		
		return c;
	}

}
