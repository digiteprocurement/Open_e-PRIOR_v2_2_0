package eu.europa.ec.cipa.adapter.config;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.activiti.camel.ActivitiComponent;
import org.activiti.camel.CamelBehaviour;
import org.activiti.camel.ContextProvider;
import org.activiti.camel.SimpleContextProvider;
import org.apache.camel.CamelContext;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.europa.ec.cipa.adapter.config.routes.PeppolRouteBuilder;

@SuppressWarnings("deprecation")
@Configuration
public class Camel extends CamelConfiguration {

	@Autowired
	private DataSource activitiDs;

	@Bean
	public PeppolRouteBuilder peppolRouteBuilder() {
		return new PeppolRouteBuilder();
	}

	@Bean
	CamelBehaviour camelBehaviour() throws Exception {

		List<ContextProvider> l = new ArrayList<ContextProvider>();
		l.add(new SimpleContextProvider("testFlow", camelContext()));

		CamelBehaviour o = new CamelBehaviour(l);
		return o;
	}

	@Bean
	JdbcMessageIdRepository jdbcMessageIdRepository() throws Exception {
		return new JdbcMessageIdRepository(activitiDs,
				"myJdbcMessageIdRepository");
	}

	@Override
	protected void setupCamelContext(CamelContext camelContext)
			throws Exception {

		ActivitiComponent activitiComponent = new ActivitiComponent();
		activitiComponent.setCamelContext(camelContext);

		QuartzComponent quartzComponent = new QuartzComponent(camelContext);
		quartzComponent.setPropertiesFile("custom-quartz.properties");

		SqlComponent sqlComponent = new SqlComponent(camelContext);

		camelContext.addComponent("quartz2", quartzComponent);
		camelContext.addComponent("activity", activitiComponent);
		camelContext.addComponent("sql", sqlComponent);
		camelContext.addRoutes(peppolRouteBuilder());
		camelContext.start();

	}

}
