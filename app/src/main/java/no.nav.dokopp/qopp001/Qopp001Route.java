package no.nav.dokopp.qopp001;

import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.qopp001.oppgavebehandlingV3.OpprettOppgave;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
//TODO: 2xTjoark calls + config and exception handling
@Component
public class Qopp001Route extends SpringRouteBuilder {
	
	public static final String SERVICE_ID = "qmot004";
	
	private final DokoppProperties dokoppProperties;
	private final Queue qopp001;
	private final OpprettOppgave opprettOppgave;
	
	@Inject
	public Qopp001Route(DokoppProperties dokoppProperties,
						Queue qopp001,
						OpprettOppgave opprettOppgave) {
		this.dokoppProperties = dokoppProperties;
		this.qopp001 = qopp001;
		this.opprettOppgave = opprettOppgave;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		from(jmsEndpoint(qopp001))
				.routeId(SERVICE_ID)
				.bean(opprettOppgave);
	}
	
	private String jmsEndpoint(final Queue queue) throws JMSException {
		String[] split = queue.getQueueName().split("/");
		return "jms:" + split[split.length - 1] + "?transacted=true&cacheLevelName=CACHE_CONSUMER&errorHandlerLogStackTrace=false&errorHandlerLoggingLevel=DEBUG";
	}
}
