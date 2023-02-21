package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.AvsluttBehandlingOgKastMeldingException;
import no.nav.dokopp.exception.DokoppFunctionalException;
import no.nav.dokopp.exception.ReturpostAlleredeFlaggetException;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;
import org.apache.camel.support.processor.validation.SchemaValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Queue;

@Slf4j
@Component
public class Qopp001Route extends RouteBuilder {

	public static final String SERVICE_ID = "QOPP001";
	public static final String PROPERTY_JOURNALPOST_ID = "journalpostId";
	public static final String PROPERTY_ORIGINAL_MESSAGE = "originalMessage";

	private final Queue qopp001;
	private final Queue qopp001FunksjonellFeil;
	private final Qopp001Service qopp001Service;

	@Autowired
	public Qopp001Route(Queue qopp001,
						Queue qopp001FunksjonellFeil,
						Qopp001Service qopp001Service) {
		this.qopp001 = qopp001;
		this.qopp001Service = qopp001Service;
		this.qopp001FunksjonellFeil = qopp001FunksjonellFeil;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure() throws Exception {
		errorHandler(defaultErrorHandler()
				.retryAttemptedLogLevel(LoggingLevel.INFO)
				.logRetryStackTrace(false)
				.logExhaustedMessageBody(true)
				.loggingLevel(LoggingLevel.ERROR));

		onException(DokoppFunctionalException.class, SchemaValidationException.class)
				.handled(true)
				.maximumRedeliveries(0)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.logStackTrace(false)
				.logRetryAttempted(false)
				.log(LoggingLevel.WARN, log, "${exception}, journalpostId=" + "${exchangeProperty." + PROPERTY_JOURNALPOST_ID + "}")
				.setBody(simple("${exchangeProperty." + PROPERTY_ORIGINAL_MESSAGE + "}"))
				.to("jms:" + qopp001FunksjonellFeil.getQueueName());

		onException(AvsluttBehandlingOgKastMeldingException.class, ReturpostAlleredeFlaggetException.class)
				.handled(true)
				.maximumRedeliveries(0)
				.logExhaustedMessageBody(false)
				.logExhaustedMessageHistory(false)
				.log(LoggingLevel.WARN, log, "Avslutter behandling og kaster melding. ${exception}");

		from("jms:" + qopp001.getQueueName() +
				"?transacted=true" +
				"&cacheLevelName=CACHE_CONSUMER" +
				"&errorHandlerLogStackTrace=false" +
				"&errorHandlerLoggingLevel=DEBUG")
				.routeId(SERVICE_ID)
				.setProperty(PROPERTY_ORIGINAL_MESSAGE, simple("${body}"))
				.process(new IdsProcessor())
				.to("validator:xsd/opprett_oppgave.xsd")
				.unmarshal(new JaxbDataFormat(OpprettOppgave.class.getPackage().getName()))
				.setProperty(PROPERTY_JOURNALPOST_ID, simple("${body.arkivKode}", String.class))
				.log(LoggingLevel.INFO, log, "qopp001 har mottatt og validert forespørsel med journalpostId=${exchangeProperty." + PROPERTY_JOURNALPOST_ID + "} OK.")
				.bean(qopp001Service);
	}
}
