package no.nav.dokopp;

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
import no.nav.dokopp.config.props.SrvAppserverProperties;
import no.nav.dokopp.nais.NaisContract;
import no.nav.dokopp.nais.checks.BehandleOppgaveV1Check;
import no.nav.dokopp.nais.checks.FunctionalBoqCheck;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.nais.checks.Tjoark122Check;
import no.nav.dokopp.qopp001.Qopp001Route;
import no.nav.dokopp.qopp001.Qopp001Service;
import no.nav.dokopp.consumer.behandleOppgaveV1.OpprettOppgaveGosys;
import no.nav.dokopp.consumer.tjoark110.Tjoark110SettJournalpostAttributter;
import no.nav.dokopp.consumer.tjoark122.Tjoark122HentJournalpostInfo;
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
		SrvAppserverProperties.class,
		MqChannelAlias.class,
		MqGatewayAlias.class,
		ServiceuserAlias.class,
		BehandleOppgaveV1Alias.class,
		ArkiverDokumentproduksjonV1Alias.class,
		DokumentproduksjonInfoV1Alias.class,
		NavAppCertAlias.class
})
@Import({
		TomcatConfig.class,
		NaisContract.class,
		DokumentproduksjonInfoV1EndpointConfig.class,
		ArkiverDokumentproduksjonV1EndpointConfig.class,
		BehandleOppgaveV1EndpointConfig.class,
		Tjoark110SettJournalpostAttributter.class,
		Tjoark122HentJournalpostInfo.class,
		OpprettOppgaveGosys.class,
		Qopp001Service.class,
		Tjoark110Check.class,
		Tjoark122Check.class,
		BehandleOppgaveV1Check.class,
		Qopp001QueueCheck.class,
		FunctionalBoqCheck.class,
		Qopp001Route.class
})
@Configuration
public class ApplicationConfig {
}
