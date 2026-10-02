package no.nav.dokopp.consumer.dokarkiv;

import no.nav.dokopp.config.DokoppProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.LocalDate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDate.now;
import static no.nav.dokopp.consumer.nais.NaisTexasRequestInterceptor.TARGET_SCOPE;

@Component
public class DokarkivConsumer {

	private final RestClient restClient;
	private final String targetScope;

	public DokarkivConsumer(RestClient restClientTexas,
	                        DokoppProperties dokoppProperties) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getDokarkiv().getUrl())
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokoppProperties.getEndpoints().getDokarkiv().getScope();
	}

	@Retryable(includes = {DokarkivTechnicalException.class, ResourceAccessException.class})
	public void oppdaterJournalpost(String journalpostId) {
		LocalDate datoRetur = now();
		OppdaterJournalpostRequest request = new OppdaterJournalpostRequest(datoRetur);

		restClient.put()
				.uri("/rest/journalpostapi/v1/journalpost/{journalpostId}", journalpostId)
				.attribute(TARGET_SCOPE, targetScope)
				.body(request)
				.retrieve()
				.toBodilessEntity();
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), UTF_8);

		if (response.getStatusCode().is4xxClientError()) {
			throw new DokarkivFunctionalException("Kall mot dokarkiv feilet funksjonelt med status=%s, body=%s".formatted(response.getStatusCode(), body));
		}
		throw new DokarkivTechnicalException("Kall mot dokarkiv feilet funksjonelt med status=%s, body=%s".formatted(response.getStatusCode(), body));
	}

}