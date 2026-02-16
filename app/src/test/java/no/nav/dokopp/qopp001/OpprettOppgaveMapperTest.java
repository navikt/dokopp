package no.nav.dokopp.qopp001;

import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.consumer.pdl.PdlGraphQLConsumer;
import no.nav.dokopp.exception.UkjentBrukertypeException;
import no.nav.dokopp.qopp001.domain.OppgaveType;
import no.nav.opprettoppgave.tjenestespesifikasjon.OpprettOppgave;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static no.nav.dokopp.constants.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.qopp001.domain.BrukerType.ORGANISASJON;
import static no.nav.dokopp.qopp001.domain.BrukerType.PERSON;
import static no.nav.dokopp.qopp001.domain.BrukerType.UKJENT;
import static no.nav.dokopp.qopp001.domain.OppgaveType.BEHANDLE_RETURPOST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
	private static final String FAGOMRAADE_FAR = "FAR";
	private static final String FAGOMRAADE_BID = "BID";
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
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithPersonAndPensjon(request);
	}

	/**
	 * Om både bruker og avsenderMottaker er satt skal bruker brukes
	 */
	@Test
	public void shouldMapWithBothBrukerAndAvsenderMottaker() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithBrukerAndAvsenderMottaker(),
				createOpprettReturpostOppgave());

		assertThat(request.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(request.getOrgnr()).isNull();
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_IAR);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequest(request);
	}

	/**
	 * Om bare avsenderMottaker er satt skal avsenderMottaker brukes i steden for bruker
	 */
	@Test
	public void shouldMapWithBrukerNullAvsenderMottakerOrganisasjon() {
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithBrukerisNullAndAvsenderMottaker(),
				createOpprettReturpostOppgave());

		assertThat(request.getAktoerId()).isNull();
		assertThat(request.getOrgnr()).isEqualTo(ORGNR);
		assertThat(request.getAktoerId()).isNull();
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_IAR);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequest(request);
	}

	@ParameterizedTest
	@CsvSource(value ={"BEHANDLE_RETURPOST;Returpost", "BEHANDLE_MANGLENDE_ADRESSE;Distribusjon feilet, mottaker mangler postadresse"}, delimiter = ';')
	public void shouldcreateRightOppgave(String oppgaveType, String oppgaveBeskrivelse){
		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithBrukerisNullAndAvsenderMottaker(),
				createOpprettOppgave(OppgaveType.valueOf(oppgaveType)));

		assertThat(request.getBeskrivelse()).isEqualTo(oppgaveBeskrivelse);
	}

	@Test
	public void shouldMapTemaFarToTemaBid() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithTemaFar(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithTemaFar(request);
	}

	@Test
	public void shouldOpprettOppgaveWithSaksnummerWhenFagomraadeIsGosys() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(FNR)).thenReturn(AKTOER_ID);

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithPersonAndGosys(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithPersonAndGosys(request);
	}

	@Test
	public void shouldOpprettOppgaveWithOrgnr() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithOrganisasjon(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithOrganisasjon(request);
	}

	@Test
	public void shouldOpprettetOppgaveAndSetTildeltEnhetsnrNullWhenJournalfEnhetEr9999(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithEnhet9999(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(request);
	}

	@Test
	public void shouldOpprettetOppgaveAndSetTildeltEnhetsnrWithJournalfEnhetWhenNot9999(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithOrganisasjon(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithOrganisasjon(request);
	}

	@Test
	public void shouldOpprettetOppgaveSetTildeltEnhetsnrNullWhenJournalfEnhetIsNull(){
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for orgnr."));

		OpprettOppgaveRequest request = opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithJournalEnhetNull(),
				createOpprettReturpostOppgave());

		assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(request);
	}

	@Test
	public void shouldThrowUkjentBrukertypeException() {
		when(pdlGraphQLConsumer.hentAktoerIdForFolkeregisterident(anyString())).thenThrow(
				new RuntimeException("Skal ikke kalle PDL for ukjent brukertype."));

		assertThatExceptionOfType(UkjentBrukertypeException.class)
				.isThrownBy(() -> opprettOppgaveMapper.map(createHentJournalpostInfoResponseToWithUkjent(), createOpprettReturpostOppgave()))
				.withMessage("Ukjent brukertype er ikke støttet.");
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithBrukerAndAvsenderMottaker() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(PERSON.name())
				.avsenderMottakerId(ORGANISASJON.name())
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
				.avsenderMottakerType(ORGANISASJON.name())
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
				.brukertype(PERSON.name())
				.tema(FAGOMRAADE_HJE)
				.fagsystem(FAGSYSTEM_PEN)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithTemaFar() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(PERSON.name())
				.tema(FAGOMRAADE_FAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithPersonAndGosys() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(PERSON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithOrganisasjon() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithJournalEnhetNull() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(null)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithUkjent() {
		return JournalpostResponse.builder()
				.brukerId(FNR)
				.brukertype(UKJENT.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(JOURNALF_ENHET)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private JournalpostResponse createHentJournalpostInfoResponseToWithEnhet9999() {
		return JournalpostResponse.builder()
				.brukerId(ORGNR)
				.brukertype(ORGANISASJON.name())
				.tema(FAGOMRAADE_IAR)
				.fagsystem(FAGSYSTEM_GOSYS)
				.journalfEnhet(ENHETS_ID)
				.saksnummer(SAKSNUMMER)
				.build();
	}

	private void assertOpprettOppgaveRequestWithPersonAndPensjon(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(request.getOrgnr()).isNull();
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_HJE);
		assertThat(request.getSaksreferanse()).isNull();
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithTemaFar(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(request.getOrgnr()).isNull();
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_BID);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithPersonAndGosys(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(request.getOrgnr()).isNull();
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_IAR);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithOrganisasjon(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId()).isNull();
		assertThat(request.getOrgnr()).isEqualTo(ORGNR);
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_IAR);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequest(request);
	}

	private void assertOpprettOppgaveRequestWithOrganisasjonOgEnhetNrNull(OpprettOppgaveRequest request) {
		assertThat(request.getAktoerId()).isNull();
		assertThat(request.getOrgnr()).isEqualTo(ORGNR);
		assertThat(request.getTema()).isEqualTo(FAGOMRAADE_IAR);
		assertThat(request.getSaksreferanse()).isEqualTo(SAKSNUMMER);
		assertOpprettOppgaveRequestWithEnhetNrNull(request);
	}

	private void assertOpprettOppgaveRequestWithEnhetNrNull(OpprettOppgaveRequest request) {
		assertThat(request.getTildeltEnhetsnr()).isNull();;
		assertThat(request.getOpprettetAvEnhetsnr()).isEqualTo(ENHETS_ID);
		assertThat(request.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(request.getBeskrivelse()).isEqualTo(OPPGAVEBESKRIVELSE);
		assertThat(request.getOppgavetype()).isEqualTo(OPPGAVETYPE_RETURPOST);
		assertThat(request.getFristFerdigstillelse()).isEqualTo(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString());
		assertThat(request.getAktivDato()).isEqualTo(LocalDate.now().toString());
		assertThat(request.getPrioritet()).isEqualTo(PRIORITETKODE_LAV);
	}

	private void assertOpprettOppgaveRequest(OpprettOppgaveRequest request) {
		assertThat(request.getTildeltEnhetsnr()).isEqualTo(JOURNALF_ENHET);
		assertThat(request.getOpprettetAvEnhetsnr()).isEqualTo(ENHETS_ID);
		assertThat(request.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
		assertThat(request.getBeskrivelse()).isEqualTo(OPPGAVEBESKRIVELSE);
		assertThat(request.getOppgavetype()).isEqualTo(OPPGAVETYPE_RETURPOST);
		assertThat(request.getFristFerdigstillelse()).isEqualTo(LocalDate.now().plusDays(ANTALL_DAGER_AKTIV).toString());
		assertThat(request.getAktivDato()).isEqualTo(LocalDate.now().toString());
		assertThat(request.getPrioritet()).isEqualTo(PRIORITETKODE_LAV);
	}

	private OpprettOppgave createOpprettReturpostOppgave() {
		return createOpprettOppgave(BEHANDLE_RETURPOST);
	}

	private OpprettOppgave createOpprettOppgave(OppgaveType oppgaveType){
		OpprettOppgave opprettOppgave = new OpprettOppgave();
		opprettOppgave.setOppgaveType(oppgaveType.toString());
		opprettOppgave.setArkivSystem(ARKIVSYSTEM_JOARK);
		opprettOppgave.setArkivKode(JOURNALPOST_ID);
		return opprettOppgave;
	}

}