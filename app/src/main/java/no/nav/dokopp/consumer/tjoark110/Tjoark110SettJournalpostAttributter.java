package no.nav.dokopp.consumer.tjoark110;

import no.nav.dokopp.exception.DokoppTechnicalException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

@Service
public class Tjoark110SettJournalpostAttributter {
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;

	@Autowired
	public Tjoark110SettJournalpostAttributter(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1) {
		this.arkiverDokumentproduksjonV1 = arkiverDokumentproduksjonV1;
	}
	
	@Retryable(value = DokoppTechnicalException.class, backoff = @Backoff(delay = 500))
	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		try {
			arkiverDokumentproduksjonV1.settJournalpostAttributter(mapRequest(settJournalpostAttributterRequestTo));
		} catch (Exception e) {
			throw new DokoppTechnicalException("Teknisk feil ved kall mot arkiverDokumentproduksjonV1:settJournalpostAttributter for journalpostId=" + settJournalpostAttributterRequestTo
					.getJournalpostId(), e);
		}
	}
	
	private SettJournalpostAttributterRequest mapRequest(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		return new SettJournalpostAttributterRequest()
				.withEndretAvNavn(SERVICE_ID)
				.withJournalpostIdListe(Long.valueOf(settJournalpostAttributterRequestTo.getJournalpostId()))
				.withAntallReturpost(settJournalpostAttributterRequestTo.getAntallRetur());
	}
}
