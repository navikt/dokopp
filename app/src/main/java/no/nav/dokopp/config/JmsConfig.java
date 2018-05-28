
package no.nav.dokopp.config;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.wmq.WMQConstants;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.props.SrvAppserverProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile("nais")
public class JmsConfig {
	
	private static final int UTF_8_WITH_PUA = 1208;
	
	@Bean
	public Queue qopp001(@Value("${DOKOPP_QOPP001_OPPRETT_OPPGAVE_QUEUENAME}") String qopp001QueueName) throws JMSException {
		return new MQQueue(qopp001QueueName);
	}
	
	@Bean
	public Queue qopp001FunksjonellFeil(@Value("${DOKOPP_QOPP001_FUNKSJONELL_FEIL_QUEUENAME}") String qopp001FunksjonellFeil) throws JMSException {
		return new MQQueue(qopp001FunksjonellFeil);
	}
	
	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayAlias mqGatewayAlias,
												  final MqChannelAlias mqChannelAlias,
												  final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, mqChannelAlias, srvAppserverProperties);
	}
	
	private UserCredentialsConnectionFactoryAdapter createConnectionFactory(final MqGatewayAlias mqGatewayAlias,
																			final MqChannelAlias mqChannelAlias,
																			final SrvAppserverProperties srvAppserverProperties) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setChannel(mqChannelAlias.getName());
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE);
		connectionFactory.setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(srvAppserverProperties.getUsername());
		adapter.setPassword(srvAppserverProperties.getPassword());
		return adapter;
	}
}
