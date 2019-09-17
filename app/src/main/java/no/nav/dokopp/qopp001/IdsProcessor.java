package no.nav.dokopp.qopp001;

import no.nav.dokopp.constants.MdcConstants;
import no.nav.modig.common.MDCOperations;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * @author Erik Bråten, Visma Consulting.
 */
public class IdsProcessor implements Processor {

	@Override
	public void process(Exchange exchange) {
		addCallIdToMdc(exchange);
	}

	private void addCallIdToMdc(Exchange exchange) {
		String callId = exchange.getIn().getHeader(MdcConstants.NAV_CALL_ID, String.class);
		if (callId == null || callId.trim().isEmpty()) {
			callId = UUID.randomUUID().toString();
		}
		MDC.put(MdcConstants.NAV_CALL_ID, callId);
		MDC.put(MDCOperations.MDC_CALL_ID, callId); // ref. MDCUsernameTokenOutHandler
	}
}
