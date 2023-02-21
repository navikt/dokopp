package no.nav.dokopp.qopp001;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.dokopp.constants.HeaderConstants.NAV_CALL_ID;
import static no.nav.dokopp.util.MDCOperations.MDC_CALL_ID;

public class IdsProcessor implements Processor {

	@Override
	public void process(Exchange exchange) {
		addCallIdToMdc(exchange);
	}

	private void addCallIdToMdc(Exchange exchange) {
		String callId = exchange.getIn().getHeader(NAV_CALL_ID, String.class);
		if (callId == null || callId.trim().isEmpty()) {
			callId = UUID.randomUUID().toString();
		}
		MDC.put(NAV_CALL_ID, callId);
		MDC.put(MDC_CALL_ID, callId); // ref. MDCUsernameTokenOutHandler
	}
}
