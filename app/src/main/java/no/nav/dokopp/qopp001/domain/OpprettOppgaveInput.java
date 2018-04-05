package no.nav.dokopp.qopp001.domain;

import lombok.AllArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@AllArgsConstructor
public class OpprettOppgaveInput {
	
	private final String oppgaveType;
	private final String arkivSystem;
	private final String arkivKode;
}