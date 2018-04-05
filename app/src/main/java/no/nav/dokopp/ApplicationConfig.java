package no.nav.dokopp;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;
import no.nav.dokopp.config.TomcatConfig;
import no.nav.dokopp.config.cxf.ArkiverDokumentproduksjonV1EndpointConfig;
import no.nav.dokopp.config.cxf.BehandleOppgaveV1EndpointConfig;
import no.nav.dokopp.config.cxf.DokumentproduksjonInfoV1EndpointConfig;
import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.config.fasit.BehandleOppgaveV1Alias;
import no.nav.dokopp.config.fasit.DokumentproduksjonInfoV1Alias;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.NavAppCertAlias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import no.nav.dokopp.config.props.DokoppProperties;
import no.nav.dokopp.config.props.SrvAppserverProperties;
import no.nav.dokopp.nais.NaisContract;
import no.nav.dokopp.nais.checks.BehandleOppgaveV1Check;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.nais.checks.Tjoark122Check;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.qopp001.behandleOppgaveV1.OpprettOppgave;
import no.nav.dokopp.qopp001.service.ServiceOrchestrator;
import no.nav.dokopp.qopp001.support.OpprettOppgaveInputMapper;
import no.nav.dokopp.qopp001.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.qopp001.tjoark122.Tjoark122HentJournalpostInfo;
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
		BehandleOppgaveV1Alias.class,
		ArkiverDokumentproduksjonV1Alias.class,
		DokumentproduksjonInfoV1Alias.class,
		NavAppCertAlias.class,
})
@Import({
		TomcatConfig.class,
		NaisContract.class,
		DokumentproduksjonInfoV1EndpointConfig.class,
		ArkiverDokumentproduksjonV1EndpointConfig.class,
		BehandleOppgaveV1EndpointConfig.class,
		Tjoark110SettJournalpostAttributter.class,
		Tjoark122HentJournalpostInfo.class,
		OpprettOppgave.class,
		ServiceOrchestrator.class,
		ValidatorFeilhaandtering.class,
		OpprettOppgaveInputMapper.class,
		Tjoark110Check.class,
		Tjoark122Check.class,
		BehandleOppgaveV1Check.class,
		Qopp001QueueCheck.class,
		ApplicationReadyListener.class,
		Qopp001Route.class
})
@Configuration
public class ApplicationConfig {
}
