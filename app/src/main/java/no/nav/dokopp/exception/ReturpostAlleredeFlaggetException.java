package no.nav.dokopp.exception;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class ReturpostAlleredeFlaggetException extends DokoppFunctionalException {
	public ReturpostAlleredeFlaggetException(String message) {
		super(message);
	}

	public ReturpostAlleredeFlaggetException(String message, Throwable cause) {
		super(message, cause);
	}
}
