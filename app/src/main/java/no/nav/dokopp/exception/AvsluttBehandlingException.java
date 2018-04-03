package no.nav.dokopp.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class AvsluttBehandlingException extends DokoppFunctionalException {
	public AvsluttBehandlingException(String message) {
		super(message);
	}
	
	public AvsluttBehandlingException(String message, Throwable cause) {
		super(message, cause);
	}
}
