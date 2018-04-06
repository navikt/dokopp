package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.DokoppFunctionalException;
import no.nav.dokopp.qopp001.service.ServiceOrchestrator;
import no.nav.dokopp.util.ValidatorFeilhaandtering;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
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
	private int maximumRedeliveries;
	
	@Value("${DOKOPP_QOPP001_MAXIMUMREDELIVERYDELAYMS}")
	private int maximumRedeliveryDelayMs;
	
	@Value("${DOKOPP_QOPP001_REDELIVERYDELAYMS}")
	private int redeliveryDelayMs;
	
	@Value("${DOKOPP_QOPP001_BACKOFFMULTIPLIER}")
	private int backoffMultiplier;
	
	private final Queue qopp001;
	private final Queue functionalBOQ;
	private final ValidatorFeilhaandtering validatorFeilhaandtering;
	private final ServiceOrchestrator serviceOrchestrator;
	
	@Inject
	public Qopp001Route(Queue qopp001,
						Queue functionalBOQ,
						ValidatorFeilhaandtering validatorFeilhaandtering,
						ServiceOrchestrator serviceOrchestrator) {
		this.qopp001 = qopp001;
		this.validatorFeilhaandtering = validatorFeilhaandtering;
		this.serviceOrchestrator = serviceOrchestrator;
		this.functionalBOQ = functionalBOQ;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.maximumRedeliveries(maximumRedeliveries)
				.maximumRedeliveryDelay(maximumRedeliveryDelayMs)
				.redeliveryDelay(redeliveryDelayMs)
				.backOffMultiplier(backoffMultiplier)
				.useExponentialBackOff()
				.retryAttemptedLogLevel(LoggingLevel.INFO)
				.logRetryStackTrace(false)
				.logExhaustedMessageBody(true)
				.loggingLevel(LoggingLevel.ERROR));
		
		onException(DokoppFunctionalException.class)
				.handled(true)
				.maximumRedeliveries(0)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.logRetryAttempted(false)
				.to("jms:" + functionalBOQ.getQueueName());
		
		from("jms:" + qopp001.getQueueName() +
				"?transacted=true" +
				"&cacheLevelName=CACHE_CONSUMER" +
				"&errorHandlerLogStackTrace=false" +
				"&errorHandlerLoggingLevel=DEBUG")
				.routeId(SERVICE_ID)
				.doTry()
				.to("validator:xsd/opprett_oppgave.xsd")
				.unmarshal(new JaxbDataFormat(OpprettOppgave.class.getPackage().getName()))
				.doCatch(SchemaValidationException.class, Exception.class)
				.bean(validatorFeilhaandtering)
				.end()
				.setProperty(PROPERTY_JOURNALPOST_ID, simple("${body.arkivKode}", String.class))
				.log("Qopp001 har mottatt og validert forespørsel med journalpostId= ${property." + PROPERTY_JOURNALPOST_ID + "} OK.")
				.bean(serviceOrchestrator);
	}
}
