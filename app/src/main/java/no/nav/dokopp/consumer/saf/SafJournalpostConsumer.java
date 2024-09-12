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
import java.util.function.Consumer;

import static ch.qos.logback.core.recovery.RecoveryCoordinator.BACKOFF_MULTIPLIER;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALLID;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokopp.consumer.azure.AzureProperties.CLIENT_REGISTRATION_SAF;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class SafJournalpostConsumer {
	private final String JP_NOT_FOUND_ERROR_CODE = "not_found";
	private final String FORBIDDEN_ERROR_CODE = "forbidden";
	private final String BAD_REQUEST_ERROR_CODE = "bad_request";
	private final String SERVER_ERROR_CODE = "server_error";

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
				.doOnError(handleSafErrors(journalpostId))
				.block();
		List<SafResponse.SafError> errors = safResponse == null ? null : safResponse.getErrors();
		if (safResponse == null) {
			throw new SafJournalpostQueryTechnicalException("Kall til saf feilet. data feltet er null");
		}
		SafResponse.SafHentJournalpost safResponseData = safResponse.getData();
		return (errors == null || errors.isEmpty()) ? SafJournalpostMapper.map(safResponseData.getJournalpost(), journalpostId) : handleSafError(errors, journalpostId);

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
		if (SERVER_ERROR_CODE.equals(errorCode)) {
			throw new SafJournalpostQueryTechnicalException(format("Teknisk feil ved kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		} else if (JP_NOT_FOUND_ERROR_CODE.equals(errorCode)) {
			throw new SafFunctionalException(format("Fant ingen journalpost med journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		} else if (FORBIDDEN_ERROR_CODE.equals(errorCode)) {
			throw new SafFunctionalException(format("Forbidden i kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		} else if (BAD_REQUEST_ERROR_CODE.equals(errorCode)) {
			throw new SafFunctionalException(format("Bad request mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		} else {
			throw new SafFunctionalException(format("Ukjent feil:%s i kall mot SAF for journalpostId=%s, feilmelding:%s", errorCode, journalpostId, errorMsg));
		}
	}

	private final String JOURNALPOST_QUERY = """
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

	private Consumer<Throwable> handleSafErrors(String journalpostId) {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new SafFunctionalException(format("Henting av journalpost=%s feilet med status: %s, feilmelding: %s", journalpostId,
						((WebClientResponseException) error).getStatusCode(), error.getMessage()));
			} else if (error instanceof WebClientResponseException response && response.getStatusCode().is5xxServerError()) {
				throw new SafJournalpostQueryTechnicalException(format("Kall mot SAF feilet for journalpost=%s med status: %s, feilmelding: %s", journalpostId,
						((WebClientResponseException) error).getStatusCode(), error.getMessage()), error);
			}
		};
	}
}
