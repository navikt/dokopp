package no.nav.dokopp.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.consumer.saf.exceptions.SafFunctionalException;
import no.nav.dokopp.consumer.saf.exceptions.SafJournalpostQueryTechnicalException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.dokopp.qopp001.JournalpostResponse;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

import static ch.qos.logback.core.recovery.RecoveryCoordinator.BACKOFF_MULTIPLIER;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALLID;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokopp.consumer.azure.AzureProperties.CLIENT_REGISTRATION_SAF;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.map;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class SafJournalpostConsumer {
	private static final String JP_NOT_FOUND_ERROR_CODE = "not_found";
	private static final String FORBIDDEN_ERROR_CODE = "forbidden";
	private static final String BAD_REQUEST_ERROR_CODE = "bad_request";
	private static final String SERVER_ERROR_CODE = "server_error";

	private final WebClient webClient;

	public SafJournalpostConsumer(WebClient webClient,
								  DokoppProperties dokoppProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getSaf().getUrl())
				.defaultHeaders(httpHeaders -> httpHeaders.setContentType(APPLICATION_JSON))
				.build();
	}

	@Retryable(retryFor = SafJournalpostQueryTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = BACKOFF_MULTIPLIER))
	public JournalpostResponse hentJournalpost(String journalpostId) {
		SafResponse safResponse = webClient.post()
				.uri("/graphql")
				.headers(httpHeaders -> httpHeaders.add(NAV_CALLID, MDC.get(NAV_CALL_ID)))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_SAF))
				.bodyValue(createHentJournalpostRequest(journalpostId))
				.retrieve()
				.bodyToMono(SafResponse.class)
				.onErrorMap(error -> mapSafError(error, journalpostId))
				.block();

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
					throw new SafJournalpostQueryTechnicalException(format("Teknisk feil ved kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
			case JP_NOT_FOUND_ERROR_CODE ->
					throw new SafFunctionalException(format("Fant ingen journalpost med journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
			case FORBIDDEN_ERROR_CODE ->
					throw new SafFunctionalException(format("Forbidden i kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
			case BAD_REQUEST_ERROR_CODE ->
					throw new SafFunctionalException(format("Bad request mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
			case null, default ->
					throw new SafFunctionalException(format("Ukjent feil:%s i kall mot SAF for journalpostId=%s, feilmelding:%s", errorCode, journalpostId, errorMsg));
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

	private Throwable mapSafError(Throwable error, String journalpostId) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new SafFunctionalException(format("Henting av journalpost=%s feilet med status=%s, feilmelding=%s",
					journalpostId, response.getStatusCode(), error.getMessage())
			);
		}
		return new SafJournalpostQueryTechnicalException(format("Kall mot SAF feilet teknisk for journalpost=%s", journalpostId), error);
	}
}
