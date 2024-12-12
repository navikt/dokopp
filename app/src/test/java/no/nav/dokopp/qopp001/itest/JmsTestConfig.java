package no.nav.dokopp.qopp001.itest;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

@Profile("itest")
@Configuration
public class JmsTestConfig {
	public static final String QOPP001_BQ = "qopp001Bq";

	@Bean
	public Queue qopp001(@Value("${dokopp.qopp001.opprett.oppgave.queuename}") String qopp001QueueName) {
		return new ActiveMQQueue(qopp001QueueName);
	}

	@Bean
	public Queue qopp001FunksjonellFeil(@Value("${dokopp.qopp001.funksjonell.feil.queuename}") String qopp001FunksjonellFeil) {
		return new ActiveMQQueue(qopp001FunksjonellFeil);
	}

	@Bean
	public Queue backoutQueue() {
		return new ActiveMQQueue(QOPP001_BQ);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public EmbeddedActiveMQ embeddedActiveMQ() {
		EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
		embeddedActiveMQ.setConfigResourcePath("artemis-server.xml");
		return embeddedActiveMQ;
	}

	@Bean
	@DependsOn("embeddedActiveMQ")
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://0");
		JmsPoolConnectionFactory pooledFactory = new JmsPoolConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}

}
