package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.DokoppFunctionalException;
import no.nav.dokopp.qopp001.service.ServiceOrchestrator;
import no.nav.dokopp.util.ValidatorFeilhaandtering;
import org.apache.camel.LoggingLevel;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.processor.validation.SchemaValidationException;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Value("${DOKOPP_QOPP001_MAXIMUMREDELIVERIES}")
	private int DOKOPP_QOPP001_MAXIMUMREDELIVERIES;
	
	@Value("${DOKOPP_QOPP001_MAXIMUMREDELIVERYDELAYMS}")
	private int DOKOPP_QOPP001_MAXIMUMREDELIVERYDELAYMS;
	
	@Value("${DOKOPP_QOPP001_REDELIVERYDELAYMS}")
	private int DOKOPP_QOPP001_REDELIVERYDELAYMS;
	
	@Value("${DOKOPP_QOPP001_BACKOFFMULTIPLIER}")
	private int DOKOPP_QOPP001_BACKOFFMULTIPLIER;
	
	private final Queue qopp001;
	private final ValidatorFeilhaandtering validatorFeilhaandtering;
	private final ServiceOrchestrator serviceOrchestrator;
	
	@Inject
	public Qopp001Route(Queue qopp001,
						ValidatorFeilhaandtering validatorFeilhaandtering,
						ServiceOrchestrator serviceOrchestrator) {
		this.qopp001 = qopp001;
		this.validatorFeilhaandtering = validatorFeilhaandtering;
		this.serviceOrchestrator = serviceOrchestrator;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(DOKOPP_QOPP001_MAXIMUMREDELIVERIES)
				.maximumRedeliveryDelay(DOKOPP_QOPP001_MAXIMUMREDELIVERYDELAYMS)
				.redeliveryDelay(DOKOPP_QOPP001_REDELIVERYDELAYMS)
				.backOffMultiplier(DOKOPP_QOPP001_BACKOFFMULTIPLIER)
				.useExponentialBackOff()
				.retryAttemptedLogLevel(LoggingLevel.ERROR)
				.logRetryStackTrace(false)
				.logExhaustedMessageBody(false)
				.loggingLevel(LoggingLevel.ERROR));
		
		onException(DokoppFunctionalException.class)
				.handled(true)
				.maximumRedeliveries(0)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.logRetryAttempted(false);
		
		from("jms:" + qopp001.getQueueName() +
				"?transacted=true" +
				"&cacheLevelName=CACHE_CONSUMER" +
				"&errorHandlerLogStackTrace=false" +
				"&errorHandlerLoggingLevel=DEBUG")
				.routeId(SERVICE_ID)
				.doTry()
				.to("validator:xsd/opprett_oppgave.xsd")
				.unmarshal(new JaxbDataFormat(no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave.class.getPackage()
						.getName()))
				.doCatch(SchemaValidationException.class, Exception.class)
				.bean(validatorFeilhaandtering)
				.end()
				.setProperty(PROPERTY_JOURNALPOST_ID, simple("${body.arkivKode}", String.class))
				.log("Qopp001 har mottatt og validert forespørsel med journalpostId= ${property." + PROPERTY_JOURNALPOST_ID + "} OK.")
				.bean(serviceOrchestrator);
	}
}
