package no.nav.dokopp.consumer.oppgave;

import lombok.Builder;
import lombok.Value;

/**
 * @author Erik Bråten, Visma Consulting.
 */
@Value
@Builder
public class OpprettOppgaveRequest {

	private final String tildeltEnhetsnr;
	private final String opprettetAvEnhetsnr;
	private final String aktoerId;
	private final String journalpostId;
	private final String journalpostkilde;
	private final String behandlesAvApplikasjon;
	private final String saksreferanse;
	private final String orgnr;
	private final String bnr;
	private final String samhandlernr;
	private final String tilordnetRessurs;
	private final String beskrivelse;
	private final String temagruppe;
	private final String tema;
	private final String behandlingstema;
	private final String oppgavetype;
	private final String behandlingstype;
	private final Integer mappeId;
	private final String aktivDato;
	private final String fristFerdigstillelse;
	private final String prioritet;

}
