package no.nav.dokopp.qopp001.itest;

import com.ibm.mq.jms.MQQueue;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Profile("itest")
@Configuration
public class JmsTestConfig {
	public Queue qopp001(@Value("${DOKOPP_OPPRETT_OPPGAVE_QUEUENAME}") String qopp001QueueName) {
		return new ActiveMQQueue(qopp001QueueName);
	}
	
	public Queue functionalBOQ(@Value("${DOKOPP_FUNKSJONELL_BOQ_QUEUENAME}") String functionalBOQName) throws JMSException {
		return new MQQueue(functionalBOQName);
	}
	
	@Bean
	public Queue backoutQueue() {
		return new ActiveMQQueue("ActiveMQ.DLQ");
	}
	
	@Bean(initMethod = "start", destroyMethod = "stop")
	public BrokerService broker() {
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
		return activeMQConnectionFactory;
	}
}
