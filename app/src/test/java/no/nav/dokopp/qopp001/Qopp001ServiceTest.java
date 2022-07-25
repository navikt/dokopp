package no.nav.dokopp.qopp001;

import no.nav.dokopp.constants.DomainConstants;
import no.nav.dokopp.consumer.saf.SafJournalpostConsumer;
import no.nav.dokopp.consumer.saf.JournalpostResponse;
import no.nav.dokopp.exception.AvsluttBehandlingOgKastMeldingException;
import no.nav.dokopp.exception.ReturpostAlleredeFlaggetException;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class Qopp001ServiceTest {

	private final SafJournalpostConsumer safHentJournalpostInfo = Mockito.mock(SafJournalpostConsumer.class);
	private final Qopp001Service qopp001Service = new Qopp001Service(null, null, null, safHentJournalpostInfo);

	@Test
	public void shouldThrowAvsluttBehandlingOgKastMeldingExceptionWhenUgyldigArkivSystem() {
		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivKode("1");

		assertThrows(AvsluttBehandlingOgKastMeldingException.class, () -> qopp001Service.qopp001("1", opprettOppgave));
	}

	@Test
	public void shouldThrowAvsluttBehandlingOgKastMeldingExceptionWhenUgyldigArkivKode() {
		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivSystem(DomainConstants.ARKIVSYSTEM_JOARK);

		assertThrows(AvsluttBehandlingOgKastMeldingException.class, () -> qopp001Service.qopp001("1", opprettOppgave));
	}

	@Test
	public void shouldThrowReturpostAlleredeFlaggetExceptionWhenAntallReturNotNull() {
		when(safHentJournalpostInfo.hentJournalpost(any(String.class))).thenReturn(createHentJournalpostInfoResponse());

		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivSystem(DomainConstants.ARKIVSYSTEM_JOARK);
		opprettOppgave.setArkivKode("1");

		assertThrows(ReturpostAlleredeFlaggetException.class, () -> qopp001Service.qopp001("1", opprettOppgave));
	}

	private JournalpostResponse createHentJournalpostInfoResponse(){
		return JournalpostResponse.builder()
				.antallRetur(1)
				.build();
	}
}
