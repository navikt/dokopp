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
public class FunctionalBoqCheck extends AbstractDependencyCheck {

	private final Queue qopp001FunksjonellFeil;
	private final JmsTemplate jmsTemplate;

	@Autowired
	public FunctionalBoqCheck(Queue qopp001FunksjonellFeil,
							  JmsTemplate jmsTemplate) throws JMSException {
		super(DependencyType.QUEUE, "Qopp001FunctionalBoq", qopp001FunksjonellFeil.getQueueName(), Importance.CRITICAL);
		this.qopp001FunksjonellFeil = qopp001FunksjonellFeil;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	protected void doCheck() {
		try {
			checkQueue(qopp001FunksjonellFeil);
		} catch (Exception e) {
			throw new ApplicationNotReadyException("JMS Queue Browser failed to get queue: " + qopp001FunksjonellFeil, e);
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
