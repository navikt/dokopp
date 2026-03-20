package no.nav.dokopp.consumer.oppgave;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.constants.DomainConstants;
import no.nav.dokopp.consumer.nais.NaisTexasRequestInterceptor;
import no.nav.dokopp.exception.OpprettOppgaveFunctionalException;
import no.nav.dokopp.exception.OpprettOppgaveTechnicalException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static no.nav.dokopp.constants.DomainConstants.APP_NAME;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CONSUMER_ID;
import static no.nav.dokopp.constants.HeaderConstants.X_CORRELATION_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Slf4j
public class OppgaveConsumer implements Oppgave {

	private static final String SERVICE_NAME = "oppgave";

	private final RestClient restClient;
	private final String targetScope;

	public OppgaveConsumer(RestClient restClientTexas,
						   DokoppProperties dokoppProperties) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getOppgave().getUrl())
				.defaultHeaders(headers -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(NAV_CONSUMER_ID, APP_NAME);
				})
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokoppProperties.getEndpoints().getOppgave().getScope();
	}

	@Override
	@Retryable(retryFor = OpprettOppgaveTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public Integer opprettOppgave(OpprettOppgaveRequest opprettOppgaveRequest) {
		OpprettOppgaveResponse response = restClient.post()
				.uri("/api/v1/oppgaver")
				.attribute(NaisTexasRequestInterceptor.TARGET_SCOPE, targetScope)
				.body(opprettOppgaveRequest)
				.retrieve()
				.body(OpprettOppgaveResponse.class);

		return response != null ? response.getId() : null;
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		String feilmelding = "Kall mot %s feilet %s med status=%s, body=%s"
				.formatted(SERVICE_NAME,
						response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
						response.getStatusCode(), body);
		if (response.getStatusCode().is4xxClientError()) {
			throw new OpprettOppgaveFunctionalException(feilmelding);
		}
		throw new OpprettOppgaveTechnicalException(feilmelding);
	}
}
