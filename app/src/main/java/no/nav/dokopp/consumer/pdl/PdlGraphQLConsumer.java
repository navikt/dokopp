package no.nav.dokopp.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrFunctionalException;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrTechnicalException;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.consumer.azure.AzureProperties.CLIENT_REGISTRATION_PDL;
import static no.nav.dokopp.util.MDCOperations.MDC_CALL_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";

	private final WebClient webClient;

	public PdlGraphQLConsumer(WebClient webClient,
							  DokoppProperties dokoppProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getPdl().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.set(NAV_CALL_ID, MDC.get(MDC_CALL_ID));
				})
				.build();
	}

	@Retryable(retryFor = HttpServerErrorException.class)
	public String hentAktoerIdForFolkeregisterident(final String personnummer) {
		return webClient.post()
				.uri("/graphql")
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PDL))
				.bodyValue(mapRequest(personnummer))
				.retrieve()
				.bodyToMono(PdlHentIdenterResponse.class)
				.doOnError(handlePdlErrors())
				.mapNotNull(this::getAktorIdFromResponse)
				.block();
	}

	private String getAktorIdFromResponse(PdlHentIdenterResponse pdlHentIdenterResponse) {
		if (pdlHentIdenterResponse.getErrors() == null || pdlHentIdenterResponse.getErrors().isEmpty()) {
			return Optional.ofNullable(pdlHentIdenterResponse.getData())
					.map(PdlHentIdenterResponse.PdlHentIdenterData::getHentIdenter)
					.map(PdlHentIdenterResponse.PdlIdenter::getIdenter)
					.flatMap(identer -> identer.stream()
							.filter(it -> it.getGruppe() == IdentType.AKTORID)
							.filter(it -> !it.isHistorisk())
							.map(PdlHentIdenterResponse.PdlIdentTo::getIdent)
							.findFirst()).orElseThrow(() -> new PdlHentAktoerIdForFnrFunctionalException("Kunne ikke hente aktørid ident fra PDL. Respons fra PDL inneholdt ikke gjeldende aktørid"));
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlHentIdenterResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PdlHentAktoerIdForFnrFunctionalException("Fant ikke person i Persondataløsningen (PDL).");
			}
			throw new PdlHentAktoerIdForFnrFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlHentIdenterResponse.getErrors());
		}


	}

	private PdlRequest mapRequest(final String aktoerId) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", aktoerId);
		return PdlRequest.builder()
				.query("""
						query($ident: ID!) {
						  hentIdenter(ident: $ident, historikk: false, grupper: AKTORID) {
						    identer {
						      ident
						      historisk
						      gruppe
						    }
						  }
						}
						""")
				.variables(variables)
				.build();
	}

	private Consumer<Throwable> handlePdlErrors() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new PdlHentAktoerIdForFnrFunctionalException("Kall mot pdl feilet funksjonelt.", error);
			}
		};
	}
}