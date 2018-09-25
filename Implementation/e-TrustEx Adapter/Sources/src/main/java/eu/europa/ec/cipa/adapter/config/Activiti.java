package eu.europa.ec.cipa.adapter.config;

import javax.sql.DataSource;

import org.activiti.engine.FormService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Activiti implements ApplicationContextAware {

	ApplicationContext applicationContext;
	
	private static Logger logger = Logger.getLogger(Activiti.class);

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;

	}

	@Autowired
	PlatformTransactionManager transactionManager;

	@Autowired
	DataSource activitiDs;

	@Bean
	public ProcessEngine processEngine() throws Exception {
		ProcessEngineFactoryBean pe = new ProcessEngineFactoryBean();
		pe.setProcessEngineConfiguration(processEngineConfiguration());
		pe.setApplicationContext(applicationContext);
		pe.getProcessEngineConfiguration().setJobExecutorActivate(true);
		pe.getProcessEngineConfiguration().setAsyncExecutorActivate(true);
		return pe.getObject();
	}

	@Bean
	public ProcessEngineConfigurationImpl processEngineConfiguration() {
		SpringProcessEngineConfiguration pec = new SpringProcessEngineConfiguration();

		pec.setDataSource(activitiDs);
		//pec.setDatabaseType(SpringProcessEngineConfiguration.DATABASE_TYPE_ORACLE);
		pec.setDatabaseType(SpringProcessEngineConfiguration.DATABASE_TYPE_MYSQL);
		pec.setTransactionManager(transactionManager);
		pec.setApplicationContext(applicationContext);
		Resource documentResource = new ClassPathResource(
				"process/Document-0_6.bpmn20.xml.bpmn");
		pec.setDeploymentResources(new Resource[] { documentResource });
		return pec;
	}

	@Bean(name = "runtimeService")
	public RuntimeService runtimeService(ProcessEngine processEngine) {
		return processEngine.getRuntimeService();
	}

	@Bean(name = "identityService")
	public IdentityService identityService(ProcessEngine processEngine) {
		return processEngine.getIdentityService();
	}

	@Bean(name = "formService")
	public FormService formService(ProcessEngine processEngine) {
		return processEngine.getFormService();
	}

	@Bean(name = "managementService")
	public ManagementService managementService(ProcessEngine processEngine) {
		return processEngine.getManagementService();
	}

	@Bean(name = "repositoryService")
	public RepositoryService repositoryService(ProcessEngine processEngine) {
		return processEngine.getRepositoryService();
	}

	@Bean(name = "taskService")
	public TaskService taskService(ProcessEngine processEngine) {
		return processEngine.getTaskService();
	}

}
