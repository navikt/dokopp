package no.nav.dokopp.qopp001.behandleOppgaveV1;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Builder;
import lombok.Data;
import no.nav.dokopp.qopp001.domain.BrukerType;

import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class OpprettOppgaveRequestTo {

	private static final String GOSYS = "FS22";
	private static final String GSAK = "FS19";
	private static final List<String> GOSYS_APPIDS = Arrays.asList(GOSYS, GSAK);

	private final String oppgavetype;
	private final String fagomrade;
	private final String prioritetkode;
	private final String beskrivelse;
	private final String journalFEnhet;
	private final String journalpostId;
	private String brukerId;
	private BrukerType brukertypeKode;
	private String saksnummer;

	boolean containsBruker() {
		return !isBlank(brukerId) && nonNull(brukertypeKode);
	}

	boolean isFagomradeGosys() {
		return GOSYS_APPIDS.contains(fagomrade);
	}
}
