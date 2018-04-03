package no.nav.dokopp.config.cxf;

import no.nav.dokopp.config.fasit.BehandleOppgaveV1Alias;
import no.nav.dokopp.config.fasit.NavAppCertAlias;
import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.binding.BehandleOppgaveV1;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
//TODO Add secutity properties to message
public class BehandleOppgaveV1EndpointConfig extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/behandleOppgave/v1/Binding";
	
	private static final QName BEHANDLEOPPGAVE_V3_PORT_QNAME = new QName(NAMESPACE, "BehandleOppgave_v1Port");
	private static final QName BEHANDLEOPPGAVE__V3_SERVICE_QNAME = new QName(NAMESPACE, "BehandleOppgave_v1");
	
	public static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/behandleOppgave/v1/Binding.wsdl";
	
	@Bean
	public BehandleOppgaveV1 behandleOppgaveV1(BehandleOppgaveV1Alias behandleOppgaveV1Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();
		
		setWsdlUrl(WSDL_URL);
		setEndpointName(BEHANDLEOPPGAVE_V3_PORT_QNAME);
		setServiceName(BEHANDLEOPPGAVE__V3_SERVICE_QNAME);
		setAdress(behandleOppgaveV1Alias.getEndpointurl());
		setReceiveTimeout(behandleOppgaveV1Alias.getReadtimeoutms());
		setConnectTimeout(behandleOppgaveV1Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(BehandleOppgaveV1.class);
	}
}
