package no.nav.dokopp.qopp001.itest;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import jakarta.jms.Queue;
import jakarta.jms.TextMessage;
import jakarta.xml.bind.JAXBElement;
import no.nav.dokopp.Application;
import no.nav.dokopp.qopp001.Qopp001Service;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.removeAllMappings;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.dokopp.config.cache.LokalCacheConfig.STS_CACHE;
import static no.nav.dokopp.util.MDCOperations.MDC_CALL_ID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.annotation.DirtiesContext.MethodMode.BEFORE_METHOD;

@Import({JmsTestConfig.class, ApplicationItestConfig.class})
@SpringBootTest(classes = {Application.class}, webEnvironment = RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qopp001IT {

	private static final String CALLID = "itest-callId";

	private static final String JOURNALPOST_ID = "123456";
	private static final String ENHETS_ID = "9999";
	private static final String JOURNALF_ENHET_ID = "2990";
	private static final String SAKS_REFERANSE = "1";
	private static final String AKTOER_ID = "1000012345678";
	private static final String ORGNR = "123456789";
	public static final String SCENARIO_NEDLAGT_ENHET = "Nedlagt enhet";

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Queue qopp001;

	@Autowired
	private Queue qopp001FunksjonellFeil;

	@Autowired
	private Queue backoutQueue;

	@Autowired
	private CacheManager cacheManager;

	@BeforeAll
	public static void beforeClass() {
		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}

	@BeforeEach
	public void setupBefore() {
		resetAllRequests();
		Cache stsCache = cacheManager.getCache(STS_CACHE);
		if (stsCache != null) {
			stsCache.clear();
		}
	}

	/**
	 * HVIS kall er gjort mot SAF SÅ SKAL input til tjenesten sendes som angitt i behandlingssteg
	 * HVIS kall mot TJOARK110 går ok SÅ skal input og output behandles som angitt i behandlingssteg
	 * HVIS kall mot BehandleOppgave_v1 er ok SÅ skal input og output behandles som angitt i behandlingssteg
	 */
	@ParameterizedTest
	@CsvSource({"qopp001_happy_returpost.xml","qopp001_happy_manglende_adresse.xml"})
	public void shouldOppretteOppgaveGosys(String inputFilename) throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("oppgaver/opprett_oppgave_happy.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/"+inputFilename), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			// vent på siste API-kall før videre verifisering
			verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon")));
		});

		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/pdl/graphql")));
		verify(postRequestedFor(urlEqualTo("/oppgaver"))
				.withRequestBody(matchingJsonPath("$[?(@.opprettetAvEnhetsnr == '" + ENHETS_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.tildeltEnhetsnr == '" + JOURNALF_ENHET_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.saksreferanse == '" + SAKS_REFERANSE + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.aktoerId == '" + AKTOER_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.orgnr == null)]")));
		verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon"))
				.withRequestBody(matchingXPath("//journalpostIdListe/text()", equalTo(JOURNALPOST_ID))));
	}

	@Test
	public void shouldOppretteOppgaveGosysMedAvsenderMottaker() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happyAvsenderMottaker.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("oppgaver/opprett_oppgave_happy.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			// vent på siste API-kall før videre verifisering
			verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon")));
		});

		verify(1, getRequestedFor(urlEqualTo("/securitytoken?grant_type=client_credentials&scope=openid")));
		verify(1, postRequestedFor(urlEqualTo("/pdl/graphql")));
		verify(postRequestedFor(urlEqualTo("/oppgaver"))
				.withRequestBody(matchingJsonPath("$[?(@.opprettetAvEnhetsnr == '" + ENHETS_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.tildeltEnhetsnr == '" + JOURNALF_ENHET_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.saksreferanse == '" + SAKS_REFERANSE + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.aktoerId == '" + AKTOER_ID + "')]"))
				.withRequestBody(matchingJsonPath("$[?(@.orgnr == null)]")));
		verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon"))
				.withRequestBody(matchingXPath("//journalpostIdListe/text()", equalTo(JOURNALPOST_ID))));
	}

	@Test
	public void shouldNotOppretteOppgaveWithSaksreferanseWhenFagomradeNotGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-pensjon.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("oppgaver/opprett_oppgave_pensjon.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> verify(postRequestedFor(urlEqualTo("/oppgaver"))
				.withRequestBody(matchingJsonPath("$[?(@.saksreferanse == null)]"))));
	}

	@Test
	public void shouldOppretteOppgaveWithOrgnrGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-organisasjon.json")));
		stubSecurityTokens();
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("oppgaver/opprett_oppgave_organisasjon.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> verify(postRequestedFor(urlEqualTo("/oppgaver"))
				.withRequestBody(matchingJsonPath("$[?(@.aktoerId == null)]"))
				.withRequestBody(matchingJsonPath("$[?(@.orgnr == '" + ORGNR + "')]"))));
	}

	@Test
	public void shouldNotOppretteOppgaveWithFagomraadeSTO() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-fagomraade_STO.json")));
		stubSecurityTokens();
		Logger fooLogger = (Logger) LoggerFactory.getLogger(Qopp001Service.class);
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		fooLogger.addAppender(listAppender);
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			assertThat(listAppender.list.size(), is(3));
			assertThat(listAppender.list.get(0).getFormattedMessage(), is("qopp001 har hentet journalpostInfo fra Joark for returpost med journalpostId=123456."));
			assertThat(listAppender.list.get(1).getFormattedMessage(), is("qopp001 lager ikke oppgave i Gosys for journalpostId=123456 da den er returpost fra fagområde=STO og ikke vil bli behandlet."));
			assertThat(listAppender.list.get(2).getFormattedMessage(), is("qopp001 har flagget journalpost med journalpostId=123456 som returpost."));
			verify(exactly(0), postRequestedFor(urlEqualTo("/oppgaver")));
		});
	}

	@Test
	void shouldOppretteOppgaveWithTildeltEnhetsNummerNullWhenOpprettOppgaveFails() throws IOException {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver")
				.inScenario(SCENARIO_NEDLAGT_ENHET)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse()
						.withStatus(HttpStatus.BAD_REQUEST.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("oppgaver/opprett_oppgave_nedlagt_enhet.json"))
				.willSetStateTo("Nytt forsøk med tildeltEnhetsnummer=null"));
		stubFor(post("/oppgaver")
				.inScenario(SCENARIO_NEDLAGT_ENHET)
				.whenScenarioStateIs("Nytt forsøk med tildeltEnhetsnummer=null")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("oppgaver/opprett_oppgave_happy.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			// vent på siste API-kall før videre verifisering
			verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon")));
		});

		verify(2, postRequestedFor(urlEqualTo("/oppgaver")));
		verify(postRequestedFor(urlEqualTo("/oppgaver"))
				.withRequestBody(matchingJsonPath("$[?(@.tildeltEnhetsnr == null)]")));
	}

	@Test
	void shouldDiscardMessageAndNotOppretteOppgaveWithTildeltEnhetsNummerNullWhenOpprettOppgaveFailsAndEnhet9999() throws IOException {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy_maskin.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver")
				.willReturn(aResponse()
						.withStatus(HttpStatus.BAD_REQUEST.value())
						.withBodyFile("oppgaver/opprett_oppgave_nedlagt_enhet.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		assertThrows(ConditionTimeoutException.class, () ->
				await().during(2, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					// Om alt fungerer som forventet blokker den over fordi køen er tom, og vi når aldri den under
					assertThat("Denne skal være null, men vi asserter motsatt sånn at den ytre asserten skal feile om det blir feil", response, notNullValue());
				}), "await skal få timeout pga. blocking read på det som skal være en tom kø (qdok001_funk_feil)");
		verify(1, postRequestedFor(urlEqualTo("/oppgaver")));
	}

	@Test
	public void shouldThrowUkjentBrukertypeException() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-ukjent.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en feil
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowValideringFeiletException() throws Exception {
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_valideringFeiler.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_valideringFeiler.xml")));
				});
	}

	/**
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 * HVIS det oppstår en funksjonell feil i SAF SÅ avslutt og returner feilmelding
	 */
	@Test
	public void shouldThrowJournalpostIkkeFunnetException() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-journalpostIkkeFunnet.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	@Test
	public void shouldThrowUkjentBrukertypeExceptionWhenNoBrukerAndAvsenderMottaker() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-ingenBruker.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	/**
	 * HVIS SAF ikke er tilgjengelig SÅ prøv igjen før avslutt
	 */
	@Test
	public void shouldThrowTechnicalExceptionSAF() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-internalServerError.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
		verify(exactly(3), postRequestedFor(urlEqualTo("/saf")));
	}

	/**
	 * HVIS TJOARK110 ikke er tilgjengelig SÅ prøv igjen før avslutt
	 */
	@Test
	public void shouldThrowTechnicalExceptionTjoark110() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_internalServerError.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("oppgaver/opprett_oppgave_happy.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	/**
	 * HVIS tjeneste opprettOppgave gir teknisk feil SÅ avslutt og returner feil
	 */
	@Test
	public void shouldThrowTechnicalExceptionOpprettOppgave() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	/**
	 * HVIS bruker ikke er autorisert for operasjonen -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowSikkerhetsbegrensningExceptionGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-happy.json")));
		stubFor(post("/oppgaver").willReturn(aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowUgyldigInputverdiExceptionIllegalOppgavetype() throws Exception {
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_illegalOppgavetype.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_illegalOppgavetype.xml")));
				});
	}

	@Test
	public void shouldThrowReturpostAlleredeFlaggetExceptionWhenAntallReturpostReturnedFromSaf() throws Exception {
		resetAllRequests();
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-returpostFlagget.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> verify(exactly(0), postRequestedFor(urlEqualTo("/oppgaver")))
		);
		verify(exactly(0), postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon")));
	}

	@Test
	public void shouldThrowAktoerHentAktoerIdForFnrFunctionalException() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(
				aResponse().withStatus(HttpStatus.FORBIDDEN.value())));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(qopp001FunksjonellFeil);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	@Test
	public void shouldThrowAktoerHentAktoerIdForFnrTechnicalException() throws Exception {
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(
				aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	@Test
	//Det kan virke som Azure-clienten cacher azure-tokenet slik at vi aldri får internal server error fra azure uten dirties context.
	@DirtiesContext(methodMode = BEFORE_METHOD)
	public void shouldThrowAzureTechnicalException() throws Exception {
		stubFor(post("/azure_token").willReturn(
				aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happy.json")));
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
				});
	}

	@Test
	public void shouldNotOppretteOppgaveWhenPdlBadRequest() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happyAvsenderMottaker.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-bad-request.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			String response = receive(qopp001FunksjonellFeil);
			assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
		});

	}

	@Test
	public void shouldNotOppretteOppgaveWhenPdlIngenIdenter() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post(urlMatching("/saf"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("saf/safGraphQlResponse-happyAvsenderMottaker.json")));
		stubSecurityTokens();
		stubFor(post("/pdl/graphql").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("pdl/pdl-ingen-identer.json")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy_returpost.xml"), CALLID);

		await().atMost(10, SECONDS).untilAsserted(() -> {
			// vent på siste API-kall før videre verifisering
			String response = receive(qopp001FunksjonellFeil);
			assertThat(response, is(classpathToString("qopp001/qopp001_happy_returpost.xml")));
		});

	}

	private void stubSecurityTokens() {
		stubFor(get("/securitytoken?grant_type=client_credentials&scope=openid").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
				.withBodyFile("securitytoken/stsResponse-happy.json")));
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response_dummy.json")));
	}

	private void sendStringMessage(Queue queue, final String message, String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = session.createTextMessage();
			msg.setText(message);
			msg.setStringProperty(MDC_CALL_ID, callId);
			return msg;
		});
	}

	private String classpathToString(String classpathResource) throws IOException {
		InputStream inputStream = new ClassPathResource(classpathResource).getInputStream();
		String message = IOUtils.toString(inputStream, UTF_8);
		IOUtils.closeQuietly(inputStream);
		return message;
	}

	@SuppressWarnings("unchecked")
	private <T> T receive(Queue queue) {
		Object response = jmsTemplate.receiveAndConvert(queue);
		if (response instanceof JAXBElement) {
			response = ((JAXBElement) response).getValue();
		}
		return (T) response;
	}

}
