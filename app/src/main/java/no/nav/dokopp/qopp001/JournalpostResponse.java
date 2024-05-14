package no.nav.dokopp.qopp001;

import lombok.Builder;
import lombok.Data;

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
	private boolean skjerming;
	private boolean hoveddokumentSkjerming;

	public boolean isAlleredeRegistrertReturpost() {
		return antallRetur != null && antallRetur > 0;
	}
}
