package no.nav.dokopp.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class DokoppFunctionalException extends RuntimeException {

	public DokoppFunctionalException(String message) {
		super(message);
	}

	public DokoppFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
