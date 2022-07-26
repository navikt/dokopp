package no.nav.dokopp.qopp001;

import no.nav.dokopp.constants.DomainConstants;
import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokopp.exception.UkjentBrukertypeException;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpprettOppgaveMapperTest {

	private static final String SAKSNUMMER = "123456";
	private static final String FNR = "12345678901";
	private static final String AKTOER_ID = "10000047896";
	private static final String ORGNR = "123456789";
	private static final String JOURNALPOST_ID = "36875";
	private static final String JOURNALF_ENHET = "1234";
	private static final String OPPGAVEBESKRIVELSE = "Returpost";
	private static final String FAGSYSTEM_PEN = "PEN";
	private static final String FAGSYSTEM_GOSYS = "FS22";
	private static final String FAGOMRAADE_IAR = "IAR";
	private static final String FAGOMRAADE_HJE = "HJE";
	private static final String PRIORITETKODE_LAV = "LAV";
	private static final String OPPGAVETYPE_RETURPOST = "RETUR";
	private static final String ENHETS_ID = "9999";
	private static final int ANTALL_DAGER_AKTIV = 14;

	private final PdlGraphQLConsumer pdlGraphQLConsumer = mock(PdlGraphQLConsumer.class);
	private final OpprettOppgaveMapper opprettOppgaveMapper = new OpprettOppgaveMapper(pdlGraphQLConsumer);

	@Test
	public void shouldOpprettOppgaveGosys() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithPersonAndPensjon(),
				createOpprettOppgave());

		assertOpprettOppgaveRequestWithPersonAndPensjon(request);
	}

	/**
	 * Om både bruker og avsenderMottaker er satt skal bruker brukes
	 */
	@Test
	public void shouldMapWithBothBrukerAndAvsenderMottaker() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithBrukerAndAvsenderMottaker(),
				createOpprettOppgave());

		assertThat(request.getAktoerId(), is(AKTOER_ID));
		assertNull(request.getOrgnr());
		assertThat(request.getTema(), is(FAGOMRAADE_IAR));
		assertThat(request.getSaksreferanse(), is(SAKSNUMMER));
		assertOpprettOppgaveRequest(request);
	}

	/**
	 * Om bare avsenderMottaker er satt skal avsenderMottaker brukes i steden for bruker
	 */
	@Test
	public void shouldMapWithBrukerNullAvsenderMottakerOrganisasjon() {
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithBrukerisNullAndAvsenderMottaker(),
				createOpprettOppgave());

		assertNull(request.getAktoerId());
		assertThat(request.getOrgnr(), is(ORGNR));
		assertNull(request.getAktoerId());
		assertThat(request.getTema(), is(FAGOMRAADE_IAR));
		assertThat(request.getSaksreferanse(), is(SAKSNUMMER));
		assertOpprettOppgaveRequest(request);
	}

	@Test
	public void shouldOpprettOppgaveWithSaksnummerWhenFagomraadeIsGosys() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithPersonAndGosys(),
				createOpprettOppgave());

		assertOpprettOppgaveRequestWithPersonAndGosys(request);
	}

	@Test
	public void shouldOpprettOppgaveWithOrgnr() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithOrganisasjon(),
				createOpprettOppgave());

		assertOpprettOppgaveRequestWithOrganisasjon(request);
	}

	@Test
	public void shouldOpprettetOppgaveAndSetTildeltEnhetsnrNullWhenJournalfEnhetEr9999(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithEnhet9999(),
				createOpprettOppgave());
		assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(request);
	}

	@Test
	public void shouldOpprettetOppgaveAndSetTildeltEnhetsnrWithJournalfEnhetWhenNot9999(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithOrganisasjon(),
				createOpprettOppgave());
		assertOpprettOppgaveRequestWithOrganisasjon(request);
	}

	@Test
	public void shouldOpprettetOppgaveSetTildeltEnhetsnrNullWhenJournalfEnhetIsNull(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithJournalEnhetNull(),
				createOpprettOppgave());
		assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(request);
	}

	@Test
	public void shouldThrowUkjentBrukertypeException() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for ukjent brukertype."));

		Exception e = Assertions.assertThrows(UkjentBrukertypeException.class, () -> opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithUkjent(), createOpprettOppgave()));
		Assertions.assertEquals("Ukjent brukertype er ikke støttet.", e.getMessage());
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithBrukerAndAvsenderMottaker() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(BrukerType.PERSON.name())
				.avsenderMottakerId(BrukerType.ORGANISASJON.name())
				.avsenderMottakerId(ORGNR)
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}
	private JournalpostResponse createHentJournalpostInfoResponseToWithBrukerisNullAndAvsenderMottaker() {
		return JournalpostResponse.builder()
				.brukerId(null)
				.brukertype(null)
				.avsenderMottakerType(BrukerType.ORGANISASJON.name())
				.avsenderMottakerId(ORGNR)
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}
	private JournalpostResponse createHentJournalpostInfoResponseToWithPersonAndPensjon() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(BrukerType.PERSON.name())
				.tema(FAGOMRAADE_HJE)
				.fagsystem(FAGSYSTEM_PEN)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithPersonAndGosys() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(BrukerType.PERSON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithOrganisasjon() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(BrukerType.ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithJournalEnhetNull() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(BrukerType.ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(null)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithUkjent() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(BrukerType.UKJENT.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithEnhet9999() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(BrukerType.ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(ENHETS_ID)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private void assertOpprettOppgaveRequestWithPersonAndPensjon(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId(), is(AKTOER_ID));
		assertNull(request.getOrgnr());
		assertThat(request.getTema(), is(FAGOMRAADE_HJE));
		assertNull(request.getSaksreferanse());
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithPersonAndGosys(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId(), is(AKTOER_ID));
		assertNull(request.getOrgnr());
		assertThat(request.getTema(), is(FAGOMRAADE_IAR));
		assertThat(request.getSaksreferanse(), is(SAKSNUMMER));
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithOrganisasjon(OpprettOppgaveRequest request) {
		assertNull(request.getAktoerId());
		assertThat(request.getOrgnr(), is(ORGNR));
		assertThat(request.getTema(), is(FAGOMRAADE_IAR));
		assertThat(request.getSaksreferanse(), is(SAKSNUMMER));
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(OpprettOppgaveRequest request) {
		assertNull(request.getAktoerId());
		assertThat(request.getOrgnr(), is(ORGNR));
		assertThat(request.getTema(), is(FAGOMRAADE_IAR));
		assertThat(request.getSaksreferanse(), is(SAKSNUMMER));
		assertOpprettOppgaveRequestWithEnhetNrNull(request);
	}

	private void assertOpprettOppgaveRequestWithEnhetNrNull(OpprettOppgaveRequest request) {
		assertThat(request.getTildeltEnhetsnr(), nullValue());
		assertThat(request.getOpprettetAvEnhetsnr(), is(ENHETS_ID));
		assertThat(request.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(request.getBeskrivelse(), is(OPPGAVEBESKRIVELSE));
		assertThat(request.getOppgavetype(), is(OPPGAVETYPE_RETURPOST));
		assertThat(request.getFristFerdigstillelse(), is(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString()));
		assertThat(request.getAktivDato(), is(LocalDate.now().toString()));
		assertThat(request.getPrioritet(), is(PRIORITETKODE_LAV));
	}

	private void assertOpprettOppgaveRequest(OpprettOppgaveRequest request) {
		assertThat(request.getTildeltEnhetsnr(), is(JOURNALF_ENHET));
		assertThat(request.getOpprettetAvEnhetsnr(), is(ENHETS_ID));
		assertThat(request.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(request.getBeskrivelse(), is(OPPGAVEBESKRIVELSE));
		assertThat(request.getOppgavetype(), is(OPPGAVETYPE_RETURPOST));
		assertThat(request.getFristFerdigstillelse(), is(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString()));
		assertThat(request.getAktivDato(), is(LocalDate.now().toString()));
		assertThat(request.getPrioritet(), is(PRIORITETKODE_LAV));
	}

	private OpprettOppgave createOpprettOppgave() {
		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(DomainConstants.BEHANDLE_RETURPOST);
		opprettOppgave.setArkivSystem(DomainConstants.ARKIVSYSTEM_JOARK);
		opprettOppgave.setArkivKode(JOURNALPOST_ID);
		return opprettOppgave;
	}
}
