package no.nav.dokopp.qopp001.behandleOppgaveV1;

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
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class OpprettOppgaveGosys {
	
	private final BehandleOppgaveV1 behandleOppgaveV1;
	
	@Inject
	public OpprettOppgaveGosys(BehandleOppgaveV1 behandleOppgaveV1) {
		this.behandleOppgaveV1 = behandleOppgaveV1;
	}
	
	public String opprettOppgave(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		try {
			WSOpprettOppgaveResponse wsOpprettOppgaveResponse = behandleOppgaveV1.opprettOppgave(mapRequest(opprettOppgaveRequestTo));
			return wsOpprettOppgaveResponse.getOppgaveId();
		} catch (WSSikkerhetsbegrensningException e) {
			throw new AvsluttBehandlingException("OpprettOppgave tilgang avvist", e);
		} catch (Exception e){
			throw new DokoppTechnicalException("teknisk feil ved kall mot behandleOppgaveV1:opprettOppgave, journalpostId=" + opprettOppgaveRequestTo.getJournalpostId(), e);
		}
	}
	
	private WSOpprettOppgaveRequest mapRequest(OpprettOppgaveRequestTo opprettOppgaveRequestTo) {
		final WSAktor wsAktor = mapAktoer(opprettOppgaveRequestTo);
		return new WSOpprettOppgaveRequest()
				//TODO hva er dette
				.withOpprettetAvEnhetId(9999)
				.withWsOppgave(new WSOppgave()
						.withOppgavetypeKode(opprettOppgaveRequestTo.getOppgavetype())
						.withFagomradeKode(opprettOppgaveRequestTo.getFagomrade())
						.withPrioritetKode(opprettOppgaveRequestTo.getPrioritetkode())
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
