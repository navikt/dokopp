package no.nav.dokopp.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.consumer.nais.NaisTexasRequestInterceptor;
import no.nav.dokopp.consumer.saf.exceptions.SafFunctionalException;
import no.nav.dokopp.consumer.saf.exceptions.SafJournalpostQueryTechnicalException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.dokopp.qopp001.JournalpostResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.Collections.singletonMap;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokopp.consumer.nais.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.map;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class SafJournalpostConsumer {
	private static final String SERVICE_NAME = "saf";
	private static final String JP_NOT_FOUND_ERROR_CODE = "not_found";
	private static final String FORBIDDEN_ERROR_CODE = "forbidden";
	private static final String BAD_REQUEST_ERROR_CODE = "bad_request";
	private static final String SERVER_ERROR_CODE = "server_error";

	private final RestClient restClient;
	private final String targetScope;

	public SafJournalpostConsumer(RestClient restClientTexas,
								  DokoppProperties dokoppProperties) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getSaf().getUrl())
				.defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokoppProperties.getEndpoints().getSaf().getScope();
	}

	@Retryable(retryFor = SafJournalpostQueryTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = 2))
	public JournalpostResponse hentJournalpost(String journalpostId) {
		SafResponse safResponse = restClient.post()
				.uri("/graphql")
				.attribute(TARGET_SCOPE, targetScope)
				.body(createHentJournalpostRequest(journalpostId))
				.retrieve()
				.body(SafResponse.class);

		if (safResponse == null) {
			throw new SafJournalpostQueryTechnicalException("Kall til saf feilet. data feltet er null");
		}

		var errors = safResponse.getErrors();
		if (errors != null && !errors.isEmpty()) {
			return handleSafError(errors, journalpostId);
		}

		return map(safResponse.getData().getJournalpost(), journalpostId);
	}

	private SafRequest createHentJournalpostRequest(String journalpostId) {
		try {
			Long.parseLong(journalpostId);
		} catch (NumberFormatException e) {
			throw new UgyldigInputverdiException("journalpostId er ikke et tall", e);
		}

		return SafRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", journalpostId))
				.build();
	}

	private JournalpostResponse handleSafError(List<SafResponse.SafError> errors, String journalpostId) {
		String errorCode = errors.getFirst().getExtensions().getCode();
		String errorMsg = errors.getFirst().getMessage();
		switch (errorCode) {
			case SERVER_ERROR_CODE ->
					throw new SafJournalpostQueryTechnicalException("Teknisk feil ved kall mot SAF for journalpostId=%s, feilmelding:%s"
							.formatted(journalpostId, errorMsg));
			case JP_NOT_FOUND_ERROR_CODE ->
					throw new SafFunctionalException("Fant ingen journalpost med journalpostId=%s, feilmelding:%s"
							.formatted(journalpostId, errorMsg));
			case FORBIDDEN_ERROR_CODE ->
					throw new SafFunctionalException("Forbidden i kall mot SAF for journalpostId=%s, feilmelding:%s"
							.formatted(journalpostId, errorMsg));
			case BAD_REQUEST_ERROR_CODE ->
					throw new SafFunctionalException("Bad request mot SAF for journalpostId=%s, feilmelding:%s"
							.formatted(journalpostId, errorMsg));
			case null, default ->
					throw new SafFunctionalException("Ukjent feil:%s i kall mot SAF for journalpostId=%s, feilmelding:%s"
							.formatted(errorCode, journalpostId, errorMsg));
		}
	}

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
				journalpost(journalpostId: $queryJournalpostId) {
					journalfoerendeEnhet
					tema
					bruker {
						id
						type
					}
					avsenderMottaker {
						id
						type
					}
					sak{
						arkivsaksnummer
						arkivsaksystem
					}
					antallRetur
					skjerming
					dokumenter {
						skjerming
						dokumentvarianter {
							variantformat
							skjerming
						}
					}
				}
			}
			""";

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		String feilmelding = "Kall mot %s feilet %s med status=%s, body=%s"
				.formatted(SERVICE_NAME,
						response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
						response.getStatusCode(), body);
		if (response.getStatusCode().is4xxClientError()) {
			throw new SafFunctionalException(feilmelding);
		}
		throw new SafJournalpostQueryTechnicalException(feilmelding);
	}
}
