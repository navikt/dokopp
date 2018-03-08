package no.nav.dokopp;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import no.nav.dokopp.config.TomcatConfig;
import no.nav.dokopp.config.cxf.ArkiverDokumentmottakV2Config;
import no.nav.dokopp.config.fasit.ArkiverDokumentmottakV2Alias;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.NavAppCertAlias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.config.props.SrvAppserverProperties;
import no.nav.dokopp.nais.NaisContract;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.util.ValidatorFeilhaandtering;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jms.annotation.EnableJms;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@EnableJms
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
@EnableConfigurationProperties({
		DokoppProperties.class,
		SrvAppserverProperties.class,
		MqChannelAlias.class,
		MqGatewayAlias.class,
		ServiceuserAlias.class,
		ArkiverDokumentmottakV2Alias.class,
		NavAppCertAlias.class,
})
@Import({
		ArkiverDokumentmottakV2Config.class,
		TomcatConfig.class,
//		RestConfig.class,
		NaisContract.class,
		ValidatorFeilhaandtering.class,
//		Tjoark203JournalfoerForsendelse.class,
//		JournalfoerInngaaendeForsendelseRequestMapper.class,
//		Tjoark203Check.class,
		Qopp001QueueCheck.class,
		ApplicationReadyListener.class,
		Qopp001Route.class
})
@Configuration
public class ApplicationConfig {
}
