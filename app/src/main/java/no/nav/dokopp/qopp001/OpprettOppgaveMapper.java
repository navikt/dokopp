package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokopp.exception.UkjentBrukertypeException;
import no.nav.dokopp.qopp001.domain.OppgaveType;
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

@Component
@Slf4j
public class OpprettOppgaveMapper {

	private static final String GSAK_OPPGAVETYPE_RETURPOST = "RETUR";
	private static final String GSAK_PRIORITETKODE_LAV = "LAV";
	private static final String OPPGAVEBESKRIVELSE_RETURPOST = "Returpost";
	private static final String OPPGAVEBESKRIVELSE_MANGLENDE_ADRESSE = "Distribusjon feilet, mottaker mangler postadresse";
	private static final String OPPRETTET_AV_ENHET = "9999";
	private static final int ANTALL_DAGER_AKTIV = 14;
	private static final String AKTOER_ID = "aktoerId";
	private static final String ORGNR = "orgnr";
	private static final String GOSYS = "FS22";
	private static final String GSAK = "FS19";
	private static final String TEMA_FAR = "FAR";
	private static final String TEMA_BID = "BID";
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
				.beskrivelse(mapBeskrivelse(opprettOppgave.getOppgaveType()))
				.fristFerdigstillelse(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString())
				.journalpostId(opprettOppgave.getArkivKode())
				.oppgavetype(GSAK_OPPGAVETYPE_RETURPOST)
				.opprettetAvEnhetsnr(OPPRETTET_AV_ENHET)
				.orgnr(brukerMap.get(ORGNR))
				.prioritet(GSAK_PRIORITETKODE_LAV)
				.saksreferanse(mapSaksreferanse(journalpost))
				.tema(mapTema(journalpost))
				.tildeltEnhetsnr(hentJournalfEnhet(journalpost))
				.build();
	}

	private String mapBeskrivelse(String oppgaveType){
		return switch (OppgaveType.valueOf(oppgaveType)){
			case BEHANDLE_RETURPOST -> OPPGAVEBESKRIVELSE_RETURPOST;
			case BEHANDLE_MANGLENDE_ADRESSE -> OPPGAVEBESKRIVELSE_MANGLENDE_ADRESSE;
		};
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
		} else if (!isEmpty(orgnr)) {
			brukerMap.put(ORGNR, orgnr);
		}
		return brukerMap;
	}

	private String mapTema(JournalpostResponse journalpost) {
		String tema = journalpost.getTema();

		if (TEMA_FAR.equalsIgnoreCase(tema)) {
			log.info("qopp001 har mappet tema FAR til tema BID for journalpostId={} siden Oppgave ikke støtter opprettelse av oppgaver på tema FAR", journalpost.getJournalpostId());
			return TEMA_BID;
		}

		return tema;
	}

	private String mapSaksreferanse(JournalpostResponse journalpostResponse) {
		if (GOSYS_APPIDS.contains(journalpostResponse.getFagsystem())) {
			return journalpostResponse.getSaksnummer();
		}
		return null;
	}

	//Sjekk at brukerType eller avsenderMottakerType er enten person eller organisasjon
	private void validateBrukerType(JournalpostResponse journalpost) {
		if (!isBrukerTypePerson(journalpost.getBrukertype()) && !isBrukerTypePerson(journalpost.getAvsenderMottakerType()) &&
				!isBrukerTypeOrganisasjon(journalpost.getBrukertype()) && !isBrukerTypeOrganisasjon(journalpost.getAvsenderMottakerType())) {
			throw new UkjentBrukertypeException("Ukjent brukertype er ikke støttet.");
		}
	}

	private String mapOrgnr(JournalpostResponse journalpost) {
		String brukerOrgnr = isBrukerTypeOrganisasjon(journalpost.getBrukertype()) ? journalpost.getBrukerId() : "";
		String avsenderMottakerOrgnr = isBrukerTypeOrganisasjon(journalpost.getAvsenderMottakerType()) ? journalpost.getAvsenderMottakerId() : "";

		if (!isEmpty(brukerOrgnr)) {
			return brukerOrgnr;
		} else if (!isEmpty(avsenderMottakerOrgnr)) {
			log.info("JournalpostId={} har ikke brukerId. Bruker avsenderMottakerId for å hente ut orgnr", journalpost.getJournalpostId());
			return avsenderMottakerOrgnr;
		}
		return "";
	}

	private String mapAktoerId(JournalpostResponse journalpost) {
		String brukerId = isBrukerTypePerson(journalpost.getBrukertype()) ? journalpost.getBrukerId() : "";
		String avsenderMottakerId = isBrukerTypePerson(journalpost.getAvsenderMottakerType()) ? journalpost.getAvsenderMottakerId() : "";

		if (!isEmpty(brukerId)) {
			return pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(brukerId);
		} else if (!isEmpty(avsenderMottakerId)) {
			log.info("JournalpostId={} har ikke brukerId. Bruker AvsenderMottakerId for å hente ut personIdent", journalpost.getJournalpostId());
			return pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(avsenderMottakerId);
		}
		return "";
	}

	private boolean isBrukerTypePerson(String brukertype) {
		return PERSON.name().equalsIgnoreCase(brukertype);
	}

	private boolean isBrukerTypeOrganisasjon(String brukertype) {
		return ORGANISASJON.name().equalsIgnoreCase(brukertype);
	}
}
