package no.nav.dokopp.config;

import org.apache.catalina.Context;
import org.apache.catalina.realm.JAASRealm;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Profile("remote")
@Configuration
public class TomcatConfig {

	private static final String NAV_SAML = "NavSAML";
	private static final String JAAS_LOGIN_CONFIG = "/login.config";

	@Bean
	public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainer() {
		TomcatEmbeddedServletContainerFactory servletContainerFactory = new TomcatEmbeddedServletContainerFactory();
		servletContainerFactory.addContextCustomizers((TomcatContextCustomizer) this::navSamlJaasRealm);
		return servletContainerFactory;
	}

	private void navSamlJaasRealm(Context context) {
		JAASRealm realm = new JAASRealm();
		realm.setAppName(NAV_SAML);
		realm.setConfigFile(new ClassPathResource(JAAS_LOGIN_CONFIG).getPath());
		context.setRealm(realm);
	}
}
