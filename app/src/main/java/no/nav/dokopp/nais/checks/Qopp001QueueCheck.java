package no.nav.dokopp.nais.checks;

import no.nav.dokopp.nais.selftest.AbstractDependencyCheck;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.DependencyType;
import no.nav.dokopp.nais.selftest.Importance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Qopp001QueueCheck extends AbstractDependencyCheck {

	private final Queue qopp001;
	private final JmsTemplate jmsTemplate;

	@Autowired
	public Qopp001QueueCheck(Queue qopp001,
							 JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "Qopp001QueueFeil", qopp001.getQueueName(), Importance.CRITICAL);
		this.qopp001 = qopp001;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(qopp001);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + qopp001, e);
		}
	}

	private void checkQueue(final Queue queue) {
		jmsTemplate.browse(queue,
				(session, browser) -> {
					browser.getQueue();
					return null;
				}
		);
	}
}
