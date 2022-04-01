package no.nav.dokopp.consumer.tjoark122;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokopp.exception.AvsluttBehandlingException;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.apache.camel.ExchangeProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS_CALLED;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_LATENCY_TIMER_BUILDER;
import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Tjoark122HentJournalpostInfo {
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1;
	private final MeterRegistry meterRegistry;
	private final static int retries = 3;
	
	@Autowired
	public Tjoark122HentJournalpostInfo(DokumentproduksjonInfoV1 dokumentproduksjonInfoV1, MeterRegistry meterRegistry) {
		this.dokumentproduksjonInfoV1 = dokumentproduksjonInfoV1;
		this.meterRegistry = meterRegistry;
	}
	
	@Retryable(value = DokoppTechnicalException.class, maxAttempts = retries, backoff = @Backoff(delay = 500))
	public HentJournalpostInfoResponseTo hentJournalpostInfo(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId) {
		HentJournalpostInfoRequest hentJournalpostInfoRequest = mapRequest(journalpostId);
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			HentJournalpostInfoResponse hentJournalpostInfoResponse = dokumentproduksjonInfoV1.hentJournalpostInfo(hentJournalpostInfoRequest);
			return mapResponse(hentJournalpostInfoResponse);
		} catch (HentJournalpostInfoJournalpostIkkeFunnet e) {
			throw new AvsluttBehandlingException("journalpost ikke funnet. Antall retries=0", e);
		} catch(NullPointerException e) {
			throw new AvsluttBehandlingException("HentJournalpostInfoResponse hadde data med ugyldig nullverdi.", e);
		} catch (Exception e) {
			throw new DokoppTechnicalException("teknisk feil ved kall mot dokumentproduksjonInfoV1:hentJournalpostInfo. Antall retries=" + retries + ", journalpostId=" + journalpostId, e);
		} finally {
			sample.stop(REQUEST_LATENCY_TIMER_BUILDER.tags(LABEL_PROCESS, SERVICE_ID,
					LABEL_PROCESS_CALLED, "Joark::DokumentproduksjonInfoV1:hentJournalpostInfo")
					.register(meterRegistry));
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
				.brukerId(hentJournalpostInfoResponse.getBrukerId().trim())
				.brukertype(hentJournalpostInfoResponse.getBrukerType())
				.saksnummer(hentJournalpostInfoResponse.getSaksNummer())
				.fagsystem(hentJournalpostInfoResponse.getFagsystem())
				.antallRetur(hentJournalpostInfoResponse.getAntallRetur())
				.build();
	}
}
