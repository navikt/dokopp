package no.nav.dokopp.qopp001;

import static no.nav.dokopp.constants.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.constants.DomainConstants.BEHANDLE_RETURPOST;
import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.consumer.oppgave.Oppgave;
import no.nav.dokopp.consumer.tjoark110.SettJournalpostAttributterRequestTo;
import no.nav.dokopp.consumer.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.consumer.tjoark122.HentJournalpostInfoResponseTo;
import no.nav.dokopp.consumer.tjoark122.Tjoark122HentJournalpostInfo;
import no.nav.dokopp.exception.AvsluttBehandlingOgKastMeldingException;
import no.nav.dokopp.exception.ReturpostAlleredeFlaggetException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
public class Qopp001Service {

	private static final int ANTALL_RETUR = 1;
	private static final String FAGOMRAADE_STO = "STO";
	private final Oppgave oppgave;
	private final OpprettOppgaveMapper opprettOppgaveMapper;
	private final Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;

	@Inject
	public Qopp001Service(Oppgave oppgave,
						  OpprettOppgaveMapper opprettOppgaveMapper,
						  Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo,
						  Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter) {
		this.oppgave = oppgave;
		this.opprettOppgaveMapper = opprettOppgaveMapper;
		this.tjoark122HentJournalpostInfo = tjoark122HentJournalpostInfo;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
	}

	@Handler
	public void qopp001(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId, OpprettOppgave opprettOppgave) {
		validateOppgaveTypeAndArkivsystem(opprettOppgave);

		HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo = tjoark122HentJournalpostInfo.hentJournalpostInfo(journalpostId);
		log.info("qopp001 har hentet journalpostInfo fra Joark for returpost med journalpostId={}.", journalpostId);

		if (hentJournalpostInfoResponseTo.isAlleredeRegistrertReturpost()) {
			throw new ReturpostAlleredeFlaggetException("qopp001 har oppdaget at returpost allerede er flagget som antallRetur=" + hentJournalpostInfoResponseTo.getAntallRetur() + ". Oppretter ikke oppgave i Gosys");
		} else {
			if (FAGOMRAADE_STO.equalsIgnoreCase(hentJournalpostInfoResponseTo.getFagomrade())) {
				log.info("qopp001 lager ikke oppgave i Gosys for journalpostId={} da den er returpost fra fagområde={} og ikke vil bli behandlet.",  journalpostId, hentJournalpostInfoResponseTo.getFagomrade());
			} else {
				Integer oppgaveId = oppgave.opprettOppgave(opprettOppgaveMapper.map(hentJournalpostInfoResponseTo, opprettOppgave));
				log.info("qopp001 har opprettet oppgave i Gosys med oppgaveId={}, fagområde={} for returpost med journalpostId={}.", oppgaveId, hentJournalpostInfoResponseTo.getFagomrade(), journalpostId);
			}
			tjoark110SettJournalpostAttributter.settJournalpostAttributter(new SettJournalpostAttributterRequestTo(journalpostId, ANTALL_RETUR));
			log.info("qopp001 har flagget journalpost med journalpostId={} som returpost.", journalpostId);
		}
	}

	private void validateOppgaveTypeAndArkivsystem(OpprettOppgave opprettOppgave) {
		if (!BEHANDLE_RETURPOST.equals(opprettOppgave.getOppgaveType())) {
			throw new UgyldigInputverdiException("oppgaveType må være BEHANDLE_RETURPOST. oppgaveType=" + opprettOppgave.getOppgaveType());
		}

		if (!ARKIVSYSTEM_JOARK.equals(opprettOppgave.getArkivSystem()) || isBlank(opprettOppgave.getArkivKode())) {
			throw new AvsluttBehandlingOgKastMeldingException("arkivSystem må være JOARK og arkivKode må være satt. arkivSystem=" + opprettOppgave.getArkivSystem() +
					", arkivKode=" + opprettOppgave.getArkivKode());
		}
	}

}
