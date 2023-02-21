package no.nav.dokopp.exception;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokoppTechnicalException extends RuntimeException {

	public DokoppTechnicalException(String message) {
		super(message);
	}

	public DokoppTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}
}
