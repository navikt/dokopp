package no.nav.dokopp.qopp001;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class JournalpostResponse {
	private String journalpostId;
	private String journalfEnhet;
	private String tema;
	private String brukerId;
	private String saksnummer;
	private String fagsystem;
	private String brukertype;
	private Integer antallRetur;
	private String avsenderMottakerId;
	private String avsenderMottakerType;

	public boolean isAlleredeRegistrertReturpost() {
		return antallRetur != null && antallRetur > 0;
	}
}
