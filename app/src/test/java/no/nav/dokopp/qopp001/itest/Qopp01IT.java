package no.nav.dokopp.qopp001.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static no.nav.modig.common.MDCOperations.MDC_CALL_ID;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.github.tomakehurst.wiremock.common.Xml;
import no.nav.dokopp.Application;
import no.nav.modig.core.test.FileUtils;
import no.nav.modig.testcertificates.TestCertificates;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(SpringRunner.class)
@Import(JmsTestConfig.class)
@SpringBootTest(classes = {Application.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("itest")
public class Qopp01IT {
	
	private static final String CALLID = "callId";
	
	private static final String JOURNALPOST_ID = "123456";
	private static final String ENHETS_ID = "9999";
	
	@Inject
	private JmsTemplate jmsTemplate;
	
	@Inject
	private Queue qopp001;
	
	@Inject
	private Queue functionalBOQ;
	
	@Inject
	private Queue backoutQueue;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@BeforeClass
	public static void beforeClass() {
		TestCertificates.setupTemporaryTrustStore("no/nav/modig/testcertificates/truststore.jts", "changeit");
		File file = FileUtils.putInTempFile(TestCertificates.class.getClassLoader()
				.getResourceAsStream("no/nav/modig/testcertificates/keystore.jks"));
		System.setProperty("SRVDOKOPP_CERT_KEYSTORE", file.getAbsolutePath());
		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}
	
	/**
	 * HVIS kall er gjort mot TJOARK0122 SÅ SKAL input til tjenesten sendes som angitt i behandlingssteg
	 * HVIS kall mot TJOARK110 går ok SÅ skal input og output behandles som angitt i behandlingssteg
	 * HVIS kall mot BehandleOppgave_v1 er ok SÅ skal input og output behandles som angitt i behandlingssteg
	 */
	@Test
	public void shouldOppretteOppgaveGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		
		Thread.sleep(5000);
		verify(postRequestedFor(urlEqualTo("/dokumentproduksjoninfo"))
				.withRequestBody(matchingXPath("//journalpostId/text()", equalTo(JOURNALPOST_ID))));
		verify(postRequestedFor(urlEqualTo("/behandleoppgave"))
				.withRequestBody(matchingXPath("//opprettetAvEnhetId/text()", equalTo(ENHETS_ID))));
		verify(postRequestedFor(urlEqualTo("/arkiverdokumentproduksjon"))
				.withRequestBody(matchingXPath("//journalpostIdListe/text()", equalTo(JOURNALPOST_ID))));
	}
	
	@Test
	public void shouldNotOppretteOppgaveWithSaksnummerWhenFagomradeNotGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_pensjon.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		Thread.sleep(4000);
		String soapXml = Xml.prettyPrint((findAll(postRequestedFor(urlEqualTo("/behandleoppgave")))
				.get(0)
				.getBodyAsString()));
		assertThat(soapXml.indexOf("saksnummer"), is(-1));
	}
	
	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en feil
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowValideringFeiletException() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_valideringFeiler.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_valideringFeiler.xml")));
				});
	}
	
	/**
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 * HVIS det oppstår en funksjonell feil i TJOARK122 SÅ avslutt og returner feilmelding
	 */
	@Test
	public void shouldThrowJournalpostIkkeFunnetExceptionTjoark122() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_jp-ikkefunnet.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy.xml")));
				});
	}
	
	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en feil
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowUgyldigInputverdiExceptionJournalpostIdNotANumberTjoark122() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_journalpostId_notANumber.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_journalpostId_notANumber.xml")));
				});
	}
	
	/**
	 * HVIS TJOARK122 ikke er tilgjengelig SÅ prøv igjen før avslutt
	 */
	@Test
	public void shouldThrowTechnicalExceptionTjoark122() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_internalServerError.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy.xml")));
				});
	}
	
	/**
	 * HVIS TJOARK110 ikke er tilgjengelig SÅ prøv igjen før avslutt
	 */
	@Test
	public void shouldThrowTechnicalExceptionTjoark110() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_internalServerError.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy.xml")));
				});
	}
	
	/**
	 * HVIS tjeneste BehandleOppgave_v1 ikke er tilgjengelig SÅ avslutt og returner feil
	 */
	@Test
	public void shouldThrowTechnicalExceptionOpprettOppgave() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_internalServerError.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(backoutQueue);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy.xml")));
				});
	}
	
	/**
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowWSSikkerhetsbegrensningExceptionGosys() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_WSSikkerhetsbegrensningException.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_happy.xml")));
				});
	}
	
	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowUgyldigInputverdiExceptionIllegalOppgavetype() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_illegalOppgavetype.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_illegalOppgavetype.xml")));
				});
	}
	
	/**
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en feil
	 * HVIS oppgavetype er feil -  legg i kø for funksjonelle feil (med hele meldingen)
	 */
	@Test
	public void shouldThrowUgyldigInputverdiExceptionIllegaArkivsystem() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));
		stubFor(post("/behandleoppgave").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("behandleoppgave/opprettOppgave_happy.xml")));
		
		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_illegalArkivsystem.xml"), CALLID);
		
		await().atMost(10, SECONDS)
				.untilAsserted(() -> {
					String response = receive(functionalBOQ);
					assertThat(response, is(classpathToString("qopp001/qopp001_illegalArkivsystem.xml")));
				});
	}
	
	private void sendStringMessage(Queue queue, final String message, String callId) {
		jmsTemplate.send(queue, session -> {
			TextMessage msg = new ActiveMQTextMessage();
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
