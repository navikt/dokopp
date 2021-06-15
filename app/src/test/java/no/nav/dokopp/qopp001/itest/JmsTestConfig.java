package no.nav.dokopp.qopp001.itest;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Profile("itest")
@Configuration
public class JmsTestConfig {

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
		return new ActiveMQQueue("ActiveMQ.DLQ");
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public BrokerService broker() throws Exception {
		BrokerService service = new BrokerService();
		service.setPersistent(false);

		return service;
	}

	@Bean
	public ConnectionFactory activemqConnectionFactory() {
		ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("vm://localhost?create=false");
		RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
		redeliveryPolicy.setMaximumRedeliveries(0);
		activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);

		PooledConnectionFactory pooledFactory = new PooledConnectionFactory();
		pooledFactory.setConnectionFactory(activeMQConnectionFactory);
		pooledFactory.setMaxConnections(1);
		return pooledFactory;
	}
}
