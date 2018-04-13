package no.nav.dokopp.qopp001.service;

import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokopp.qopp001.domain.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.qopp001.domain.DomainConstants.BEHANDLE_RETURPOST;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgaveGosys;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgaveRequestTo;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.dokopp.qopp001.tjoark110.SettJournalpostAttributterRequestTo;
import no.nav.dokopp.qopp001.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.qopp001.tjoark122.HentJournalpostInfoResponseTo;
import no.nav.dokopp.qopp001.tjoark122.Tjoark122HentJournalpostInfo;
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
public class ServiceOrchestrator {
	
	private final OpprettOppgaveGosys opprettOppgaveGosys;
	private final Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;
	
	@Inject
	public ServiceOrchestrator(OpprettOppgaveGosys opprettOppgaveGosys,
							   Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo,
							   Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter) {
		this.opprettOppgaveGosys = opprettOppgaveGosys;
		this.tjoark122HentJournalpostInfo = tjoark122HentJournalpostInfo;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
	}
	
	@Handler
	public void orchestrate(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId, OpprettOppgave opprettOppgave) {
		validateOppgaveTypeAndArkivsystem(opprettOppgave);
		
		HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo = tjoark122HentJournalpostInfo.hentJournalpostInfo(journalpostId);
		log.info("qopp001 har hentet journalpostInfo fra Joark for forespørsel med journalpostId=" + journalpostId + ".");
		
		String oppgaveId = opprettOppgaveGosys.opprettOppgave(mapToOpprettOppgaveRequestTo(hentJournalpostInfoResponseTo, opprettOppgave));
		log.info("qopp001 har opprettet oppgave i Gosys med oppgaveId=" + oppgaveId + " for forespørsel med journalpostId=" + journalpostId + ".");
		
		tjoark110SettJournalpostAttributter.settJournalpostAttributter(new SettJournalpostAttributterRequestTo(journalpostId, 1));
		log.info("qopp001 har oppdatert journalpost med journalpostId=" + journalpostId + " i Joark.");
	}
	
	private void validateOppgaveTypeAndArkivsystem(no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave opprettOppgave) {
		if (!BEHANDLE_RETURPOST.equals(opprettOppgave.getOppgaveType().trim())) {
			throw new UgyldigInputverdiException("input.oppgavetype må være BEHANDLE_RETURPOST. Fikk: " + opprettOppgave.getOppgaveType());
		}
		
		if (!ARKIVSYSTEM_JOARK.equals(opprettOppgave.getArkivSystem().trim())) {
			throw new UgyldigInputverdiException("input.arkivsystem må være JOARK. Fikk: " + opprettOppgave.getArkivSystem());
		}
	}
	//TODO: Sette korrekte verdier!
	private OpprettOppgaveRequestTo mapToOpprettOppgaveRequestTo(HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo, OpprettOppgave opprettOppgave) {
		return OpprettOppgaveRequestTo.builder()
				.oppgavetype("JFR")//opprettOppgave.getOppgaveType())
				.fagomrade(hentJournalpostInfoResponseTo.getFagomrade())
				.prioritetkode("LAV")
				.beskrivelse("TestBeskrivelseDokopp")
				.journalFEnhet(hentJournalpostInfoResponseTo.getJournalfEnhet())
				.journalpostId(opprettOppgave.getArkivKode())
				.brukerId(hentJournalpostInfoResponseTo.getBrukerId())
				.brukertypeKode(BrukerType.valueOf(hentJournalpostInfoResponseTo.getBrukertype()))
				.saksnummer(hentJournalpostInfoResponseTo.getSaksnummer())
				.build();
	}
	
	
}
