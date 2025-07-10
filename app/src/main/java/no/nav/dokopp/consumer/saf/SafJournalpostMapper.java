package no.nav.dokopp.consumer.saf;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.AvsenderMottaker;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.Bruker;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.SafDokument;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.Sak;
import no.nav.dokopp.qopp001.JournalpostResponse;

import java.util.List;

import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.AKTOERID;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.FNR;
import static no.nav.dokopp.consumer.saf.SafJournalpostMapper.BrukerType.ORGNR;
import static no.nav.dokopp.qopp001.domain.BrukerType.ORGANISASJON;
import static no.nav.dokopp.qopp001.domain.BrukerType.PERSON;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Slf4j
public class SafJournalpostMapper {

	public static JournalpostResponse map(SafResponse.SafJournalpost safJournalpost, String journalpostId) {

		Bruker bruker = safJournalpost.getBruker();
		AvsenderMottaker avsenderMottaker = safJournalpost.getAvsenderMottaker();
		Sak sak = safJournalpost.getSak();

		return JournalpostResponse.builder()
				.journalpostId(journalpostId)
				.journalfEnhet(safJournalpost.getJournalfoerendeEnhet())
				.tema(safJournalpost.getTema())
				.brukerId((bruker == null || isEmpty(bruker.getId())) ? null : bruker.getId().trim())
				.brukertype(bruker == null ? null : mapBrukerType(bruker.getType()))
				.saksnummer(sak == null ? null : sak.getArkivsaksnummer())
				.fagsystem(sak == null ? null : sak.getArkivsaksystem())
				.avsenderMottakerId((avsenderMottaker == null || isEmpty(avsenderMottaker.getId())) ? null : avsenderMottaker.getId().trim())
				.avsenderMottakerType(avsenderMottaker == null ? null : mapBrukerType(avsenderMottaker.getType()))
				.antallRetur(mapAntallRetur(safJournalpost.getAntallRetur()))
				.skjerming(safJournalpost.getSkjerming() != null)
				.hoveddokumentSkjerming(finnSkjermingForDokumenter(safJournalpost.getDokumenter()))
				.build();
	}

	private static String mapBrukerType(String brukertype) {
		if (AKTOERID.name().equalsIgnoreCase(brukertype) || FNR.name().equalsIgnoreCase(brukertype)) {
			return PERSON.name();
		}

		if (ORGNR.name().equalsIgnoreCase(brukertype)) {
			return ORGANISASJON.name();
		}

		return null;
	}

	private static Integer mapAntallRetur(String antallRetur) {
		try {
			return isEmpty(antallRetur) ? null : Integer.parseInt(antallRetur);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static boolean finnSkjermingForDokumenter(List<SafDokument> dokumenter) {
		if (dokumenter == null || dokumenter.isEmpty()) {
			return false;
		}

		var dokument = dokumenter.getFirst();
		if (dokument.getSkjerming() != null) {
			return true;
		}

		return dokument.getDokumentvarianter().stream()
				.filter(safDokumentVariant -> "ARKIV".equalsIgnoreCase(safDokumentVariant.getVariantformat()))
				.anyMatch(safArkivDokumentVariant -> safArkivDokumentVariant.getSkjerming() != null);
	}

	enum BrukerType {
		ORGNR,
		AKTOERID,
		FNR
	}
}
