package no.nav.dokopp.consumer.pdl;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.config.props.PdlProperties;
import no.nav.dokopp.consumer.sts.StsRestConsumer;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrFunctionalException;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrTechnicalException;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;

/**
 * https://navikt.github.io/pdl
 *
 * @author Joakim Bjørnstad, Jbit AS
 * @author Erlend Axelsson, NAV IT
 */
@Slf4j
@Component
public class PdlGraphQLConsumer {
    private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
    private final RestTemplate restTemplate;
    private final StsRestConsumer stsConsumer;
    private final String pdlUrl;

    public PdlGraphQLConsumer(RestTemplateBuilder restTemplateBuilder,
                              StsRestConsumer stsConsumer,
                              PdlProperties pdlProperties) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(20))
                .build();
        this.stsConsumer = stsConsumer;
        this.pdlUrl = pdlProperties.getUrl();
    }

    @Retryable(include = HttpServerErrorException.class)
    public String hentAktoerIdForFolkeregisterident(final String personnummer) {
        try {
            final UriComponents uri = UriComponentsBuilder.fromHttpUrl(pdlUrl).build();
            final String serviceuserToken = "Bearer " + stsConsumer.getOidcToken();
            final RequestEntity<PdlRequest> requestEntity = RequestEntity.post(uri.toUri())
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.AUTHORIZATION, serviceuserToken)
                    .header(HEADER_PDL_NAV_CONSUMER_TOKEN, serviceuserToken)
                    .header(NAV_CALL_ID, MDC.get(NAV_CALL_ID))
                    .body(mapRequest(personnummer));
            final PdlHentIdenterResponse pdlHentIdenterResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlHentIdenterResponse.class).getBody());
            if (pdlHentIdenterResponse.getErrors() == null || pdlHentIdenterResponse.getErrors().isEmpty()) {
                return getAktorIdFromResponse(pdlHentIdenterResponse);
            } else {
                throw new PdlHentAktoerIdForFnrFunctionalException("Kunne ikke hente aktørid ident fra PDL." + pdlHentIdenterResponse.getErrors());
            }
        } catch (HttpClientErrorException e) {
            throw new PdlHentAktoerIdForFnrFunctionalException("Funksjonell feil ved kall mot PDL.", e);
        } catch (HttpServerErrorException e) {
            throw new PdlHentAktoerIdForFnrTechnicalException("Teknisk feil ved kall mot PDL.", e);
        }
    }

    private String getAktorIdFromResponse(PdlHentIdenterResponse pdlHentIdenterResponse){
        return Optional.ofNullable(pdlHentIdenterResponse.getData())
                .map(PdlHentIdenterResponse.PdlHentIdenterData::getHentIdenter)
                .map(PdlHentIdenterResponse.PdlIdenter::getIdenter)
                .flatMap(identer -> identer.stream()
                        .filter(it -> it.getGruppe() == IdentType.AKTORID)
                        .filter(it -> !it.isHistorisk())
                        .map(PdlHentIdenterResponse.PdlIdentTo::getIdent)
                        .findFirst())
                .orElseThrow(()-> {
                    throw new PdlHentAktoerIdForFnrFunctionalException("Kunne ikke hente aktørid ident fra PDL. Respons fra PDL inneholdt ikke gjeldende aktørid");
                });
    }

    private PdlRequest mapRequest(final String aktoerId) {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("ident", aktoerId);
        return PdlRequest.builder()
                .query("query($ident: ID!) {\n" +
                        "  hentIdenter(ident: $ident, historikk: false, grupper: AKTORID) {\n" +
                        "    identer {\n" +
                        "      ident\n" +
                        "      historisk\n" +
                        "      gruppe\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n")
                .variables(variables)
                .build();
    }
}