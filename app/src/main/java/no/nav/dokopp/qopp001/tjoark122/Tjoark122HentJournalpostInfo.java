package no.nav.dokopp.qopp001.tjoark122;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestLatency;
import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.prometheus.client.Histogram;
import no.nav.dokopp.exception.AvsluttBehandlingException;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.apache.camel.ExchangeProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Tjoark122HentJournalpostInfo {
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1;
	private final static int retries = 3;
	
	@Inject
	public Tjoark122HentJournalpostInfo(DokumentproduksjonInfoV1 dokumentproduksjonInfoV1) {
		this.dokumentproduksjonInfoV1 = dokumentproduksjonInfoV1;
	}
	
	@Retryable(value = DokoppTechnicalException.class, maxAttempts = retries, backoff = @Backoff(delay = 500))
	public HentJournalpostInfoResponseTo hentJournalpostInfo(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId) {
		HentJournalpostInfoRequest hentJournalpostInfoRequest = mapRequest(journalpostId);
		Histogram.Timer requestTimer = requestLatency.labels(SERVICE_ID, "Joark::DokumentproduksjonInfoV1:hentJournalpostInfo")
				.startTimer();
		try {
			HentJournalpostInfoResponse hentJournalpostInfoResponse = dokumentproduksjonInfoV1.hentJournalpostInfo(hentJournalpostInfoRequest);
			return mapResponse(hentJournalpostInfoResponse);
		} catch (HentJournalpostInfoJournalpostIkkeFunnet e) {
			throw new AvsluttBehandlingException("journalpost ikke funnet. Antall retries=0", e);
		} catch (Exception e) {
			throw new DokoppTechnicalException("teknisk feil ved kall mot dokumentproduksjonInfoV1:hentJournalpostInfo. Antall retries=" + retries + ", journalpostId=" + journalpostId, e);
		} finally {
			requestTimer.observeDuration();
		}
	}
	
	private HentJournalpostInfoRequest mapRequest(String journalpostId) {
		try {
			return new HentJournalpostInfoRequest().withJournalpostId(Long.parseLong(journalpostId));
		} catch (NumberFormatException e) {
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
				.antallRetur(hentJournalpostInfoResponse.getAntallRetur())
				.build();
	}
}
