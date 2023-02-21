package no.nav.dokopp.consumer.saf.exceptions;

import no.nav.dokopp.exception.DokoppTechnicalException;

public class SafJournalpostQueryTechnicalException extends DokoppTechnicalException {

	public SafJournalpostQueryTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafJournalpostQueryTechnicalException(String message) {
		super(message);
	}
}
