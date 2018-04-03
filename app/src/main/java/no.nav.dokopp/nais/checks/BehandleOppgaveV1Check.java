package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.BehandleOppgaveV1Alias;
import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.binding.BehandleOppgaveV1;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Component
public class BehandleOppgaveV1Check extends AbstractSelftest {
	public static final String BEHANDLEOPPGAVE_V1 = "BehandleOppgave_v3";
	private final BehandleOppgaveV1 behandleOppgaveV1;
	
	@Inject
	public BehandleOppgaveV1Check(BehandleOppgaveV1 behandleOppgaveV1, BehandleOppgaveV1Alias behandleOppgaveV1Alias) {
		super(Ping.Type.Soap,
				BEHANDLEOPPGAVE_V1,
				behandleOppgaveV1Alias.getEndpointurl(),
				behandleOppgaveV1Alias.getDescription() == null ? BEHANDLEOPPGAVE_V1 : behandleOppgaveV1Alias.getDescription());
		this.behandleOppgaveV1 = behandleOppgaveV1;
	}
	
	@Override
	protected void doCheck() {
		try {
			behandleOppgaveV1.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping behandleOppgaveV1", e);
		}
	}
}
