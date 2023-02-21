package no.nav.dokopp.qopp001;

import no.nav.dokopp.constants.HeaderConstants;
import no.nav.dokopp.util.MDCOperations;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.MDC;

import java.util.UUID;

public class IdsProcessor implements Processor {

	@Override
	public void process(Exchange exchange) {
		addCallIdToMdc(exchange);
	}

	private void addCallIdToMdc(Exchange exchange) {
		String callId = exchange.getIn().getHeader(HeaderConstants.NAV_CALL_ID, String.class);
		if (callId == null || callId.trim().isEmpty()) {
			callId = UUID.randomUUID().toString();
		}
		MDC.put(HeaderConstants.NAV_CALL_ID, callId);
		MDC.put(MDCOperations.MDC_CALL_ID, callId); // ref. MDCUsernameTokenOutHandler
	}
}
