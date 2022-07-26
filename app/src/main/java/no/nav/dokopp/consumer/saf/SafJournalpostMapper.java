package no.nav.dokopp.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.qopp001.JournalpostResponse;

import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.AKTOERID;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.FNR;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.ORGNR;
import static no.nav.dokopp.qopp001.domain.BrukerType.ORGANISASJON;
import static no.nav.dokopp.qopp001.domain.BrukerType.PERSON;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class SafJournalpostMapper {

	public static JournalpostResponse map(SafResponse.SafJournalpost safJournalpost) {
		SafResponse.SafJournalpost.Bruker bruker = safJournalpost.getBruker();
		SafResponse.SafJournalpost.AvsenderMottaker avsenderMottaker = safJournalpost.getAvsenderMottaker();
		return JournalpostResponse.builder()
				.journalfEnhet(safJournalpost.getJournalfoerendeEnhet())
				.tema(safJournalpost.getTema())
				.brukerId((bruker == null || isEmpty(bruker.getId())) ? null : bruker.getId().trim())
				.brukertype(bruker == null ? null : mapBrukerType(bruker.getType()))
				.saksnummer(safJournalpost.getSak().getArkivsaksnummer())
				.fagsystem(safJournalpost.getSak().getArkivsaksystem())
				.avsenderMottakerId((avsenderMottaker == null || isEmpty(avsenderMottaker.getId())) ? null : avsenderMottaker.getId().trim())
				.avsenderMottakerType(avsenderMottaker == null ? null : mapBrukerType(avsenderMottaker.getType()))
				.antallRetur(mapAntallRetur(safJournalpost.getAntallRetur()))
				.build();
	}

	private static Integer mapAntallRetur(String antallRetur) {
		try {
			return isEmpty(antallRetur) ? null : Integer.parseInt(antallRetur);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static String mapBrukerType(String brukertype) {
		return (AKTOERID.name().equalsIgnoreCase(brukertype) || FNR.name().equalsIgnoreCase(brukertype)) ? PERSON.name() :
					ORGNR.name().equalsIgnoreCase(brukertype) ? ORGANISASJON.name() : null;
	}

	enum BrukerType {
		ORGNR, AKTOERID, FNR
	}
}
