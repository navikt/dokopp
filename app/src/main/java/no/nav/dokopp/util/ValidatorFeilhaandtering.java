package no.nav.dokopp.util;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.exception.ValideringFeiletException;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.processor.validation.SchemaValidationException;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Service
@Slf4j
public class ValidatorFeilhaandtering {
	
	@Handler
	public void handleException(@Body Exchange exchange) {
		String errors = null;
		if (exchange != null) {
			Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
			if (exception instanceof SchemaValidationException) {
				// Handle errors related to validation
				SchemaValidationException schemaValidationException = (SchemaValidationException) exception;
				errors = schemaValidationException.getErrors()
						.stream()
						.map(SAXException::getMessage)
						.collect(Collectors.joining(",\n"));
				errors += schemaValidationException.getFatalErrors()
						.stream()
						.map(SAXException::getMessage)
						.collect(Collectors.joining(",\n"));
			} else {
				if (exception != null) {
					errors = exception.getMessage();
				}
			}
		}
		
		String feilbeskrivelse = "Påkrevde parametere er ikke satt og/eller har feil format." + "\n" +
				(errors == null ? "" : errors);
		log.warn(feilbeskrivelse);
		
		throw new ValideringFeiletException(feilbeskrivelse);
	}
}
