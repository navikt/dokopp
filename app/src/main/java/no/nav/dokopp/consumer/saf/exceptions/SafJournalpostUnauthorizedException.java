package no.nav.dokopp.consumer.saf.exceptions;

import no.nav.dokopp.exception.DokoppTechnicalException;

public class SafJournalpostUnauthorizedException extends DokoppTechnicalException {
	public SafJournalpostUnauthorizedException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafJournalpostUnauthorizedException(String message) {
		super(message);
	}
}
