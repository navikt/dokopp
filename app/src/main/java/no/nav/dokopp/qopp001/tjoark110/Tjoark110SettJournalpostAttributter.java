package no.nav.dokopp.qopp001.tjoark110;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestLatency;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.prometheus.client.Histogram;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Tjoark110SettJournalpostAttributter {
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;
	private final static int retries = 3;
	
	@Inject
	public Tjoark110SettJournalpostAttributter(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1) {
		this.arkiverDokumentproduksjonV1 = arkiverDokumentproduksjonV1;
	}
	
	@Retryable(value = DokoppTechnicalException.class, maxAttempts = retries, backoff = @Backoff(delay = 500))
	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		Histogram.Timer requestTimer = requestLatency.labels(SERVICE_ID, "Joark::ArkiverDokumentproduksjonV1:settJournalpostAttributter")
				.startTimer();
		try {
			arkiverDokumentproduksjonV1.settJournalpostAttributter(mapRequest(settJournalpostAttributterRequestTo));
		} catch (Exception e) {
			throw new DokoppTechnicalException("teknisk feil ved kall mot arkiverDokumentproduksjonV1:settJournalpostAttributter. Antall retries=" + retries + ", journalpostId=" + settJournalpostAttributterRequestTo
					.getJournalpostId(), e);
		} finally {
			requestTimer.observeDuration();
		}
	}
	
	private SettJournalpostAttributterRequest mapRequest(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		return new SettJournalpostAttributterRequest()
				.withEndretAvNavn(SERVICE_ID)
				.withJournalpostIdListe(Long.valueOf(settJournalpostAttributterRequestTo.getJournalpostId()))
				.withAntallReturpost(settJournalpostAttributterRequestTo.getAntallRetur());
	}
}
