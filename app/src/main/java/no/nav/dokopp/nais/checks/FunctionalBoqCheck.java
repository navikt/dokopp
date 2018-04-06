package no.nav.dokopp.nais.checks;

import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class FunctionalBoqCheck extends AbstractSelftest {
	private static final String FASIT_NAME = "dokopp";
	private static final String INTERNAL_ID = "QOPP001";

	private final Queue functionalBoq;
	private final JmsTemplate jmsTemplate;

	@Inject
	public FunctionalBoqCheck(Queue functionalBOQ,
							  JmsTemplate jmsTemplate) throws JMSException {
		super(Ping.Type.Queue, FASIT_NAME, functionalBOQ.getQueueName(), INTERNAL_ID);
		this.functionalBoq = functionalBOQ;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(functionalBoq);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + functionalBoq, e);
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
