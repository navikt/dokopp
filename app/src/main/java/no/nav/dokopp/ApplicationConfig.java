package no.nav.dokopp;

import no.nav.dokopp.config.DokoppProperties;
import no.nav.dokopp.config.cxf.ArkiverDokumentproduksjonV1EndpointConfig;
import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.dokopp.consumer.azure.AzureProperties;
import no.nav.dokopp.consumer.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.nais.NaisContract;
import no.nav.dokopp.nais.checks.FunctionalBoqCheck;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.qopp001.Qopp001Service;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.retry.annotation.EnableRetry;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@EnableRetry
@EnableJms
@EnableConfigurationProperties({
		MqChannelAlias.class,
		MqGatewayAlias.class,
		ServiceuserAlias.class,
		ArkiverDokumentproduksjonV1Alias.class,
		DokoppProperties.class,
		AzureProperties.class

})
@Import({
		NaisContract.class,
		ArkiverDokumentproduksjonV1EndpointConfig.class,
		Tjoark110SettJournalpostAttributter.class,
		Qopp001Service.class,
		Tjoark110Check.class,
		Qopp001QueueCheck.class,
		FunctionalBoqCheck.class,
		Qopp001Route.class
})
@Configuration
public class ApplicationConfig {
}
