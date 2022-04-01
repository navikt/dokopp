package no.nav.dokopp.config.cxf;

import no.nav.dokopp.util.MDCOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.HashSet;
import java.util.Set;

import static no.nav.dokopp.ApplicationConstants.APP_ID;
import static no.nav.dokopp.ApplicationConstants.DEFAULT_APP_ID;
import static no.nav.dokopp.util.MDCOperations.MDC_CALL_ID;
import static no.nav.dokopp.util.MDCOperations.MDC_CONSUMER_ID;
import static no.nav.dokopp.util.MDCOperations.MDC_USER_ID;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Soap-handler that appends CallId and AppId to SOAP Header for outgoing requests
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class MDCUsernameTokenOutHandler implements SOAPHandler<SOAPMessageContext> {
	
	private static final Logger log = LoggerFactory.getLogger(MDCUsernameTokenOutHandler.class.getName());

	public static final String URI_NO_NAV_APPLIKASJONSRAMMEVERK = "uri:no.nav.applikasjonsrammeverk";
	private static final QName APP_ID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, APP_ID);
	private static final QName CALLID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_CALL_ID);
	private static final QName CONSUMER_ID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_CONSUMER_ID);
	private static final QName USER_ID_QNAME = new QName(URI_NO_NAV_APPLIKASJONSRAMMEVERK, MDC_USER_ID);
	
	@Override
	public boolean handleMessage(SOAPMessageContext context) {
		Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		
		if (outbound) {
			String callId = MDCOperations.getFromMDC(MDC_CALL_ID);
			if (isEmpty(callId)) {
				log.debug("Callid is null/empty, generating");
				callId = MDCOperations.generateCallId();
			}
			appendToSoapHeader(context, CALLID_QNAME, callId);
			
			String appId = MDCOperations.getFromMDC(APP_ID);
			if (isEmpty(appId)) {
				log.debug("appId is null, using default");
				appId = DEFAULT_APP_ID;
			}
			appendToSoapHeader(context, APP_ID_QNAME, appId);
			
			String consumerId = MDCOperations.getFromMDC(MDC_CONSUMER_ID);
			appendToSoapHeader(context, CONSUMER_ID_QNAME, consumerId);
			
			String userId = MDCOperations.getFromMDC(MDC_USER_ID);
			appendToSoapHeader(context, USER_ID_QNAME, userId);
		}
		return true;
	}
	
	private void appendToSoapHeader(SOAPMessageContext context, QName qName, String value) {
		try {
			SOAPHeader header = context.getMessage().getSOAPPart().getEnvelope().getHeader();
			SOAPElement element = header.addChildElement(qName);
			element.setValue(value == null ? "" : value);
		} catch (SOAPException e) {
			log.error(e.getMessage());
			throw new ProtocolException(e);
		}
	}
	
	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}
	
	@Override
	public void close(MessageContext context) {
		//Nothing to close
	}
	
	@Override
	public Set<QName> getHeaders() {
		return new HashSet<>() {
			{
				add(APP_ID_QNAME);
				add(CALLID_QNAME);
				add(CONSUMER_ID_QNAME);
				add(USER_ID_QNAME);
			}
		};
	}
	
}
