package no.nav.dokopp.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class AvsluttBehandlingException extends DokoppFunctionalException {
	public AvsluttBehandlingException(String message, String shortMessage) {
		super(message, shortMessage);
	}
	
	public AvsluttBehandlingException(String message, String shortMessage, Throwable cause) {
		super(message,shortMessage, cause);
	}
}
