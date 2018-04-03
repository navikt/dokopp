package no.nav.dokopp.config.metrics;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.requestLatency;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.prometheus.client.Histogram;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusMetricsRoutePolicy extends RoutePolicySupport {
	
	private Histogram.Timer requestTimer;
	
	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		startTimer();
		
	}
	
	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		endTimer();
	}
	
	private void startTimer() {
		requestTimer = requestLatency.labels(SERVICE_ID, "QMOT004", "QMOT004").startTimer();
	}
	
	private void endTimer() {
		requestTimer.observeDuration();
	}
}
