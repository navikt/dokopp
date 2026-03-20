package no.nav.dokopp.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrFunctionalException;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrTechnicalException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;

import static no.nav.dokopp.consumer.nais.NaisTexasRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PdlGraphQLConsumer {
	private static final String SERVICE_NAME = "pdl";
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestClient restClient;
	private final String targetScope;

	public PdlGraphQLConsumer(RestClient restClientTexas,
							  DokoppProperties dokoppProperties) {
		this.restClient = restClientTexas.mutate()
				.baseUrl(dokoppProperties.getEndpoints().getPdl().getUrl())
				.defaultHeaders(headers -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
				})
				.defaultStatusHandler(HttpStatusCode::isError, (_, res) -> handleError(res))
				.build();
		this.targetScope = dokoppProperties.getEndpoints().getPdl().getScope();
	}

	@Retryable(retryFor = PdlHentAktoerIdForFnrTechnicalException.class)
	public String hentAktoerIdForFolkeregisterident(final String personnummer) {
		PdlHentIdenterResponse pdlHentIdenterResponse = restClient.post()
				.uri("/graphql")
				.attribute(TARGET_SCOPE, targetScope)
				.body(mapRequest(personnummer))
				.retrieve()
				.body(PdlHentIdenterResponse.class);

		return getAktorIdFromResponse(pdlHentIdenterResponse);
	}

	private String getAktorIdFromResponse(PdlHentIdenterResponse pdlHentIdenterResponse) {
		if (CollectionUtils.isEmpty(pdlHentIdenterResponse.getErrors())) {
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

	private void handleError(ClientHttpResponse response) throws IOException {
		String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
		String feilmelding = "Kall mot %s feilet %s med status=%s, body=%s"
				.formatted(SERVICE_NAME,
						response.getStatusCode().is4xxClientError() ? "funksjonelt" : "teknisk",
						response.getStatusCode(), body);
		if (response.getStatusCode().is4xxClientError()) {
			throw new PdlHentAktoerIdForFnrFunctionalException(feilmelding);
		}
		throw new PdlHentAktoerIdForFnrTechnicalException(feilmelding);
	}
}