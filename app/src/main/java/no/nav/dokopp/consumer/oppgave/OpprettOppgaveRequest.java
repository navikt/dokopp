package no.nav.dokopp.consumer.oppgave;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class OpprettOppgaveRequest {

	String tildeltEnhetsnr;
	String opprettetAvEnhetsnr;
	String aktoerId;
	String journalpostId;
	String journalpostkilde;
	String behandlesAvApplikasjon;
	String saksreferanse;
	String orgnr;
	String bnr;
	String samhandlernr;
	String tilordnetRessurs;
	String beskrivelse;
	String temagruppe;
	String tema;
	String behandlingstema;
	String oppgavetype;
	String behandlingstype;
	Integer mappeId;
	String aktivDato;
	String fristFerdigstillelse;
	String prioritet;

}
