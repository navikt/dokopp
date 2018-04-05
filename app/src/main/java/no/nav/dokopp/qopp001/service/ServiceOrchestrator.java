package no.nav.dokopp.qopp001.service;

import static no.nav.dokopp.qopp001.Qopp001Route.PROPERTY_JOURNALPOST_ID;
import static no.nav.dokopp.qopp001.domain.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.qopp001.domain.DomainConstants.BEHANDLE_RETURPOST;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgave;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgaveRequestTo;
import no.nav.dokopp.qopp001.domain.BrukerType;
import no.nav.dokopp.qopp001.tjoark110.SettJournalpostAttributterRequestTo;
import no.nav.dokopp.qopp001.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.qopp001.tjoark122.HentJournalpostInfoResponseTo;
import no.nav.dokopp.qopp001.tjoark122.Tjoark122HentJournalpostInfo;
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
	
	private static final String FAGOMRADE_GOSYS = "GOSYS";
	private static final String FAGOMRADE_GSAK = "GSAK";
	
	private final OpprettOppgave opprettOppgave;
	private final Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo;
	private final Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter;
	
	@Inject
	public ServiceOrchestrator(OpprettOppgave opprettOppgave,
							   Tjoark122HentJournalpostInfo tjoark122HentJournalpostInfo,
							   Tjoark110SettJournalpostAttributter tjoark110SettJournalpostAttributter) {
		this.opprettOppgave = opprettOppgave;
		this.tjoark122HentJournalpostInfo = tjoark122HentJournalpostInfo;
		this.tjoark110SettJournalpostAttributter = tjoark110SettJournalpostAttributter;
	}
	
	@Handler
	private void orchestrate(@ExchangeProperty(PROPERTY_JOURNALPOST_ID) String journalpostId, no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave opprettOppgaveInput) {
		validateOppgaveTypeAndArkivsystem(opprettOppgaveInput);
		
		HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo = tjoark122HentJournalpostInfo.hentJournalpostInfo(journalpostId);
		log.info("Qopp001 har hentet journalpostInfo fra Joark for forespørsel med journalpostId=" + journalpostId + ".");
		
		opprettOppgave.opprettOppgave(mapToOpprettOppgaveRequestTo(hentJournalpostInfoResponseTo, opprettOppgaveInput));
		log.info("Qopp001 har opprettet oppgave i Gosys for forespørsel med journalpostId=" + journalpostId + ".");
		
		tjoark110SettJournalpostAttributter.settJournalpostAttributter(new SettJournalpostAttributterRequestTo(journalpostId, 1));
		log.info("Qopp001 har oppdatert journalpost med journalpostId=" + journalpostId + " i Joark.");
	}
	
	
	private void validateOppgaveTypeAndArkivsystem(no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave opprettOppgave) {
		if (!BEHANDLE_RETURPOST.equals(opprettOppgave.getOppgaveType().trim().toUpperCase())) {
			throw new UgyldigInputverdiException("input.oppgavetype må være \"BEHANDLE_RETURPOST\". Fikk: " + opprettOppgave.getOppgaveType());
		}
		
		if (!ARKIVSYSTEM_JOARK.equals(opprettOppgave.getArkivSystem().trim().toUpperCase())) {
			throw new UgyldigInputverdiException("input.arkivsystem må være \"JOARK\". Fikk: " + opprettOppgave.getArkivSystem());
		}
	}
	
	private OpprettOppgaveRequestTo mapToOpprettOppgaveRequestTo(HentJournalpostInfoResponseTo hentJournalpostInfoResponseTo, no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave opprettOppgaveInput) {
		return OpprettOppgaveRequestTo.builder()
				.oppgavetype(opprettOppgaveInput.getOppgaveType())
				.fagomrade(hentJournalpostInfoResponseTo.getFagomrade())
				.prioritetkode("")
				.beskrivelse("")
				.journalFEnhet(hentJournalpostInfoResponseTo.getJournalfEnhet())
				.journalpostId(opprettOppgaveInput.getArkivKode())
				.brukerId(hentJournalpostInfoResponseTo.getBrukerId())
				.brukertypeKode(BrukerType.valueOf(hentJournalpostInfoResponseTo.getBrukertype()))
				.saksnummer(settSaksnummer(hentJournalpostInfoResponseTo.getSaksnummer(), hentJournalpostInfoResponseTo.getFagomrade()))
				.build();
	}
	
	private String settSaksnummer(String saksnummer, String fagomrade) {
		if (FAGOMRADE_GOSYS.equals(fagomrade) || FAGOMRADE_GSAK.equals(fagomrade)) {
			return saksnummer;
		} else {
			return "";
		}
	}
	
	
}
