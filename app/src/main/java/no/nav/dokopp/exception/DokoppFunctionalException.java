package no.nav.dokopp.exception;

public class DokoppFunctionalException extends RuntimeException {

	public DokoppFunctionalException(String message) {
		super(message);
	}

	public DokoppFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}
}
