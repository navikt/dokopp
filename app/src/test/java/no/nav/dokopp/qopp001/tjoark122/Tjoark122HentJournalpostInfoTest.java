package no.nav.dokopp.qopp001.tjoark122;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import no.nav.dokopp.exception.AvsluttBehandlingException;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(MockitoJUnitRunner.class)
public class Tjoark122HentJournalpostInfoTest {
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final String FNR = "***gammelt_fnr***";
	private static final String PERSON = "PERSON";
	private static final String SAKSNUMMER = "1";
	private static final String FAGSYSTEM = "OB36";
	private static final String FAGOMRADE = "STO";
	private static final int ANTALL_RETUR = 0;
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1Mock = Mockito.mock(DokumentproduksjonInfoV1.class);
	private final Tjoark122HentJournalpostInfo tjoark122 = new Tjoark122HentJournalpostInfo(dokumentproduksjonInfoV1Mock, new SimpleMeterRegistry());

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldMapResponse() {
		when(dokumentproduksjonInfoV1Mock.hentJournalpostInfo(any(HentJournalpostInfoRequest.class))).thenReturn(createResponse());
		HentJournalpostInfoResponseTo responseTo = tjoark122.hentJournalpostInfo("1");
		assertThat(responseTo.getJournalfEnhet(), is(JOURNALFOERENDE_ENHET));
		assertThat(responseTo.getAntallRetur(), is(ANTALL_RETUR));
		assertThat(responseTo.getBrukerId(), is(FNR));
		assertThat(responseTo.getBrukertype(), is(PERSON));
		assertThat(responseTo.getSaksnummer(), is(SAKSNUMMER));
		assertThat(responseTo.getFagsystem(), is(FAGSYSTEM));
		assertThat(responseTo.getFagomrade(), is(FAGOMRADE));
	}

	// https://jira.adeo.no/browse/MMA-2300
	@Test
	public void shouldMapResponseWhenBrukerIdHasTrailingWhitespace() {
		when(dokumentproduksjonInfoV1Mock.hentJournalpostInfo(any(HentJournalpostInfoRequest.class))).thenReturn(createResponse()
				.withBrukerId("999999999 "));
		HentJournalpostInfoResponseTo responseTo = tjoark122.hentJournalpostInfo("1");
		assertThat(responseTo.getBrukerId(), is("999999999"));
	}

	// https://jira.adeo.no/browse/MMA-2300
	@Test
	public void shouldThrowFunctionalExceptionWhenBrukerIdIsNull() {
		thrown.expect(AvsluttBehandlingException.class);

		when(dokumentproduksjonInfoV1Mock.hentJournalpostInfo(any(HentJournalpostInfoRequest.class))).thenReturn(createResponse().withBrukerId(null));
		tjoark122.hentJournalpostInfo("1");
	}

	private HentJournalpostInfoResponse createResponse() {
		return new HentJournalpostInfoResponse()
				.withJournalfEnhet(JOURNALFOERENDE_ENHET)
				.withAntallRetur(ANTALL_RETUR)
				.withBrukerId(FNR)
				.withBrukerType(PERSON)
				.withSaksNummer(SAKSNUMMER)
				.withFagsystem(FAGSYSTEM)
				.withFagomrade(FAGOMRADE);
	}
}