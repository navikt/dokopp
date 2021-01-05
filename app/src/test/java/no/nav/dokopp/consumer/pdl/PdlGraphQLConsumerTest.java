package no.nav.dokopp.consumer.pdl;

import no.nav.dokopp.config.props.PdlProperties;
import no.nav.dokopp.consumer.sts.StsRestConsumer;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrFunctionalException;
import no.nav.dokopp.exception.PdlHentAktoerIdForFnrTechnicalException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PdlGraphQLConsumerTest {

    private static final String CONTENT_TYPE = "application/json";
    private static final String TOKEN = "a1676cbe-daaf-4fe0-aa53-f3a7b35258d0";
    private static final String CALL_ID = "17acff54-7c80-4242-819d-20765d7c883b";

    private static RestTemplate restTemplate = mock(RestTemplate.class);
    private static RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
    private static StsRestConsumer stsRestConsumer = mock(StsRestConsumer.class);

    private static PdlProperties pdlProperties;
    private static PdlGraphQLConsumer pdlGraphQLConsumer;

    @BeforeClass
    public static void setup() {
        MDC.put(NAV_CALL_ID, CALL_ID);

        when(restTemplateBuilder.setConnectTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(stsRestConsumer.getOidcToken()).thenReturn(TOKEN);

        pdlProperties = new PdlProperties();
        pdlProperties.setUrl("http://localhost");
        pdlGraphQLConsumer = new PdlGraphQLConsumer(restTemplateBuilder, stsRestConsumer, pdlProperties);
    }

    @Before
    public void beforeEach() {
        reset(restTemplate);
    }

    @AfterClass
    public static void tearDown(){
        MDC.clear();
    }

    @Test
    public void hentAktoerIdForPersonnummerHappy() throws URISyntaxException {

        PdlHentIdenterResponse pdlHentIdenterResponse = createPdlResponse(
                null,
                createIdent("1000012345678", false, IdentType.AKTORID)
        );

        when(restTemplate.exchange(any(), eq(PdlHentIdenterResponse.class))).thenReturn(new ResponseEntity<>(pdlHentIdenterResponse, HttpStatus.OK));

        String returnValue = pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident("123");

        assertThat(returnValue, is("1000012345678"));
        verify(restTemplate).exchange(
                argThat(new RequestEntityMatcher(
                        "123",
                        CALL_ID,
                        TOKEN,
                        new URI(pdlProperties.getUrl())
                )),
                eq(PdlHentIdenterResponse.class)
        );
    }

    @Test(expected = PdlHentAktoerIdForFnrFunctionalException.class)
    public void shouldThrowFunctionalExceptionIfResponseContainsError() throws URISyntaxException {
        PdlHentIdenterResponse pdlHentIdenterResponse = createPdlResponse(
                List.of(createError("ErrorMessage", "ErrorCode", "ErrorClassification")),
                createIdent("1000012345678", false, IdentType.AKTORID)
        );

        when(restTemplate.exchange(any(), eq(PdlHentIdenterResponse.class))).thenReturn(new ResponseEntity<>(pdlHentIdenterResponse, HttpStatus.OK));

        pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident("123");

        verify(restTemplate).exchange(
                argThat(new RequestEntityMatcher(
                        "123",
                        CALL_ID,
                        TOKEN,
                        new URI(pdlProperties.getUrl())
                )),
                eq(PdlHentIdenterResponse.class)
        );
    }

    @Test(expected = PdlHentAktoerIdForFnrFunctionalException.class)
    public void shouldThrowFunctionalExceptionIfResponseHas4xxStatusCode() throws URISyntaxException {
        PdlHentIdenterResponse pdlHentIdenterResponse = createPdlResponse(
                null,
                createIdent("1000012345678", false, IdentType.AKTORID)
        );

        when(restTemplate.exchange(any(), eq(PdlHentIdenterResponse.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident("123");

        verify(restTemplate).exchange(
                argThat(new RequestEntityMatcher(
                        "123",
                        CALL_ID,
                        TOKEN,
                        new URI(pdlProperties.getUrl())
                )),
                eq(PdlHentIdenterResponse.class)
        );
    }

    @Test(expected = PdlHentAktoerIdForFnrTechnicalException.class)
    public void shouldThrowTechnicalExceptionIfResponseHas5xxStatusCode() throws URISyntaxException {
        PdlHentIdenterResponse pdlHentIdenterResponse = createPdlResponse(
                null,
                createIdent("1000012345678", false, IdentType.AKTORID)
        );

        when(restTemplate.exchange(any(), eq(PdlHentIdenterResponse.class))).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident("123");

        verify(restTemplate).exchange(
                argThat(new RequestEntityMatcher(
                        "123",
                        CALL_ID,
                        TOKEN,
                        new URI(pdlProperties.getUrl())
                )),
                eq(PdlHentIdenterResponse.class)
        );
    }

    @Test(expected = PdlHentAktoerIdForFnrFunctionalException.class)
    public void shouldThrowFunctionalExceptionIfResponseContainsNoCurrentAktorIds() throws URISyntaxException {
        PdlHentIdenterResponse pdlHentIdenterResponse = createPdlResponse(
                null,
                createIdent("1000012345678", true, IdentType.AKTORID),
                createIdent("1000012345678", false, IdentType.NPID),
                createIdent("1000012345678", false, IdentType.FOLKEREGISTERIDENT)
        );

        when(restTemplate.exchange(any(), eq(PdlHentIdenterResponse.class))).thenReturn(new ResponseEntity<>(pdlHentIdenterResponse, HttpStatus.OK));

        pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident("123");

        verify(restTemplate).exchange(
                argThat(new RequestEntityMatcher(
                        "123",
                        CALL_ID,
                        TOKEN,
                        new URI(pdlProperties.getUrl())
                )),
                eq(PdlHentIdenterResponse.class)
        );
    }

    private PdlHentIdenterResponse.PdlIdentTo createIdent(String ident, boolean historisk, IdentType identType) {
        PdlHentIdenterResponse.PdlIdentTo pdlIdentTo = new PdlHentIdenterResponse.PdlIdentTo();
        pdlIdentTo.setIdent(ident);
        pdlIdentTo.setHistorisk(historisk);
        pdlIdentTo.setGruppe(identType);

        return pdlIdentTo;
    }

    private PdlHentIdenterResponse.PdlErrorTo createError(String message, String code, String classification) {

        PdlHentIdenterResponse.PdlErrorTo pdlErrorTo = new PdlHentIdenterResponse.PdlErrorTo();
        PdlHentIdenterResponse.PdlErrorExtensionTo pdlErrorExtensionTo = new PdlHentIdenterResponse.PdlErrorExtensionTo();

        pdlErrorExtensionTo.setCode(code);
        pdlErrorExtensionTo.setClassification(classification);

        pdlErrorTo.setMessage(message);
        pdlErrorTo.setExtensions(pdlErrorExtensionTo);

        return pdlErrorTo;
    }

    private PdlHentIdenterResponse createPdlResponse(List<PdlHentIdenterResponse.PdlErrorTo> errors, PdlHentIdenterResponse.PdlIdentTo ...identTos) {

        PdlHentIdenterResponse pdlHentIdenterResponse = new PdlHentIdenterResponse();
        PdlHentIdenterResponse.PdlHentIdenterData pdlHentIdenterData = new PdlHentIdenterResponse.PdlHentIdenterData();
        PdlHentIdenterResponse.PdlIdenter pdlIdenter = new PdlHentIdenterResponse.PdlIdenter();

        pdlIdenter.setIdenter(List.of(identTos));
        pdlHentIdenterData.setHentIdenter(pdlIdenter);
        pdlHentIdenterResponse.setData(pdlHentIdenterData);
        pdlHentIdenterResponse.setErrors(errors);

        return pdlHentIdenterResponse;
    }
}
