package no.nav.dokopp.config.cxf;

import no.nav.dokopp.config.fasit.NavAppCertAlias;
import no.nav.dokopp.config.fasit.OppgavebehandlingV3Alias;
import no.nav.modig.security.ws.SystemSAMLOutInterceptor;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.binding.OppgavebehandlingV3;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.springframework.context.annotation.Bean;

import javax.xml.namespace.QName;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
//TODO Add secutity properties to message
public class OppgavebehandlingV3EndpointConfig extends AbstractCxfEndpointConfig {
	private static final String NAMESPACE = "http://nav.no/tjeneste/virksomhet/organisasjon/v4/Binding";
	
	private static final QName OPPGAVEBEHANDLING_V3_PORT_QNAME = new QName(NAMESPACE, "oppgavebehandling_v3Port");
	private static final QName OPPGAVEBEHANDLING_V3_SERVICE_QNAME = new QName(NAMESPACE, "oppgavebehandling_v3");
	
	public static final String WSDL_URL = "wsdl/no/nav/tjeneste/virksomhet/oppgavebehandling/v3/oppgavebehandling.wsdl";
	
	@Bean
	public OppgavebehandlingV3 oppgavebehandlingV3(OppgavebehandlingV3Alias oppgavebehandlingV3Alias, NavAppCertAlias navAppCertAlias) {
		navAppCertAlias.postConstruct();
		
		setWsdlUrl(WSDL_URL);
		setEndpointName(OPPGAVEBEHANDLING_V3_PORT_QNAME);
		setServiceName(OPPGAVEBEHANDLING_V3_SERVICE_QNAME);
		setAdress(oppgavebehandlingV3Alias.getEndpointurl());
		setReceiveTimeout(oppgavebehandlingV3Alias.getReadtimeoutms());
		setConnectTimeout(oppgavebehandlingV3Alias.getConnecttimeoutms());
		addOutInterceptor(new SystemSAMLOutInterceptor());
		addFeature(new WSAddressingFeature());
		return createPort(OppgavebehandlingV3.class);
	}
}
