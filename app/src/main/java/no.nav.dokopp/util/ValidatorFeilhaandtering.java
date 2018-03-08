package no.nav.dokopp.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Service
@Slf4j
public class ValidatorFeilhaandtering {

//	private final Feilhaandtering feilhaandteringA;
//
//	@Inject
//	public ValidatorFeilhaandtering(Tmot501Behandlingslager tmot501Behandlingslager) {
//		this.feilhaandteringA = new Feilhaandtering(tmot501Behandlingslager);
//	}
//
//	// Please note that this is called from a doTry doCatch block and exception thrown from this or its delegates are NOT propagated
//	// to the onException handler in the main route!
//	// Therefore we need to handle logging and metrics here.
//	@Handler
//	public void handleException(@ExchangeProperty(PROPERTY_FORSENDELSE_MOTTAK_ID) String forsendelseMottakId, @Body Exchange exchange) throws DokmotSedFunctionalException {
//		String errors = null;
//		if (exchange != null) {
//			Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
//			if (exception instanceof SchemaValidationException) {
//				// Handle errors related to validation
//				SchemaValidationException schemaValidationException = (SchemaValidationException) exception;
//				errors = schemaValidationException.getErrors().stream().map(SAXException::getMessage).collect(Collectors.joining(",\n"));
//				errors += schemaValidationException.getFatalErrors().stream().map(SAXException::getMessage).collect(Collectors.joining(",\n"));
//			} else if (exception instanceof TransformerException) {
//				// Handle errors related to XSLT
//				errors = ((TransformerException) exception).getMessageAndLocation();
//			} else if (exception != null) {
//				errors = exception.getMessage();
//			}
//
//		}
//
//		String feilbeskrivelse = "Påkrevde parametere er ikke satt og/eller har feil format. forsendelseMottaksId=" + forsendelseMottakId
//				+ ".\n" +
//				(errors == null ? "" : errors);
//		log.warn(feilbeskrivelse);
//		feilhaandteringA.feilhaandteringNoThrow(forsendelseMottakId, feilbeskrivelse);
//	}
}
