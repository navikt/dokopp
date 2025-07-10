package no.nav.dokopp.consumer.saf;

import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.AvsenderMottaker;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.Bruker;
import no.nav.dokopp.consumer.saf.SafResponse.SafJournalpost.Sak;
import no.nav.dokopp.qopp001.JournalpostResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

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
	private final String JOURNALPOSTID = "12345678911";

	@Test
	public void skalMappe() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), null, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isEqualTo(BRUKER_ID);
		assertThat(response.getBrukertype()).isEqualTo(PERSON);
		assertThat(response.getAvsenderMottakerId()).isNull();
		assertThat(response.getAvsenderMottakerType()).isNull();
		assertThat(response.getJournalpostId()).isEqualTo(JOURNALPOSTID);
		assertThat(response.getJournalfEnhet()).isEqualTo(JOURNALFOERENDE_ENHET);
		assertThat(response.getTema()).isEqualTo(TEMA_DAG);
		assertThat(response.getAntallRetur()).isEqualTo(0);
		assertThat(response.getFagsystem()).isEqualTo(GOSYS);
		assertThat(response.getSaksnummer()).isEqualTo(SAKSNUMMER);
	}

	@Test
	public void skalMappeHvisBaadeBrukerOgAvsenderMottakerErSatt() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), createAvsenderMottaker(ORGNR), "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isEqualTo(BRUKER_ID);
		assertThat(response.getBrukertype()).isEqualTo(PERSON);
		assertThat(response.getAvsenderMottakerType()).isEqualTo(ORGANISASJON);
		assertThat(response.getAvsenderMottakerId()).isEqualTo(BRUKER_ID2);
	}

	@Test
	public void skalMappeNaarBrukerErNull() {
		SafJournalpost safJournalpost = createJournalpost(null, createAvsenderMottaker(ORGNR), "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isNull();
		assertThat(response.getBrukertype()).isNull();
		assertThat(response.getAvsenderMottakerType()).isEqualTo(ORGANISASJON);
		assertThat(response.getAvsenderMottakerId()).isEqualTo(BRUKER_ID2);
	}

	@Test
	public void skalMappeNaarAvsenderMottakerErNull() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), null, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isEqualTo(BRUKER_ID);
		assertThat(response.getBrukertype()).isEqualTo(PERSON);
		assertThat(response.getAvsenderMottakerType()).isNull();
		assertThat(response.getAvsenderMottakerId()).isNull();
	}

	@Test
	public void skalMappeNaarAvsenderMottakerOgSakErNull() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), null, "0", null);

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isEqualTo(BRUKER_ID);
		assertThat(response.getBrukertype()).isEqualTo(PERSON);
		assertThat(response.getAvsenderMottakerType()).isNull();
		assertThat(response.getAvsenderMottakerId()).isNull();
		assertThat(response.getSaksnummer()).isNull();
	}

	@Test
	public void skalMappeNaarAvsenderMottakerOgAntallReturErNull() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), null, null);

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getAntallRetur()).isNull();
	}

	@Test
	public void skalMappeNaarAntallReturErEtTall() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(FNR), null, "1");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getAntallRetur()).isEqualTo(1);
	}

	@Test
	public void skalMappeNaarBrukerHarTrailingSpaces() {
		Bruker bruker = Bruker.builder()
				.type(FNR)
				.id("9999999999 ")
				.build();
		SafJournalpost safJournalpost = createJournalpost(bruker, null, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukerId()).isEqualTo("9999999999");
	}

	@Test
	public void skalMappeNaarAvsenderMottakerHarTrailingSpaces() {
		AvsenderMottaker avsenderMottaker = AvsenderMottaker.builder()
				.type(FNR)
				.id("9999999999 ")
				.build();
		SafJournalpost safJournalpost = createJournalpost(null, avsenderMottaker, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getAvsenderMottakerId()).isEqualTo("9999999999");
	}

	@ParameterizedTest()
	@ValueSource(strings = {AKTOERID, FNR})
	public void skalMappeNaarBrukerTypeErPerson(String brukertype) {
		SafJournalpost safJournalpost = createJournalpost(createBruker(brukertype), null, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukertype()).isEqualTo(PERSON);
	}

	@Test
	public void skalMappeNaarBrukerTypeErOrganisasjon() {
		SafJournalpost safJournalpost = createJournalpost(createBruker(ORGNR), null, "0");

		JournalpostResponse response = SafJournalpostMapper.map(safJournalpost, JOURNALPOSTID);

		assertThat(response.getBrukertype()).isEqualTo(ORGANISASJON);
	}

	public SafJournalpost createJournalpost(Bruker bruker, AvsenderMottaker avsenderMottaker, String antallRetur) {
		return createJournalpost(bruker, avsenderMottaker, antallRetur, createSak());
	}


	public SafJournalpost createJournalpost(Bruker bruker, AvsenderMottaker avsenderMottaker, String antallRetur, Sak sak) {
		return SafJournalpost.builder()
				.antallRetur(antallRetur)
				.bruker(bruker)
				.avsenderMottaker(avsenderMottaker)
				.sak(sak)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.tema(TEMA_DAG)
				.build();
	}

	private Bruker createBruker(String brukerType) {
		return Bruker.builder()
				.type(brukerType)
				.id(BRUKER_ID)
				.build();
	}

	private Sak createSak() {
		return Sak.builder()
				.arkivsaksnummer(SAKSNUMMER)
				.arkivsaksystem(GOSYS)
				.build();
	}

	private AvsenderMottaker createAvsenderMottaker(String avsenderMottakerType) {
		return AvsenderMottaker.builder()
				.type(avsenderMottakerType)
				.id(BRUKER_ID2)
				.build();
	}
}
