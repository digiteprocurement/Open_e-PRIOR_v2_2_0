package eu.europa.ec.cipa.adapter.config.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeppolRouteBuilder extends RouteBuilder {

	@Autowired
	JdbcMessageIdRepository jdbcMessageIdRepository;

	@Override
	public void configure() throws Exception {

		from(
				"quartz2://AdapterScheduler?trigger.repeatCount=0&recoverableJob=true")
				.from("sql://select * from OXA_ADAPTER_INT where PROCESSED = 'N'?dataSource=oxalysDs&maxMessagesPerPoll=10&"
						+ "consumer.delay=10000&consumer.useFixedDelay=true&"
						+ "consumer.onConsume=update OXA_ADAPTER_INT set PROCESSED = 'Y' where OXA_ID = :#OXA_ID")
				.filter(simple("${body} != null"))
				.idempotentConsumer(simple("${body[OXA_ID]}"),
						jdbcMessageIdRepository).skipDuplicate(true) // don't
																		// process
																		// messages
																		// already
																		// processed
																		// or
																		// under
																		// processing
				.to("direct:document");

		from("direct:document").log("invoice ${body}").to(
				"activiti:document-0.6");

	}

}
