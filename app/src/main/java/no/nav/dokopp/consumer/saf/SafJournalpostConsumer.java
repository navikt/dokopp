package no.nav.dokopp.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.consumer.azure.AzureTokenConsumer;
import no.nav.dokopp.consumer.saf.exceptions.SafFunctionalException;
import no.nav.dokopp.consumer.saf.exceptions.SafJournalpostQueryTechnicalException;
import no.nav.dokopp.consumer.saf.exceptions.SafJournalpostUnauthorizedException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static ch.qos.logback.core.recovery.RecoveryCoordinator.BACKOFF_MULTIPLIER;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;

@Slf4j
@Component
public class SafJournalpostConsumer {
	private final RestTemplate restTemplate;
	private final String graphQLurl;
	private final AzureTokenConsumer azureTokenConsumer;

	private final String SERVER_ERROR_CODE = "server_error";
	private final String JP_NOT_FOUND_ERROR_CODE = "not_found";
	private final String FORBIDDEN_ERROR_CODE = "forbidden";
	private final String BAD_REQUEST_ERROR_CODE = "bad_request";

	@Autowired
	public SafJournalpostConsumer(RestTemplateBuilder restTemplateBuilder,
								  @Value("${saf.graphql.url}") String graphQLurl,
								  AzureTokenConsumer azureTokenConsumer) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.graphQLurl = graphQLurl;
		this.azureTokenConsumer = azureTokenConsumer;
	}

	@Retryable(include = SafJournalpostQueryTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT, multiplier = BACKOFF_MULTIPLIER))
	public JournalpostResponse hentJournalpost(String journalpostId) {
		try {
			final SafRequest hentJournalpostRequest = createHentJournalpostRequest(journalpostId);
			final SafResponse safResponse = restTemplate.exchange(graphQLurl, HttpMethod.POST, new HttpEntity<>(hentJournalpostRequest, createAuthorizationHeader()), SafResponse.class).getBody();

			List<SafResponse.SafError> errors = safResponse.getErrors();
			return (errors == null || errors.isEmpty()) ? SafJournalpostMapper.map(safResponse.getData().getJournalpost()) : handleSafError(errors, journalpostId);

		} catch (HttpClientErrorException e) {
			throw new SafJournalpostUnauthorizedException(format("Henting av journalpost feilet med status: %s, feilmelding: %s", e
					.getStatusCode(), e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new SafJournalpostQueryTechnicalException(format("Tjenesten SAF (graphQL) feilet med status: %s, feilmelding: %s", e.getStatusCode(), e.getMessage()), e);
		}
	}

	private HttpHeaders createAuthorizationHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(azureTokenConsumer.getClientCredentialToken().getAccess_token());
		return headers;
	}

	private SafRequest createHentJournalpostRequest(String journalpostId) {
		try{
			Long.parseLong(journalpostId);
		} catch (NumberFormatException e){
			throw new UgyldigInputverdiException("journalpostId er ikke et tall", e);
		}

		SafRequest request = SafRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", journalpostId))
				.build();

		return request;
	}

	private JournalpostResponse handleSafError(List<SafResponse.SafError> errors, String journalpostId){
		String errorCode = errors.get(0).getExtensions().getCode();
		String errorMsg = errors.get(0).getMessage();
		if (SERVER_ERROR_CODE.equals(errorCode)) {
			throw new SafJournalpostQueryTechnicalException(format("Teknisk feil ved kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		}
		else if (JP_NOT_FOUND_ERROR_CODE.equals(errorCode)) {
			throw new SafFunctionalException(format("Fant ingen journalpost med journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		}
		else if(FORBIDDEN_ERROR_CODE.equals(errorCode)) {
			throw new SafFunctionalException(format("Forbidden i kall mot SAF for journalpostId=%s, feilmelding:%s", journalpostId, errorMsg));
		}
		else if(BAD_REQUEST_ERROR_CODE.equals(errorCode)){
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
				}
			}
			""";
}
