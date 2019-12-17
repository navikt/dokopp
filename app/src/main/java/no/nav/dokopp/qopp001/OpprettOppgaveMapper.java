package no.nav.dokopp.qopp001;

import no.nav.dokopp.consumer.aktoerregister.Aktoerregister;
import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.consumer.tjoark122.HentJournalpostInfoResponseTo;
import no.nav.dokopp.exception.UkjentBrukertypeException;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Component
public class OpprettOppgaveMapper {

	private static final String GSAK_OPPGAVETYPE_RETURPOST = "RETUR";
	private static final String GSAK_PRIORITETKODE_LAV = "LAV";
	private static final String OPPGAVEBESKRIVELSE = "Returpost";
	private static final String OPPRETTET_AV_ENHET = "9999";
	private static final int ANTALL_DAGER_AKTIV = 14;
	private static final String AKTOER_ID = "aktoerId";
	private static final String ORGNR = "orgnr";
	private static final String GOSYS = "FS22";
	private static final String GSAK = "FS19";
	private static final List<String> GOSYS_APPIDS = Arrays.asList(GOSYS, GSAK);

	private final Aktoerregister aktoerregister;

	public OpprettOppgaveMapper(Aktoerregister aktoerregister) {
		this.aktoerregister = aktoerregister;
	}

	public OpprettOppgaveRequest map(HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo, OpprettOppgave opprettOppgave) {
		Map<String, String> brukerMap = mapBruker(hentJournalpostInfoResponseTo.getBrukerId(), hentJournalpostInfoResponseTo.getBrukertype());
		return OpprettOppgaveRequest.builder()
				.aktivDato(LocalDate.now().toString())
				.aktoerId(brukerMap.get(AKTOER_ID))
				.beskrivelse(OPPGAVEBESKRIVELSE)
				.fristFerdigstillelse(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString())
				.journalpostId(opprettOppgave.getArkivKode())
				.oppgavetype(GSAK_OPPGAVETYPE_RETURPOST)
				.opprettetAvEnhetsnr(OPPRETTET_AV_ENHET)
				.orgnr(brukerMap.get(ORGNR))
				.prioritet(GSAK_PRIORITETKODE_LAV)
				.saksreferanse(mapSaksreferanse(hentJournalpostInfoResponseTo))
				.tema(hentJournalpostInfoResponseTo.getFagomrade())
				.tildeltEnhetsnr(hentJournalfEnhet(hentJournalpostInfoResponseTo))
				.build();
	}

	private String hentJournalfEnhet(HentJournalpostInfoResponseTo responseTo) {
		return responseTo.getJournalfEnhet() != "9999" ? responseTo.getJournalfEnhet() : "";
	}

	private Map<String, String> mapBruker(String brukerId, String brukertype) {
		Map<String, String> brukerMap = new HashMap<>();
		if (brukerId == null || brukerId.isEmpty() || brukertype == null || brukertype.isEmpty()) {
			return brukerMap;
		}

		BrukerType brukertypeKode = BrukerType.valueOf(brukertype);
		switch (brukertypeKode) {
			case PERSON:
				brukerMap.put(AKTOER_ID, aktoerregister.hentAktoerIdForFnr(brukerId));
				break;
			case ORGANISASJON:
				brukerMap.put(ORGNR, brukerId);
				break;
			default:
				throw new UkjentBrukertypeException("Ukjent brukertype er ikke støttet.");
		}
		return brukerMap;
	}

	private String mapSaksreferanse(HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo) {
		if (GOSYS_APPIDS.contains(hentJournalpostInfoResponseTo.getFagsystem())) {
			return hentJournalpostInfoResponseTo.getSaksnummer();
		}
		return null;
	}
}
