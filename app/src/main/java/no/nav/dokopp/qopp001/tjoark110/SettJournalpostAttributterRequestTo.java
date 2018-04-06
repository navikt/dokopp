package no.nav.dokopp.qopp001.tjoark110;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
@AllArgsConstructor
public class SettJournalpostAttributterRequestTo {
	private final String journalpostId;
	private int antallRetur;
}
