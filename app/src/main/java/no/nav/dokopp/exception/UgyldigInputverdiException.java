package no.nav.dokopp.exception;

public class UgyldigInputverdiException extends DokoppFunctionalException {

	public UgyldigInputverdiException(String message) {
		super(message);
	}
	
	public UgyldigInputverdiException(String message, Throwable cause) {
		super(message, cause);
	}
}
