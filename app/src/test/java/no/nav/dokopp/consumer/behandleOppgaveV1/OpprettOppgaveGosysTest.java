package no.nav.dokopp.consumer.behandleOppgaveV1;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveRequest;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class OpprettOppgaveGosysTest {
	
	private static final String SAKSNUMMER = "123456";
	private static final String BRUKER_ID = "98632547896";
	private static final String JOURNALPOST_ID = "36875";
	private static final String JOURNALF_ENHET = "journalFEnhet";
	private static final String BESKRIVELSE = "beskrivelse";
	private static final String PRIORITETKODE = "prioritetkode";
	private static final String FAGOMRAADE = "fagomraade";
	private static final String FAGOMRAADE_GOSYS = "FS22";
	private static final String FAGOMRAADE_GSAK = "FS19";
	private static final String OPPGAVETYPE = "oppgavetype";
	private static final String OPPGAVE_ID = "oppgaveId";
	private static final BrukerType BRUKER_TYPE = BrukerType.PERSON;
	private static final int ENHETS_ID = 9999;
	
	private BehandleOppgaveV1 behandleOppgaveV1 = Mockito.mock(BehandleOppgaveV1.class);
	private OpprettOppgaveGosys opprettOppgaveGosys = new OpprettOppgaveGosys(behandleOppgaveV1, new SimpleMeterRegistry());
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldOpprettOppgaveGosys() throws Exception {
		when(behandleOppgaveV1.opprettOppgave(any(WSOpprettOppgaveRequest.class))).thenReturn(new WSOpprettOppgaveResponse().withOppgaveId(OPPGAVE_ID));
		String response = opprettOppgaveGosys.opprettOppgave(createOpprettOppgaveRequestTo(FAGOMRAADE));
		
		assertThat(response, is(OPPGAVE_ID));
		ArgumentCaptor<WSOpprettOppgaveRequest> argumentCaptor = ArgumentCaptor.forClass(WSOpprettOppgaveRequest.class);
		verify(behandleOppgaveV1).opprettOppgave(argumentCaptor.capture());
		assertWSOpprettOppgaveRequest(argumentCaptor.getValue());
	}
	
	@Test
	public void shouldOpprettOppgaveWithSaksnummerWhenFagomraadeIsGosys() throws Exception {
		when(behandleOppgaveV1.opprettOppgave(any(WSOpprettOppgaveRequest.class))).thenReturn(new WSOpprettOppgaveResponse().withOppgaveId(OPPGAVE_ID));
		String response = opprettOppgaveGosys.opprettOppgave(createOpprettOppgaveRequestTo(FAGOMRAADE_GOSYS));
		
		assertThat(response, is(OPPGAVE_ID));
		ArgumentCaptor<WSOpprettOppgaveRequest> argumentCaptor = ArgumentCaptor.forClass(WSOpprettOppgaveRequest.class);
		verify(behandleOppgaveV1).opprettOppgave(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getWsOppgave().getSaksnummer(), is(SAKSNUMMER));
	}
	
	@Test
	public void shouldOpprettOppgaveWithSaksnummerWhenFagomraadeIsGsak() throws Exception {
		when(behandleOppgaveV1.opprettOppgave(any(WSOpprettOppgaveRequest.class))).thenReturn(new WSOpprettOppgaveResponse().withOppgaveId(OPPGAVE_ID));
		String response = opprettOppgaveGosys.opprettOppgave(createOpprettOppgaveRequestTo(FAGOMRAADE_GSAK));
		
		assertThat(response, is(OPPGAVE_ID));
		ArgumentCaptor<WSOpprettOppgaveRequest> argumentCaptor = ArgumentCaptor.forClass(WSOpprettOppgaveRequest.class);
		verify(behandleOppgaveV1).opprettOppgave(argumentCaptor.capture());
		
		assertThat(argumentCaptor.getValue().getWsOppgave().getSaksnummer(), is(SAKSNUMMER));
	}
	
	
	private OpprettOppgaveRequestTo createOpprettOppgaveRequestTo(String fagomraade) {
		return OpprettOppgaveRequestTo.builder()
				.saksnummer(SAKSNUMMER)
				.brukerId(BRUKER_ID)
				.journalpostId(JOURNALPOST_ID)
				.journalFEnhet(JOURNALF_ENHET)
				.beskrivelse(BESKRIVELSE)
				.fagomrade(fagomraade)
				.brukertypeKode(BRUKER_TYPE)
				.build();
	}
	
	private void assertWSOpprettOppgaveRequest(WSOpprettOppgaveRequest request) {
		assertThat(request.getOpprettetAvEnhetId(), is(ENHETS_ID));
		assertThat(request.getWsOppgave().getOppgavetypeKode(), is("RETUR_" + FAGOMRAADE));
		assertThat(request.getWsOppgave().getFagomradeKode(), is(FAGOMRAADE));
		assertThat(request.getWsOppgave().getPrioritetKode(), is("LAV"));
		assertThat(request.getWsOppgave().getBeskrivelse(), is(BESKRIVELSE));
		assertThat(request.getWsOppgave().getAnsvarligEnhetId(), is(JOURNALF_ENHET));
		assertThat(request.getWsOppgave().getDokumentId(), is(JOURNALPOST_ID));
		assertThat(request.getWsOppgave().getDokumentId(), is(JOURNALPOST_ID));
		assertThat(request.getWsOppgave().getSaksnummer(), is(nullValue()));
		assertThat(request.getWsOppgave().getGjelderBruker().getAktorType().name(), is(BRUKER_TYPE.name()));
		assertThat(request.getWsOppgave().getGjelderBruker().getIdent(), is(BRUKER_ID));
	}
	
}
