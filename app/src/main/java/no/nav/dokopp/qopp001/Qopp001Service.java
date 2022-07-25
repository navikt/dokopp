package no.nav.dokopp.qopp001;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.consumer.oppgave.Oppgave;
import no.nav.dokopp.consumer.oppgave.OpprettOppgaveRequest;
import no.nav.dokopp.consumer.saf.JournalpostResponse;
import no.nav.dokopp.consumer.saf.SafJournalpostConsumer;
import no.nav.dokopp.consumer.tjoark110.SettJournalpostAttributterRequestTo;
import no.nav.dokopp.consumer.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.exception.AvsluttBehandlingOgKastMeldingException;
import no.nav.dokopp.exception.OpprettOppgaveFunctionalException;
import no.nav.dokopp.exception.ReturpostAlleredeFlaggetException;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static no.nav.dokopp.constants.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.constants.DomainConstants.BEHANDLE_RETURPOST;
import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Service
public class Qopp001Service {

	private static final int ANTALL_RETUR = 1;
	private static final String FAGOMRAADE_STO = "STO";
	private static final String MASKINELL_ENHET = "9999";
	private final Oppgave oppgave;
	private final OpprettOppgaveMapper opprettOppgaveMapper;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;
	private final SafJournalpostConsumer safJournalpostConsumer;

	@Autowired
	public Qopp001Service(Oppgave oppgave,
						  OpprettOppgaveMapper opprettOppgaveMapper,
						  Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter,
						  SafJournalpostConsumer safJournalpostConsumer) {
		this.oppgave = oppgave;
		this.opprettOppgaveMapper = opprettOppgaveMapper;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
		this.safJournalpostConsumer = safJournalpostConsumer;
	}

	@Handler
	public void qopp001(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId, OpprettOppgave opprettOppgave) {
		validateOppgaveTypeAndArkivsystem(opprettOppgave);

		JournalpostResponse journalpostResponse = safJournalpostConsumer.hentJournalpost(journalpostId);
		log.info("qopp001 har hentet journalpostInfo fra Joark for returpost med journalpostId={}.", journalpostId);

		if (journalpostResponse.isAlleredeRegistrertReturpost()) {
			throw new ReturpostAlleredeFlaggetException("qopp001 har oppdaget at returpost allerede er flagget som antallRetur=" + journalpostResponse.getAntallRetur() + ". Oppretter ikke oppgave i Gosys");
		} else {
			behandleReturpostOppgave(journalpostId, opprettOppgave, journalpostResponse);
		}
	}

	private void behandleReturpostOppgave(String journalpostId, OpprettOppgave opprettOppgave, JournalpostResponse journalpostResponse) {
		final String fagomrade = journalpostResponse.getFagomrade();
		final String journalfoerendeEnhet = journalpostResponse.getJournalfEnhet();
		if (FAGOMRAADE_STO.equalsIgnoreCase(fagomrade)) {
			log.info("qopp001 lager ikke oppgave i Gosys for journalpostId={} da den er returpost fra fagområde={} og ikke vil bli behandlet.",
					journalpostId, fagomrade);
		} else {
			OpprettOppgaveRequest opprettOppgaveRequest = opprettOppgaveMapper.map(journalpostResponse, opprettOppgave);
			try {
				Integer oppgaveId = oppgave.opprettOppgave(opprettOppgaveRequest);
				log.info("qopp001 har opprettet oppgave i Gosys med oppgaveId={}, fagområde={} for returpost med journalpostId={}.",
						oppgaveId, fagomrade, journalpostId);
			} catch (OpprettOppgaveFunctionalException e) {
				if (MASKINELL_ENHET.equals(journalfoerendeEnhet)) {
					log.warn("qopp001 klarte ikke å opprette oppgave i Gosys for journalpostId={}, fagområde={}. Maskinell enhet 9999.",
							journalpostId, fagomrade);
					throw e;
				} else {
					log.info("qopp001 klarte ikke å opprette oppgave i Gosys for journalpostId={}, fagområde={} på første forsøk med journalførendeEnhet={}. Forsøker på nytt med tildeltEnhetsnummer=null",
							journalpostId, fagomrade, journalfoerendeEnhet);
					Integer oppgaveId = oppgave.opprettOppgave(opprettOppgaveRequest.toBuilder().tildeltEnhetsnr(null).build());
					log.info("qopp001 har opprettet oppgave i Gosys med oppgaveId={}, fagområde={} for returpost med journalpostId={} og tildeltEnhetsnummer=null.",
							oppgaveId, fagomrade, journalpostId);
				}
			}
		}
		tjoark110SettJournalpostAttributter.settJournalpostAttributter(new SettJournalpostAttributterRequestTo(journalpostId, ANTALL_RETUR));
		log.info("qopp001 har flagget journalpost med journalpostId={} som returpost.", journalpostId);
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
