package no.nav.dokopp.qopp001;

import no.nav.dokopp.exception.AvsluttBehandlingOgKastMeldingException;
import no.nav.dokopp.qopp001.domain.DomainConstants;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class Qopp001ServiceTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private final Qopp001Service qopp001Service = new Qopp001Service(null, null, null);

	@Test
	public void shouldThrowAvsluttBehandlingOgKastMeldingExceptionWhenUgyldigArkivSystem() {
		thrown.expect(AvsluttBehandlingOgKastMeldingException.class);

		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivKode("1");
		qopp001Service.qopp001("1", opprettOppgave);
	}

	@Test
	public void shouldThrowAvsluttBehandlingOgKastMeldingExceptionWhenUgyldigArkivKode() {
		thrown.expect(AvsluttBehandlingOgKastMeldingException.class);

		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivSystem(DomainConstants.ARKIVSYSTEM_JOARK);
		qopp001Service.qopp001("1", opprettOppgave);
	}
}
