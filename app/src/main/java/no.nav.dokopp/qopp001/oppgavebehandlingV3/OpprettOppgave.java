package no.nav.dokopp.qopp001.oppgavebehandlingV3;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.binding.OppgavebehandlingV3;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.OpprettOppgaveRequest;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.meldinger.OpprettOppgaveResponse;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
//TODO Implement this class - as of now it is only a rough sketch
public class OpprettOppgave {
	
	private final OppgavebehandlingV3 oppgavebehandlingV3;
	private final OpprettOppgaveRequestMapper requestMapper;
	
	@Inject
	public OpprettOppgave(OppgavebehandlingV3 oppgavebehandlingV3,
						  OpprettOppgaveRequestMapper requestMapper) {
		this.oppgavebehandlingV3 = oppgavebehandlingV3;
		this.requestMapper = requestMapper;
	}
	
	@Handler
	public String opprettOppgave() {
		OpprettOppgaveRequest request = requestMapper.map();
		try {
			OpprettOppgaveResponse response = oppgavebehandlingV3.opprettOppgave(request);
			return mapOpprettOppgaveResponse(response);
			//TODO catch correct exception type and log a suitable message
		} catch (Exception e) {
			log.info("");
		}
		return null;
	}
	
	//TODO Implement responseMapper
	private String mapOpprettOppgaveResponse(OpprettOppgaveResponse response) {
		return new String();
	}
}
