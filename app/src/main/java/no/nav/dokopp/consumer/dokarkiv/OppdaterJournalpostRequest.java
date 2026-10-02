package no.nav.dokopp.consumer.dokarkiv;

import java.time.LocalDate;

public record OppdaterJournalpostRequest(
		LocalDate datoRetur
) {
}