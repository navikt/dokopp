package no.nav.dokopp.consumer.saf;

import no.nav.dokopp.qopp001.JournalpostResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SafHentSafJournalpostMapperTest {

	private final String AKTOERID = "AKTOERID";
	private final String FNR = "FNR";
	private final String ORGNR = "ORGNR";
	private final String PERSON = "PERSON";
	private final String ORGANISASJON = "ORGANISASJON";
	private final String TEMA_DAG = "DAG";
	private final String JOURNALFOERENDE_ENHET = "2990";
	private final String GOSYS = "FS22";
	private final String BRUKER_ID = "4324324234";
	private final String BRUKER_ID2 = "1212121";
	private final String SAKSNUMMER = "123";

	@Test
	public void shouldMap() {
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), null, "0"));
		assertThat(response.getBrukerId(), is(BRUKER_ID));
		assertThat(response.getBrukertype(), is(PERSON));
		assertNull(response.getAvsenderMottakerId());
		assertNull(response.getAvsenderMottakerType());
		validateJournalpost(response);
	}

	@Test
	public void shouldMapBrukerOgAvsenderMottakerSatt(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), createAvsenderMottaker(ORGNR), "0"));
		assertThat(response.getBrukerId(), is(BRUKER_ID));
		assertThat(response.getBrukertype(), is(PERSON));
		assertThat(response.getAvsenderMottakerType(), is(ORGANISASJON));
		assertThat(response.getAvsenderMottakerId(), is(BRUKER_ID2));
	}

	@Test
	public void shouldMapWhenBrukerNullAndAvsenderMottaker(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(null, createAvsenderMottaker(ORGNR), "0"));
		assertNull(response.getBrukerId());
		assertNull(response.getBrukertype());
		assertThat(response.getAvsenderMottakerType(), is(ORGANISASJON));
		assertThat(response.getAvsenderMottakerId(), is(BRUKER_ID2));
	}

	@Test
	public void shouldMapWhenAvsenderMottakerNull(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), null, "0"));
		assertThat(response.getBrukerId(), is(BRUKER_ID));
		assertThat(response.getBrukertype(), is(PERSON));
		assertNull(response.getAvsenderMottakerType());
		assertNull(response.getAvsenderMottakerId());
	}

	@Test
	public void shouldMapWhenSakNull(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), null, "0", null));
		assertThat(response.getBrukerId(), is(BRUKER_ID));
		assertThat(response.getBrukertype(), is(PERSON));
		assertNull(response.getAvsenderMottakerType());
		assertNull(response.getAvsenderMottakerId());
		assertNull(response.getSaksnummer());
	}

	@Test
	public void shouldMapWhenAntallReturNull(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), null, null));
		assertNull(response.getAntallRetur());
	}

	@Test
	public void shouldMapWhenAntallReturisNumber(){
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(FNR), null, "1"));
		assertThat(response.getAntallRetur(), is(1));
	}

	@Test
	public void shouldMapBrukerWithTrailingSpaces(){
		SafResponse.SafJournalpost.Bruker bruker = SafResponse.SafJournalpost.Bruker.builder()
				.type(FNR)
				.id("9999999999 ").build();

		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(bruker, null, "0"));
		assertThat(response.getBrukerId(), is("9999999999"));
	}

	@Test
	public void shouldAvsenderMottakerWithTrailingSpaces(){
		SafResponse.SafJournalpost.AvsenderMottaker avsenderMottaker = SafResponse.SafJournalpost.AvsenderMottaker.builder()
				.type(FNR)
				.id("9999999999 ").build();

		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(null, avsenderMottaker, "0"));
		assertThat(response.getAvsenderMottakerId(), is("9999999999"));
	}

	@ParameterizedTest()
	@ValueSource(strings = {AKTOERID, FNR})
	public void shouldMapBrukerTypeWhenPerson(String brukertype) {
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(brukertype), null, "0"));
		assertThat(response.getBrukertype(), is(PERSON));
	}

	@ParameterizedTest()
	@ValueSource(strings = ORGNR)
	public void shouldMapBrukerTypeWhenOrganisasjon(String brukertype) {
		JournalpostResponse response = SafJournalpostMapper.map(createJournalpost(createBruker(brukertype), null, "0"));
		assertThat(response.getBrukertype(), is(ORGANISASJON));
	}

	private void validateJournalpost(JournalpostResponse response){
		assertThat(response.getJournalfEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(response.getTema(), is(TEMA_DAG));
		assertThat(response.getAntallRetur(), is(0));
		assertThat(response.getFagsystem(), is(GOSYS));
		assertThat(response.getSaksnummer(), is(SAKSNUMMER));
	}

	public SafResponse.SafJournalpost createJournalpost(SafResponse.SafJournalpost.Bruker bruker,
														SafResponse.SafJournalpost.AvsenderMottaker avsenderMottaker,
														String antallRetur) {
		return createJournalpost(bruker, avsenderMottaker,antallRetur, createSak());

	}


	public SafResponse.SafJournalpost createJournalpost(SafResponse.SafJournalpost.Bruker bruker,
														SafResponse.SafJournalpost.AvsenderMottaker avsenderMottaker,
														String antallRetur,
														SafResponse.SafJournalpost.Sak sak) {
		return SafResponse.SafJournalpost.builder()
				.antallRetur(antallRetur)
				.bruker(bruker)
				.avsenderMottaker(avsenderMottaker)
				.sak(sak)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.tema(TEMA_DAG).build();

	}


	private SafResponse.SafJournalpost.Bruker createBruker(String brukerType) {
		return SafResponse.SafJournalpost.Bruker.builder()
				.type(brukerType)
				.id(BRUKER_ID).build();
	}

	private SafResponse.SafJournalpost.Sak createSak() {
		return SafResponse.SafJournalpost.Sak.builder()
				.arkivsaksnummer(SAKSNUMMER)
				.arkivsaksystem(GOSYS).build();
	}

	private SafResponse.SafJournalpost.AvsenderMottaker createAvsenderMottaker(String avsenderMottakerType) {
		return SafResponse.SafJournalpost.AvsenderMottaker.builder()
				.type(avsenderMottakerType)
				.id(BRUKER_ID2).build();
	}
}
