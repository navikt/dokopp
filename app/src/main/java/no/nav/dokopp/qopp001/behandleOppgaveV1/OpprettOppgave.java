package no.nav.dokopp.qopp001.behandleOppgaveV1;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
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
	
	private final BehandleOppgaveV1 behandleOppgaveV1;
	
	@Inject
	public OpprettOppgave(BehandleOppgaveV1 behandleOppgaveV1) {
		this.behandleOppgaveV1 = behandleOppgaveV1;
	}
	
	@Handler
	public String opprettOppgave() {
//TODO: Make request
		try {
			//TODO: Do the call and return the response
			
			//TODO catch correct exception type and log a suitable message
		} catch (Exception e) {
			log.info("");
		}
		return null;
	}
	
}
