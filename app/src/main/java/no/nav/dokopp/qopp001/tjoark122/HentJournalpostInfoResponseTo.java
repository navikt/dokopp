package no.nav.dokopp.qopp001.tjoark122;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class HentJournalpostInfoResponseTo {
	private String journalfEnhet;
	private String fagomrade;
	private String brukerId;
	private String saksnummer;
	private String fagsystem;
	private String brukertype;
}
