package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.OppgavebehandlingV3Alias;
import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.binding.OppgavebehandlingV3;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting AS
 */
@Component
public class OppgavebehandlingV3Check extends AbstractSelftest {
	public static final String OPPGAVEBEHANDLING_V3 = "Oppgavebehandling_v3";
	private final OppgavebehandlingV3 oppgavebehandlingV3;
	
	@Inject
	public OppgavebehandlingV3Check(OppgavebehandlingV3 oppgavebehandlingV3, OppgavebehandlingV3Alias oppgavebehandlingV3Alias) {
		super(Ping.Type.Soap,
				OPPGAVEBEHANDLING_V3,
				oppgavebehandlingV3Alias.getEndpointurl(),
				oppgavebehandlingV3Alias.getDescription() == null ? OPPGAVEBEHANDLING_V3 : oppgavebehandlingV3Alias.getDescription());
		this.oppgavebehandlingV3 = oppgavebehandlingV3;
	}
	
	@Override
	protected void doCheck() {
		try {
			oppgavebehandlingV3.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping OrganisasjonV4", e);
		}
	}
}
