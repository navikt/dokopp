package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.qopp001.service.ServiceOrchestrator;
import no.nav.dokopp.qopp001.support.OpprettOppgaveInputMapper;
import no.nav.dokopp.qopp001.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.qopp001.tjoark122.Tjoark122HentJournalpostInfo;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgave;
import no.nav.dokopp.util.ValidatorFeilhaandtering;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.processor.validation.SchemaValidationException;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.Queue;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Component
public class Qopp001Route extends SpringRouteBuilder {
	
	public static final String SERVICE_ID = "qopp001";
	public static final String PROPERTY_JOURNALPOST_ID = "journalpostId";
	
	private final DokoppProperties dokoppProperties;
	private final Queue qopp001;
	private final OpprettOppgave opprettOppgave;
	private final OpprettOppgaveInputMapper OpprettOppgaveInputMapper;
	private final Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;
	private final ValidatorFeilhaandtering validatorFeilhaandtering;
	private final ServiceOrchestrator serviceOrchestrator;
	
	@Inject
	public Qopp001Route(DokoppProperties dokoppProperties,
						Queue qopp001,
						OpprettOppgave opprettOppgave,
						OpprettOppgaveInputMapper OpprettOppgaveInputMapper,
						Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo,
						Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter,
						ValidatorFeilhaandtering validatorFeilhaandtering,
						ServiceOrchestrator serviceOrchestrator) {
		this.dokoppProperties = dokoppProperties;
		this.qopp001 = qopp001;
		this.opprettOppgave = opprettOppgave;
		this.OpprettOppgaveInputMapper = OpprettOppgaveInputMapper;
		this.tjoark122HentJournalpostInfo = tjoark122HentJournalpostInfo;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
		this.validatorFeilhaandtering = validatorFeilhaandtering;
		this.serviceOrchestrator = serviceOrchestrator;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		from("jms:" + qopp001.getQueueName() +
				"?transacted=true" +
				"&cacheLevelName=CACHE_CONSUMER" +
				"&errorHandlerLogStackTrace=false" +
				"&errorHandlerLoggingLevel=DEBUG")
				.routeId(SERVICE_ID)
				.doTry()
				.to("validator:xsd/opprett_oppgave.xsd")
				.unmarshal(new JaxbDataFormat(no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave.class.getPackage().getName()))
				.doCatch(SchemaValidationException.class, Exception.class)
				.bean(validatorFeilhaandtering)
				.end()
				.setProperty(PROPERTY_JOURNALPOST_ID, simple("${body.arkivKode}", String.class))
				.log("Qopp001 har mottatt og validert forespørsel med journalpostId= ${property." + PROPERTY_JOURNALPOST_ID + "} OK.")
				.bean(serviceOrchestrator);
	}
	
	
}
