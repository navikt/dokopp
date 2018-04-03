package no.nav.dokopp.qopp001.itest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.nio.charset.StandardCharsets.UTF_8;
import static no.nav.modig.common.MDCOperations.MDC_CALL_ID;

import no.nav.dokopp.Application;
import no.nav.modig.core.test.FileUtils;
import no.nav.modig.testcertificates.TestCertificates;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
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

	@Inject
	private JmsTemplate jmsTemplate;
	@Inject
	private Queue qopp001;

	@BeforeClass
	public static void beforeClass() {
		TestCertificates.setupTemporaryTrustStore("no/nav/modig/testcertificates/truststore.jts", "changeit");
		File file = FileUtils.putInTempFile(TestCertificates.class.getClassLoader()
				.getResourceAsStream("no/nav/modig/testcertificates/keystore.jks"));
		System.setProperty("SRVDOKOPP_CERT_KEYSTORE", file.getAbsolutePath());
		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
	}

	@Test
	public void shouldOppretteOppgave() throws Exception {
		stubFor(post("/arkiverdokumentproduksjon").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark110/tjoark110_happy.xml")));
		stubFor(post("/dokumentproduksjoninfo").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withBodyFile("tjoark122/tjoark122_happy.xml")));

		sendStringMessage(qopp001, classpathToString("qopp001/qopp001_happy.xml"), CALLID);

		//TODO uncomment when impl ready
//		await().atMost(5, SECONDS).until(() -> findAll(postRequestedFor(urlEqualTo("/arkiverdokumentmottak"))).size() == 1);
//		verify(postRequestedFor(urlEqualTo("/arkiverdokumentmottak")).withRequestBody(matchingXPath("//journalpostIdListe", equalTo("100000"))));
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
}
