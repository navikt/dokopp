package no.nav.dokopp.qopp001.support;

import static no.nav.dokopp.qopp001.domain.DomainConstants.ARKIVSYSTEM_JOARK;
import static no.nav.dokopp.qopp001.domain.DomainConstants.BEHANDLE_RETURPOST;

import no.nav.dokopp.exception.UgyldigInputverdiException;
import no.nav.dokopp.qopp001.domain.OpprettOppgaveInput;
import no.nav.opprettoppgave.tjenestespesifikasjon.v1.xml.jaxb2.gen.OpprettOppgave;
import org.apache.camel.Handler;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class OpprettOppgaveInputMapper {
	
	@Handler
	public OpprettOppgaveInput validateInputAndMap(OpprettOppgave opprettOppgave) throws Exception {
		validateInput(opprettOppgave);
		
		return new OpprettOppgaveInput(opprettOppgave.getOppgaveType(), opprettOppgave.getArkivSystem(), opprettOppgave
				.getArkivKode());
	}
	
	private void validateInput(OpprettOppgave opprettOppgave) {
		if (opprettOppgave == null) {
			throw new UgyldigInputverdiException("input kan ikke være null");
		}
		if (isNullOrEmpty(opprettOppgave.getOppgaveType())) {
			throw new UgyldigInputverdiException("input.oppgavetype er null eller tom");
		}
		if (isNullOrEmpty(opprettOppgave.getArkivSystem())) {
			throw new UgyldigInputverdiException("input.arkivSystem er null eller tom");
		}
		if (isNullOrEmpty(opprettOppgave.getArkivKode())) {
			throw new UgyldigInputverdiException("input.arkivKode er null eller tom");
		}
		
		validateOppgaveTypeAndArkivsystem(opprettOppgave);
	}
	
	private void validateOppgaveTypeAndArkivsystem(OpprettOppgave opprettOppgave) {
		if (!BEHANDLE_RETURPOST.equals(opprettOppgave.getOppgaveType().trim().toUpperCase())) {
			throw new UgyldigInputverdiException("input.oppgavetype må være \"BEHANDLE_RETURPOST\". Fikk: " + opprettOppgave.getOppgaveType());
		}
		
		if (!ARKIVSYSTEM_JOARK.equals(opprettOppgave.getArkivSystem().trim().toUpperCase())) {
			throw new UgyldigInputverdiException("input.arkivsystem må være \"JOARK\". Fikk: " + opprettOppgave.getArkivSystem());
		}
	}
	
	private Boolean isNullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}
}
