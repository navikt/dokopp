package no.nav.dokopp.consumer.oppgave;

import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS_CALLED;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_LATENCY_TIMER_BUILDER;
import static no.nav.dokopp.constants.DomainConstants.APP_NAME;
import static no.nav.dokopp.constants.DomainConstants.BEARER_PREFIX;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CONSUMER_ID;
import static no.nav.dokopp.constants.HeaderConstants.X_CORRELATION_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.consumer.sts.StsRestConsumer;
import no.nav.dokopp.exception.OpprettOppgaveFunctionalException;
import no.nav.dokopp.exception.OpprettOppgaveTechnicalException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
public class OppgaveConsumer implements Oppgave {

	private final RestTemplate restTemplate;
	private final String oppgaveoppgaverUrl;
	private final StsRestConsumer stsRestConsumer;
	private final MeterRegistry meterRegistry;

	public OppgaveConsumer(RestTemplateBuilder restTemplateBuilder,
						   StsRestConsumer stsRestConsumer,
						   MeterRegistry meterRegistry,
						   DokoppProperties dokoppProperties) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.oppgaveoppgaverUrl = dokoppProperties.getEndpoints().getOppgave();
		this.stsRestConsumer = stsRestConsumer;
		this.meterRegistry = meterRegistry;
	}

	@Retryable(include = OpprettOppgaveTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public Integer opprettOppgave(OpprettOppgaveRequest opprettOppgaveRequest) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			HttpEntity<OpprettOppgaveRequest> request = new HttpEntity<>(opprettOppgaveRequest, createHeaders());
			OpprettOppgaveResponse oppgaveResponse = restTemplate.postForObject(oppgaveoppgaverUrl, request, OpprettOppgaveResponse.class);
			return oppgaveResponse.getId();
		} catch (HttpClientErrorException e) {
			throw new OpprettOppgaveFunctionalException(String.format("Funksjonell feil ved kall mot Oppgave:opprettOppgave: %s",
					e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new OpprettOppgaveTechnicalException(String.format("Teknisk feil ved kall mot Oppgave:opprettOppgave: %s",
					e.getMessage()), e);
		} finally {
			sample.stop(REQUEST_LATENCY_TIMER_BUILDER.tags(LABEL_PROCESS, SERVICE_ID,
					LABEL_PROCESS_CALLED, "Oppgave::oppgaver:opprettOppgave")
					.register(meterRegistry));
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + stsRestConsumer.getOidcToken());
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(NAV_CALL_ID));
		headers.add(X_CORRELATION_ID, MDC.get(NAV_CALL_ID));
		return headers;
	}
}
