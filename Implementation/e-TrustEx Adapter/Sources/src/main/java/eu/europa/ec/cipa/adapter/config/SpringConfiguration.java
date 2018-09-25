package eu.europa.ec.cipa.adapter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ Basic.class, Camel.class, Activiti.class, DAO.class, GUI.class, Services.class })
public class SpringConfiguration {

}
