package no.nav.dokopp.qopp001.oppgavebehandlingV3;

import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.OpprettOppgaveRequest;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class OpprettOppgaveRequestMapper {
	
	//TODO Implement requestMapper.
	public OpprettOppgaveRequest map() {
		return new OpprettOppgaveRequest();
	}
}
