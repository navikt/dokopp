package no.nav.dokopp.qopp001.behandleOppgaveV1;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestLatency;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.prometheus.client.Histogram;
import no.nav.dokopp.exception.AvsluttBehandlingException;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.dokopp.util.XmlGregorianConverter;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSAktor;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSAktorType;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOppgave;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class OpprettOppgaveGosys {

	private static final String GSAK_OPPGAVETYPE_RETURPOST = "RETUR";
	private static final String GSAK_PRIORITETKODE_LAV = "LAV";
	public static final int OPPRETTET_AV_ENHET = 9999;
	private final BehandleOppgaveV1 behandleOppgaveV1;
	private final static int RETRIES = 3;
	
	@Inject
	public OpprettOppgaveGosys(BehandleOppgaveV1 behandleOppgaveV1) {
		this.behandleOppgaveV1 = behandleOppgaveV1;
	}
	
	@Retryable(value = DokoppTechnicalException.class, maxAttempts = RETRIES, backoff = @Backoff(delay = 500))
	public String opprettOppgave(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		Histogram.Timer requestTimer = requestLatency.labels(SERVICE_ID, "Gsak::BehandleOppgaveV1:opprettOppgave").startTimer();
		try {
			WSOpprettOppgaveResponse wsOpprettOppgaveResponse = behandleOppgaveV1.opprettOppgave(mapRequest(opprettOppgaveRequestTo));
			return wsOpprettOppgaveResponse.getOppgaveId();
		} catch (WSSikkerhetsbegrensningException e) {
			throw new AvsluttBehandlingException("OpprettOppgave tilgang avvist. Antall retries=0", e);
		} catch (Exception e) {
			throw new DokoppTechnicalException("teknisk feil ved kall mot behandleOppgaveV1:opprettOppgave. Antall retries=" + RETRIES + ", journalpostId=" + opprettOppgaveRequestTo
					.getJournalpostId(), e);
		} finally {
			requestTimer.observeDuration();
		}
	}
	
	private WSOpprettOppgaveRequest mapRequest(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		final WSAktor wsAktor = mapAktoer(opprettOppgaveRequestTo);
		return new WSOpprettOppgaveRequest()
				.withOpprettetAvEnhetId(OPPRETTET_AV_ENHET)
				.withWsOppgave(new WSOppgave()
						// OppgaveTypeKode er eks RETUR_FOR, RETUR_PEN osv.
						.withOppgavetypeKode(GSAK_OPPGAVETYPE_RETURPOST + "_" + opprettOppgaveRequestTo.getFagomrade())
						.withFagomradeKode(opprettOppgaveRequestTo.getFagomrade())
						.withPrioritetKode(GSAK_PRIORITETKODE_LAV)
						.withBeskrivelse(opprettOppgaveRequestTo.getBeskrivelse())
						.withAnsvarligEnhetId(opprettOppgaveRequestTo.getJournalFEnhet())
						.withDokumentId(opprettOppgaveRequestTo.getJournalpostId())
						.withSaksnummer(mapSaksnummer(opprettOppgaveRequestTo))
						.withAktivFra(XmlGregorianConverter.toXmlGregorianCalendar(LocalDateTime.now()))
						.withLest(false)
						.withGjelderBruker(wsAktor));
	}
	
	private WSAktor mapAktoer(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		if (opprettOppgaveRequestTo.containsBruker()) {
			return new WSAktor()
					.withAktorType(mapAktoerType(opprettOppgaveRequestTo.getBrukertypeKode()))
					.withIdent(opprettOppgaveRequestTo.getBrukerId());
		}
		return null;
	}
	
	private WSAktorType mapAktoerType(BrukerType brukertypeKode) {
		switch (brukertypeKode) {
			case PERSON:
				return WSAktorType.PERSON;
			case ORGANISASJON:
				return WSAktorType.ORGANISASJON;
			default:
				return WSAktorType.UKJENT;
		}
	}
	
	private String mapSaksnummer(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		if (opprettOppgaveRequestTo.isFagomradeGosysOrGsak()) {
			return opprettOppgaveRequestTo.getSaksnummer();
		}
		return null;
	}
}
