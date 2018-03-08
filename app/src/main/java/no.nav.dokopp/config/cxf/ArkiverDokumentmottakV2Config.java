package no.nav.dokopp.config.cxf;

import org.springframework.context.annotation.Configuration;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
public class ArkiverDokumentmottakV2Config extends AbstractCxfEndpointConfig {

//	private static final String NAMESPACE = "http://nav.no/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v2/";
//
//	private static final QName SERVICE_QNAME = new QName(NAMESPACE, "ArkiverDokumentmottakService_v2");
//	private static final QName PORT_QNAME = new QName(NAMESPACE, "ArkiverDokumentmottakPort_v2");
//
//	private static final String WSDL_URL = "wsdl/no/nav/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v2/arkiverdokumentmottak.wsdl";
//
//	@Bean
//	public ArkiverDokumentmottakV2 arkiverDokumentmottakPort(ArkiverDokumentmottakV2Alias arkiverDokumentmottakV2Alias, ServiceuserAlias serviceuserAlias) {
//		setWsdlUrl(WSDL_URL);
//		setServiceName(SERVICE_QNAME);
//		setEndpointName(PORT_QNAME);
//		setAdress(arkiverDokumentmottakV2Alias.getEndpointurl());
//		setReceiveTimeout(arkiverDokumentmottakV2Alias.getReadtimeoutms());
//		setConnectTimeout(arkiverDokumentmottakV2Alias.getConnecttimeoutms());
//		addFeature(new WSAddressingFeature());
//		addOutInterceptor(wss4JOutInterceptor(serviceuserAlias));
//		addHandler(new MDCUsernameTokenOutHandler());
//
//		return createPort(ArkiverDokumentmottakV2.class);
//	}
//
//	private WSS4JOutInterceptor wss4JOutInterceptor(ServiceuserAlias serviceuserAlias) {
//		Map<String, Object> properties = new HashMap<>();
//		properties.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
//		properties.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
//		properties.put(WSHandlerConstants.USER, serviceuserAlias.getUsername());
//		properties.put(WSHandlerConstants.PW_CALLBACK_REF, new SystemuserPasswordCallback(serviceuserAlias.getPassword()));
//		return new WSS4JOutInterceptor(properties);
//	}
}
