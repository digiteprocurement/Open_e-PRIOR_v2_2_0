package eu.europa.ec.cipa.adapter.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

@Configuration
public class Basic {

	@Bean
	public DataSource activitiDs() {
		final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		//DataSource dataSource = dsLookup.getDataSource("jdbc/activityDs");
		DataSource dataSource = dsLookup.getDataSource("java:jboss/datasources/activityDs");
		return dataSource;
	}

	@Bean
	public DataSource oxalysDs() {
		final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		//DataSource dataSource = dsLookup.getDataSource("jdbc/oxalysDs");
		DataSource dataSource = dsLookup.getDataSource("java:jboss/datasources/oxalysDs");
		return dataSource;
	}

	@Bean
	public DataSource etrustexDs() {
		final JndiDataSourceLookup dsLookup = new JndiDataSourceLookup();
		dsLookup.setResourceRef(true);
		//DataSource dataSource = dsLookup.getDataSource("jdbc/eTrustExDs");
		DataSource dataSource = dsLookup.getDataSource("java:jboss/datasources/eTrustExDs");
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		//return new WebLogicJtaTransactionManager();
		return new JtaTransactionManager();
	}

}
