package no.nav.dokopp.qopp001;

import no.nav.dokopp.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.exception.UkjentBrukertypeException;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static no.nav.dokopp.qopp001.domain.BrukerType.ORGANISASJON;
import static no.nav.dokopp.qopp001.domain.BrukerType.PERSON;
import static org.apache.commons.lang3.StringUtils.isEmpty;


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

	private final PdlGraphQLConsumer pdlGraphQLConsumer;

	public OpprettOppgaveMapper(PdlGraphQLConsumer pdlGraphQLConsumer) {
		this.pdlGraphQLConsumer = pdlGraphQLConsumer;
	}

	public OpprettOppgaveRequest map(JournalpostResponse journalpost, OpprettOppgave opprettOppgave) {
		Map<String, String> brukerMap = mapBruker(journalpost);
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
				.saksreferanse(mapSaksreferanse(journalpost))
				.tema(journalpost.getTema())
				.tildeltEnhetsnr(hentJournalfEnhet(journalpost))
				.build();
	}

	private String hentJournalfEnhet(JournalpostResponse responseTo) {
		return OPPRETTET_AV_ENHET.equals(responseTo.getJournalfEnhet()) ? null : responseTo.getJournalfEnhet();
	}


	private Map<String, String> mapBruker(JournalpostResponse journalpost) {

		Map<String, String> brukerMap = new HashMap<>();
		validateBrukerType(journalpost);
		String aktoerId = mapAktoerId(journalpost);
		String orgnr = mapOrgnr(journalpost);


		if (!isEmpty(aktoerId)) {
			brukerMap.put(AKTOER_ID, aktoerId);
		} else if(!isEmpty(orgnr)){
			brukerMap.put(ORGNR, orgnr);
		} else {
			return brukerMap;
		}

		return brukerMap;
	}

	private String mapSaksreferanse(JournalpostResponse journalpostResponse) {
		if (GOSYS_APPIDS.contains(journalpostResponse.getFagsystem())) {
			return journalpostResponse.getSaksnummer();
		}
		return null;
	}

	//Sjekk at brukerType eller avsenderMottakerType er enten person eller organisasjon
	private void validateBrukerType(JournalpostResponse journalpost) {
		if (!isBrukerPerson(journalpost.getBrukertype()) && !isBrukerPerson(journalpost.getAvsenderMottakerType()) &&
				!isBrukerOrganisasjon(journalpost.getBrukertype()) && !isBrukerOrganisasjon(journalpost.getAvsenderMottakerType())) {
			throw new UkjentBrukertypeException("Ukjent brukertype er ikke støttet.");
		}
	}

	private String mapOrgnr(JournalpostResponse journalpost) {
		return isBrukerOrganisasjon(journalpost.getBrukertype()) ? journalpost.getBrukerId() :
				isBrukerOrganisasjon(journalpost.getAvsenderMottakerType()) ? journalpost.getAvsenderMottakerId() :
						"";
	}

	private String mapAktoerId(JournalpostResponse journalpost) {
		String brukerId = isBrukerPerson(journalpost.getBrukertype()) ? journalpost.getBrukerId() :
				isBrukerPerson(journalpost.getAvsenderMottakerType()) ? journalpost.getAvsenderMottakerId() :
						"";
		return isEmpty(brukerId) ? brukerId : pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(brukerId);
	}

	private boolean isBrukerPerson(String brukertype) {
		return PERSON.name().equalsIgnoreCase(brukertype);
	}

	private boolean isBrukerOrganisasjon(String brukertype) {
		return ORGANISASJON.name().equalsIgnoreCase(brukertype);
	}
}
