package no.nav.dokopp.exception;

/**
 * Avslutter all behandling og kaster meldingen.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AvsluttBehandlingOgKastMeldingException extends DokoppFunctionalException {

	public AvsluttBehandlingOgKastMeldingException(String message) {
		super(message);
	}
}
