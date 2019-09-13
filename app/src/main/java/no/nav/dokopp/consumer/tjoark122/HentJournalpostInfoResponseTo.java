package no.nav.dokopp.consumer.tjoark122;

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
	private Integer antallRetur;

	public boolean isAlleredeRegistrertReturpost() {
		return antallRetur != null && antallRetur > 0;
	}
}
