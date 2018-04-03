package no.nav.dokopp.qopp001.route;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.qopp001.joark.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.qopp001.tjoark122.Tjoark122HentJournalpostInfo;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgave;
import no.nav.dokopp.qopp001.support.Qopp001InputValidationProcessor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
//TODO: 2xTjoark calls + config and exception handling
@Slf4j
@Component
public class Qopp001Route extends SpringRouteBuilder {
	
	public static final String SERVICE_ID = "qopp001";
	
	private final DokoppProperties dokoppProperties;
	private final Queue qopp001;
	private final OpprettOppgave opprettOppgave;
	private final Qopp001InputValidationProcessor qopp001InputValidationProcessor;
	private final Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;
	
	@Inject
	public Qopp001Route(DokoppProperties dokoppProperties,
						Queue qopp001,
						OpprettOppgave opprettOppgave,
						Qopp001InputValidationProcessor qopp001InputValidationProcessor,
						Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo,
						Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter) {
		this.dokoppProperties = dokoppProperties;
		this.qopp001 = qopp001;
		this.opprettOppgave = opprettOppgave;
		this.qopp001InputValidationProcessor = qopp001InputValidationProcessor;
		this.tjoark122HentJournalpostInfo = tjoark122HentJournalpostInfo;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		from(jmsEndpoint(qopp001))
				.routeId(SERVICE_ID)
				.process(qopp001InputValidationProcessor)
				.bean(tjoark122HentJournalpostInfo)
				.bean(opprettOppgave)
				.bean(tjoark110SettJournalpostAttributter);
	}
	
	private String jmsEndpoint(final Queue queue) throws JMSException {
		String[] split = queue.getQueueName().split("/");
		return "jms:" + split[split.length - 1] + "?transacted=true&cacheLevelName=CACHE_CONSUMER&errorHandlerLogStackTrace=false&errorHandlerLoggingLevel=DEBUG";
	}
}
