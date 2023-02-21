package no.nav.dokopp.consumer.tjoark110;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS_CALLED;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_LATENCY_TIMER_BUILDER;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

@Service
public class Tjoark110SettJournalpostAttributter {
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;
	private final MeterRegistry meterRegistry;
	private final static int RETRIES = 3;
	
	@Autowired
	public Tjoark110SettJournalpostAttributter(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1,
											   MeterRegistry meterRegistry) {
		this.arkiverDokumentproduksjonV1 = arkiverDokumentproduksjonV1;
		this.meterRegistry = meterRegistry;
	}
	
	@Retryable(value = DokoppTechnicalException.class, maxAttempts = RETRIES, backoff = @Backoff(delay = 500))
	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		Timer.Sample sample = Timer.start(meterRegistry);
		try {
			arkiverDokumentproduksjonV1.settJournalpostAttributter(mapRequest(settJournalpostAttributterRequestTo));
		} catch (Exception e) {
			throw new DokoppTechnicalException("teknisk feil ved kall mot arkiverDokumentproduksjonV1:settJournalpostAttributter. Antall retries=" + RETRIES + ", journalpostId=" + settJournalpostAttributterRequestTo
					.getJournalpostId(), e);
		} finally {
			sample.stop(REQUEST_LATENCY_TIMER_BUILDER.tags(LABEL_PROCESS, SERVICE_ID,
					LABEL_PROCESS_CALLED, "Joark::ArkiverDokumentproduksjonV1:settJournalpostAttributter")
					.register(meterRegistry));
		}
	}
	
	private SettJournalpostAttributterRequest mapRequest(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		return new SettJournalpostAttributterRequest()
				.withEndretAvNavn(SERVICE_ID)
				.withJournalpostIdListe(Long.valueOf(settJournalpostAttributterRequestTo.getJournalpostId()))
				.withAntallReturpost(settJournalpostAttributterRequestTo.getAntallRetur());
	}
}
