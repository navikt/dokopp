package no.nav.dokopp.consumer.aktoerregister;

import static no.nav.dokopp.constants.DomainConstants.APP_NAME;
import static no.nav.dokopp.constants.DomainConstants.BEARER_PREFIX;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CONSUMER_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;

import no.nav.dokopp.consumer.sts.StsRestConsumer;
import no.nav.dokopp.exception.AktoerHentAktoerIdForFnrFunctionalException;
import no.nav.dokopp.exception.AktoerHentAktoerIdForFnrTechnicalException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.Map;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class AktoerregisterConsumer implements Aktoerregister {

	private static final String NAV_PERSONIDENTER = "Nav-Personidenter";

	private final RestTemplate restTemplate;
	private final String aktoerregisterUrl;
	private final StsRestConsumer stsRestConsumer;

	public AktoerregisterConsumer(RestTemplateBuilder restTemplateBuilder,
								  @Value("${aktoerregister.api.v1.url}") String aktoerregisterUrl,
								  StsRestConsumer stsRestConsumer) {
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
		this.aktoerregisterUrl = aktoerregisterUrl;
		this.stsRestConsumer = stsRestConsumer;
	}

	@Retryable(include = AktoerHentAktoerIdForFnrTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public String hentAktoerIdForFnr(String fnr) {
		try {
			final String fnrTrimmed = fnr.trim();
			HttpHeaders headers = createHeaders();
			headers.add(NAV_PERSONIDENTER, fnrTrimmed);
			Map<String, IdentInfoForAktoer> response = restTemplate.exchange(aktoerregisterUrl + "/identer?gjeldende=true&identgruppe=AktoerId",
					HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, IdentInfoForAktoer>>() {
					}).getBody();

			assertResponse(response, fnrTrimmed);
			return response.get(fnrTrimmed).getIdenter().get(0).getIdent();
		} catch (HttpClientErrorException e) {
			throw new AktoerHentAktoerIdForFnrFunctionalException(String.format("Funksjonell feil ved kall mot Aktoerregister:hentAktoerIdForFnr for fnr=%s: %s",
					fnr, e.getMessage()), e);
		} catch (HttpServerErrorException e) {
			throw new AktoerHentAktoerIdForFnrTechnicalException(String.format("Teknisk feil ved kall mot Aktoerregister:hentAktoerIdForFnr for fnr=%s: %s",
					fnr, e.getMessage()), e);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + stsRestConsumer.getOidcToken());
		headers.add(NAV_CONSUMER_ID, APP_NAME);
		headers.add(NAV_CALL_ID, MDC.get(NAV_CALL_ID));
		return headers;
	}

	private void assertResponse(Map<String, IdentInfoForAktoer> response, String aktoerId) {
		assertResponseNotNull(response, aktoerId);
		IdentInfoForAktoer identInfoForAktoer = response.get(aktoerId);
		assertNoFeilmelding(identInfoForAktoer, aktoerId);
		assertIdenter(identInfoForAktoer, aktoerId);
	}

	private void assertResponseNotNull(Map<String, IdentInfoForAktoer> response, String fnr) {
		if (response == null || response.get(fnr) == null) {
			throw new AktoerHentAktoerIdForFnrFunctionalException(String.format("Fikk ingen resons fra Aktoerregister:hentAktoerIdForFnr på fnr=%s.", fnr));
		}
	}

	private void assertNoFeilmelding(IdentInfoForAktoer identInfoForAktoer, String fnr) {
		if (identInfoForAktoer.getFeilmelding() != null) {
			throw new AktoerHentAktoerIdForFnrFunctionalException(String.format("Feil ved respons fra Aktoerregister:hentAktoerIdForFnr på fnr=%s. Feilmelding=%s",
					fnr, identInfoForAktoer.getFeilmelding()));
		}
	}

	private void assertIdenter(IdentInfoForAktoer identInfoForAktoer, String fnr) {
		if (identInfoForAktoer.getIdenter() == null || identInfoForAktoer.getIdenter().size() != 1) {
			throw new AktoerHentAktoerIdForFnrFunctionalException(String.format("Feil ved respons fra Aktoerregister:hentAktoerIdForFnr på fnr=%s. Forventet å få tilbake identliste med ett innslag ved forespørsel om gjeldende aktørId. " +
							"Fikk identliste med %s innslag. Sannsynligvis en feil i aktørregisteret. Aktørregisteret ryddes ved batchjobb hver natt kl 03.00.", fnr,
					identInfoForAktoer.getIdenter() == null ? "null" : identInfoForAktoer.getIdenter().size()));
		}
	}
}
