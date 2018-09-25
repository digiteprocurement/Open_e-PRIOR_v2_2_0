package eu.europa.ec.cipa.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.europa.ec.cipa.adapter.services.DocumentService;

@Configuration
public class Services {

	@Bean
	public DocumentService documentService() {
		DocumentService service = new DocumentService();
		return service;
	}

}
