package no.nav.dokopp.config.metrics;

import static no.nav.dokopp.config.metrics.PrometheusLabels.EVENT_PROCESSED;
import static no.nav.dokopp.config.metrics.PrometheusLabels.EVENT_RECEIVED;
import static no.nav.dokopp.config.metrics.PrometheusLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.dokopp.config.metrics.PrometheusLabels.TYPE_TECHNICAL_EXCEPTION;
import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestCounter;
import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestExceptionCounter;
import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestLatency;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.prometheus.client.Histogram;
import no.nav.dokopp.exception.DokoppFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.processor.validation.SchemaValidationException;
import org.apache.camel.support.RoutePolicySupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusMetricsRoutePolicy extends RoutePolicySupport {
	
	private Histogram.Timer requestTimer;
	
	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		startTimer();
		requestCounter.labels(SERVICE_ID, EVENT_RECEIVED).inc();
	}
	
	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		endTimer();
		Exception exception = getException(exchange);
		
		if (exception == null) {
			requestCounter.labels(SERVICE_ID, EVENT_PROCESSED).inc();
		} else {
			if (isFunctionalException(exception)) {
				requestExceptionCounter.labels(SERVICE_ID, TYPE_FUNCTIONAL_EXCEPTION, exception.getClass().getSimpleName())
						.inc();
			} else {
				requestExceptionCounter.labels(SERVICE_ID, TYPE_TECHNICAL_EXCEPTION, exception.getClass().getSimpleName())
						.inc();
			}
		}
	}
	
	private void startTimer() {
		requestTimer = requestLatency.labels(SERVICE_ID, SERVICE_ID).startTimer();
	}
	
	private void endTimer() {
		requestTimer.observeDuration();
	}
	
	private Exception getException(Exchange exchange) {
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		if (exception == null && exchange.getException() instanceof Exception) {
			exception = (Exception) exchange.getException().getCause();
		}
		return exception;
	}
	
	private Boolean isFunctionalException(Exception exception) {
		return exception instanceof DokoppFunctionalException || exception instanceof SchemaValidationException;
	}
}
