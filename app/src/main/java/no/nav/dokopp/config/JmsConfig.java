
package no.nav.dokopp.config;

import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueue;
import com.ibm.msg.client.jms.JmsConstants;
import no.nav.dokopp.config.fasit.MqChannelAlias;
import no.nav.dokopp.config.fasit.MqGatewayAlias;
import no.nav.dokopp.config.fasit.ServiceuserAlias;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.net.ssl.SSLSocketFactory;

import static com.ibm.mq.constants.CMQC.MQENC_NATIVE;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_CHARACTER_SET;
import static com.ibm.msg.client.jms.JmsConstants.JMS_IBM_ENCODING;
import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@Profile("nais")
public class JmsConfig {

	private static final int UTF_8_WITH_PUA = 1208;
	private static final String ANY_TLS13_OR_HIGHER = "*TLS13ORHIGHER";

	@Bean
	public Queue qopp001(@Value("${dokopp.qopp001.opprett.oppgave.queuename}") String qopp001QueueName) throws JMSException {
		return new MQQueue(qopp001QueueName);
	}

	@Bean
	public Queue qopp001FunksjonellFeil(@Value("${dokopp.qopp001.funksjonell.feil.queuename}") String qopp001FunksjonellFeil) throws JMSException {
		return new MQQueue(qopp001FunksjonellFeil);
	}

	@Bean
	public ConnectionFactory wmqConnectionFactory(final MqGatewayAlias mqGatewayAlias,
												  final MqChannelAlias mqChannelAlias,
												  final ServiceuserAlias serviceuserAlias) throws JMSException {
		return createConnectionFactory(mqGatewayAlias, mqChannelAlias, serviceuserAlias);
	}

	private PooledConnectionFactory createConnectionFactory(final MqGatewayAlias mqGatewayAlias, final MqChannelAlias mqChannelAlias, final ServiceuserAlias serviceuserAlias) throws JMSException {
		MQConnectionFactory connectionFactory = new MQConnectionFactory();
		connectionFactory.setHostName(mqGatewayAlias.getHostname());
		connectionFactory.setPort(mqGatewayAlias.getPort());
		connectionFactory.setQueueManager(mqGatewayAlias.getName());
		connectionFactory.setTransportType(WMQ_CM_CLIENT);
		connectionFactory.setCCSID(UTF_8_WITH_PUA);
		connectionFactory.setIntProperty(JMS_IBM_ENCODING, MQENC_NATIVE);
		connectionFactory.setIntProperty(JMS_IBM_CHARACTER_SET, UTF_8_WITH_PUA);
		connectionFactory.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);
		if (mqChannelAlias.isEnabletls()) {
			connectionFactory.setSSLCipherSuite(ANY_TLS13_OR_HIGHER);
			SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			connectionFactory.setSSLSocketFactory(sslSocketFactory);
			connectionFactory.setChannel(mqChannelAlias.getSecurename());
		} else {
			connectionFactory.setChannel(mqChannelAlias.getName());
		}

		UserCredentialsConnectionFactoryAdapter adapter = new UserCredentialsConnectionFactoryAdapter();
		adapter.setTargetConnectionFactory(connectionFactory);
		adapter.setUsername(serviceuserAlias.getUsername());
		adapter.setPassword(serviceuserAlias.getPassword());

		PooledConnectionFactory pooledFactory = new PooledConnectionFactory();
		pooledFactory.setConnectionFactory(adapter);
		pooledFactory.setMaxConnections(10);
		pooledFactory.setMaximumActiveSessionPerConnection(10);

		return pooledFactory;
	}
}
