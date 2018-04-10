package no.nav.dokopp.qopp001.tjoark122;

import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;

import no.nav.dokopp.exception.AvsluttBehandlingException;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Tjoark122HentJournalpostInfo {
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1;

	@Inject
	public Tjoark122HentJournalpostInfo(DokumentproduksjonInfoV1 dokumentproduksjonInfoV1) {
		this.dokumentproduksjonInfoV1 = dokumentproduksjonInfoV1;
	}

	@Handler
	public HentJournalpostInfoResponseTo hentJournalpostInfo(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId) {
		HentJournalpostInfoRequest hentJournalpostInfoRequest = mapRequest(journalpostId);
		try {
			HentJournalpostInfoResponse hentJournalpostInfoResponse = dokumentproduksjonInfoV1.hentJournalpostInfo(hentJournalpostInfoRequest);
			return mapResponse(hentJournalpostInfoResponse);
		} catch(HentJournalpostInfoJournalpostIkkeFunnet e) {
			throw new AvsluttBehandlingException("journalpost ikke funnet", e);
		} catch (Exception e){
			throw new DokoppTechnicalException("teknisk feil ved kall mot dokumentproduksjonInfoV1:hentJournalpostInfo, journalpostId=" + journalpostId, e);
		}
	}

	private HentJournalpostInfoRequest mapRequest(String journalpostId) {
		try {
			return new HentJournalpostInfoRequest().withJournalpostId(Long.parseLong(journalpostId));
		} catch(NumberFormatException e) {
			throw new UgyldigInputverdiException("journalpostId er ikke et tall", e);
		}
	}

	private HentJournalpostInfoResponseTo mapResponse(HentJournalpostInfoResponse hentJournalpostInfoResponse) {
		return HentJournalpostInfoResponseTo.builder()
				.journalfEnhet(hentJournalpostInfoResponse.getJournalfEnhet())
				.fagomrade(hentJournalpostInfoResponse.getFagomrade())
				.brukerId(hentJournalpostInfoResponse.getBrukerId())
				.brukertype(hentJournalpostInfoResponse.getBrukerType())
				.saksnummer(hentJournalpostInfoResponse.getSaksNummer())
				.fagsystem(hentJournalpostInfoResponse.getFagsystem())
				.build();
	}
}
