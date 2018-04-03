package no.nav.dokopp.qopp001.tjoark110;

import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class Tjoark110SettJournalpostAttributter {
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;

	@Inject
	public Tjoark110SettJournalpostAttributter(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1) {
		this.arkiverDokumentproduksjonV1 = arkiverDokumentproduksjonV1;
	}

	@Handler
	public void settJournalpostAttributter(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		arkiverDokumentproduksjonV1.settJournalpostAttributter(mapRequest(settJournalpostAttributterRequestTo));
	}

	private SettJournalpostAttributterRequest mapRequest(SettJournalpostAttributterRequestTo settJournalpostAttributterRequestTo) {
		return new SettJournalpostAttributterRequest()
				.withEndretAvNavn(SERVICE_ID)
				.withJournalpostIdListe(Long.valueOf(settJournalpostAttributterRequestTo.getJournalpostId()))
				.withAntallReturpost(settJournalpostAttributterRequestTo.getAntallRetur());
	}
}
