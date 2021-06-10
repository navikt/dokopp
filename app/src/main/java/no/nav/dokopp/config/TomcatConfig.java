package no.nav.dokopp.config;

import org.apache.catalina.Context;
import org.apache.catalina.realm.JAASRealm;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Profile("nais")
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<ConfigurableTomcatWebServerFactory> {

    private static final String NAV_SAML = "NavSAML";
    private static final String JAAS_LOGIN_CONFIG = "/login.config";

    private void navSamlJaasRealm(Context context) {
        JAASRealm realm = new JAASRealm();
        realm.setAppName(NAV_SAML);
        realm.setConfigFile(new ClassPathResource(JAAS_LOGIN_CONFIG).getPath());
        context.setRealm(realm);
    }

    @Override
    public void customize(ConfigurableTomcatWebServerFactory factory) {
        factory.addContextCustomizers(this::navSamlJaasRealm);
    }
}
