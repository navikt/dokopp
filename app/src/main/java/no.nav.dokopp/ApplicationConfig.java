package no.nav.dokopp;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import no.nav.dokopp.config.TomcatConfig;
import no.nav.dokopp.config.cxf.ArkiverDokumentproduksjonV1Config;
import no.nav.dokopp.config.cxf.DokumentproduksjonInfoV1Config;
import no.nav.dokopp.config.cxf.OppgavebehandlingV3EndpointConfig;
import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.config.fasit.DokumentproduksjonInfoV1Alias;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.NavAppCertAlias;
import no.nav.dokopp.config.fasit.OppgavebehandlingV3Alias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.config.props.SrvAppserverProperties;
import no.nav.dokopp.nais.NaisContract;
import no.nav.dokopp.nais.checks.OppgavebehandlingV3Check;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.nais.checks.Tjoark122Check;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.qopp001.oppgavebehandlingV3.OpprettOppgave;
import no.nav.dokopp.qopp001.oppgavebehandlingV3.OpprettOppgaveRequestMapper;
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
		OppgavebehandlingV3Alias.class,
		ArkiverDokumentproduksjonV1Alias.class,
		DokumentproduksjonInfoV1Alias.class,
		NavAppCertAlias.class,
})
@Import({
		DokumentproduksjonInfoV1Config.class,
		OppgavebehandlingV3EndpointConfig.class,
		TomcatConfig.class,
		NaisContract.class,
		ValidatorFeilhaandtering.class,
		ArkiverDokumentproduksjonV1Config.class,
		OpprettOppgave.class,
//		JournalfoerInngaaendeForsendelseRequestMapper.class,
		Tjoark110Check.class,
		Tjoark122Check.class,
		OpprettOppgaveRequestMapper.class,
		OppgavebehandlingV3Check.class,
		Qopp001QueueCheck.class,
		ApplicationReadyListener.class,
		Qopp001Route.class
})
@Configuration
public class ApplicationConfig {
}
