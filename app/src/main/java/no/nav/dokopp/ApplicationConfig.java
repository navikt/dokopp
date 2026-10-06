package no.nav.dokopp;

import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.dokopp.config.nais.NaisProperties;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.qopp001.Qopp001Service;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@EnableJms
@EnableConfigurationProperties({
		MqChannelAlias.class,
		MqGatewayAlias.class,
		ServiceuserAlias.class,
		DokoppProperties.class,
		NaisProperties.class

})
@Import({
		Qopp001Service.class,
		Qopp001Route.class,
})
@Configuration
public class ApplicationConfig {
}
