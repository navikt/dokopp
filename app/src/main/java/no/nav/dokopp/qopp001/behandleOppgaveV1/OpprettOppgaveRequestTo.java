package no.nav.dokopp.qopp001.behandleOppgaveV1;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class OpprettOppgaveRequestTo {
	private final String oppgavetype;
	private final String fagomrade;
	private final String prioritetkode;
	private final String beskrivelse;
	private final String journalFEnhet;
	private final String journalpostId;
	private String brukerId;
	private String brukertypeKode;
	private String saksnummer;
}
