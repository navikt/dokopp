package no.nav.dokopp.exception;

/**
 * @author Jarl Øystein Samseth, Visma Consulting
 */
public class DokoppFunctionalException extends RuntimeException {
	
	private String shortMessage;
	
	public DokoppFunctionalException(String message, String shortMessage) {
		super(message);
		this.shortMessage=shortMessage;
	}
	
	public DokoppFunctionalException(String message, String shortMessage, Throwable cause) {
		super(message, cause);
		this.shortMessage=shortMessage;
	}
	
	
	public String getShortMessage(){
		return shortMessage;
	}
}
