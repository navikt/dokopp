package no.nav.dokopp.config.cxf;

import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonServiceV1.ArkiverDokumentproduksjonPortV1;
import static no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonServiceV1.SERVICE;

@Configuration
public class ArkiverDokumentproduksjonV1EndpointConfig extends AbstractCxfEndpointConfig {

	private static final String WSDL_URL = "wsdl/arkiverdokumentproduksjon.wsdl";

	@Bean
	public ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonPort(ArkiverDokumentproduksjonV1Alias arkiverDokumentproduksjonV1Alias, ServiceuserAlias serviceuserAlias) {
		setWsdlUrl(WSDL_URL);
		setServiceName(SERVICE);
		setEndpointName(ArkiverDokumentproduksjonPortV1);
		setAdress(arkiverDokumentproduksjonV1Alias.getEndpointurl());
		setReceiveTimeout(arkiverDokumentproduksjonV1Alias.getReadtimeoutms());
		setConnectTimeout(arkiverDokumentproduksjonV1Alias.getConnecttimeoutms());
		addFeature(new WSAddressingFeature());
		addOutInterceptor(wss4JOutInterceptor(serviceuserAlias));
		addHandler(new MDCUsernameTokenOutHandler());

		return createPort(ArkiverDokumentproduksjonV1.class);
	}

	private WSS4JOutInterceptor wss4JOutInterceptor(ServiceuserAlias serviceuserAlias) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
		properties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
		properties.put(WSHandlerConstants.USER, serviceuserAlias.getUsername());
		properties.put(WSHandlerConstants.PW_CALLBACK_REF, new SystemuserPasswordCallback(serviceuserAlias.getPassword()));
		return new WSS4JOutInterceptor(properties);
	}
}
