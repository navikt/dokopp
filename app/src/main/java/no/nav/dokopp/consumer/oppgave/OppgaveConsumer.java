package no.nav.dokopp.consumer.oppgave;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.exception.OpprettOppgaveFunctionalException;
import no.nav.dokopp.exception.OpprettOppgaveTechnicalException;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static no.nav.dokopp.constants.DomainConstants.APP_NAME;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CONSUMER_ID;
import static no.nav.dokopp.constants.HeaderConstants.X_CORRELATION_ID;
import static no.nav.dokopp.constants.RetryConstants.DELAY_SHORT;
import static no.nav.dokopp.consumer.azure.AzureProperties.CLIENT_REGISTRATION_OPPGAVE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
@Slf4j
public class OppgaveConsumer implements Oppgave {

	private final WebClient webClient;

	public OppgaveConsumer(WebClient webClient,
						   DokoppProperties dokoppProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getOppgave().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.add(NAV_CONSUMER_ID, APP_NAME);
				})
				.build();
	}

	@Override
	@Retryable(retryFor = OpprettOppgaveTechnicalException.class, backoff = @Backoff(delay = DELAY_SHORT))
	public Integer opprettOppgave(OpprettOppgaveRequest opprettOppgaveRequest) {
		return webClient.post()
				.uri("/api/v1/oppgaver")
				.headers(httpHeaders -> {
					httpHeaders.add(NAV_CALL_ID, MDC.get(NAV_CALL_ID));
					httpHeaders.add(X_CORRELATION_ID, MDC.get(NAV_CALL_ID));
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_OPPGAVE))
				.bodyValue(opprettOppgaveRequest)
				.retrieve()
				.bodyToMono(OpprettOppgaveResponse.class)
				.onErrorMap(this::mapOpprettOppgaveError)
				.mapNotNull(OpprettOppgaveResponse::getId)
				.block();
	}

	private Throwable mapOpprettOppgaveError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			return new OpprettOppgaveFunctionalException(String.format("Funksjonell feil ved kall mot Oppgave:opprettOppgave: %s",
					error.getMessage()), error);
		}
		return new OpprettOppgaveTechnicalException(String.format("Teknisk feil ved kall mot Oppgave:opprettOppgave: %s",
				error.getMessage()), error);
	}
}
