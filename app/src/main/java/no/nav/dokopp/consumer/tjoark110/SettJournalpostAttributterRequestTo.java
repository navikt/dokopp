package no.nav.dokopp.consumer.tjoark110;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SettJournalpostAttributterRequestTo {
	private final String journalpostId;
	private int antallRetur;
}
