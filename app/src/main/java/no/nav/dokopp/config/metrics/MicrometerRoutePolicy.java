package no.nav.dokopp.config.metrics;

import static no.nav.dokopp.config.metrics.MetricLabels.EVENT_PROCESSED;
import static no.nav.dokopp.config.metrics.MetricLabels.EVENT_RECEIVED;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_ERROR_TYPE;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_EVENT;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_EXCEPTION_NAME;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS;
import static no.nav.dokopp.config.metrics.MetricLabels.LABEL_PROCESS_CALLED;
import static no.nav.dokopp.config.metrics.MetricLabels.TYPE_FUNCTIONAL_EXCEPTION;
import static no.nav.dokopp.config.metrics.MetricLabels.TYPE_TECHNICAL_EXCEPTION;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_COUNTER_BUILDER;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_EXCEPTION_COUNTER_METRIC;
import static no.nav.dokopp.config.metrics.MicrometerMetrics.REQUEST_LATENCY_TIMER_METRIC;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import no.nav.dokopp.exception.DokoppFunctionalException;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;
import org.apache.camel.support.processor.validation.SchemaValidationException;

public class MicrometerRoutePolicy extends RoutePolicySupport {
	private final MeterRegistry meterRegistry;
	private Timer.Sample timerSample;

	public MicrometerRoutePolicy(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void onExchangeBegin(Route route, Exchange exchange) {
		startTimer();
		REQUEST_COUNTER_BUILDER.tags(LABEL_PROCESS, SERVICE_ID,
				LABEL_EVENT, EVENT_RECEIVED).register(meterRegistry).increment();
	}

	@Override
	public void onExchangeDone(Route route, Exchange exchange) {
		endTimer();
		Exception exception = getException(exchange);

		if (exception == null) {
			REQUEST_COUNTER_BUILDER.tags(LABEL_PROCESS, SERVICE_ID,
					LABEL_EVENT, EVENT_PROCESSED).register(meterRegistry).increment();
		} else {
			if (isFunctionalException(exception)) {
				Counter.builder(REQUEST_EXCEPTION_COUNTER_METRIC)
						.description("Exception counter.")
						.tags(LABEL_PROCESS, SERVICE_ID,
								LABEL_ERROR_TYPE, TYPE_FUNCTIONAL_EXCEPTION,
								LABEL_EXCEPTION_NAME, exception.getClass().getSimpleName())
						.register(meterRegistry).increment();
			} else {
				Counter.builder(REQUEST_EXCEPTION_COUNTER_METRIC)
						.description("Exception counter.")
						.tags(LABEL_PROCESS, SERVICE_ID,
								LABEL_ERROR_TYPE, TYPE_TECHNICAL_EXCEPTION,
								LABEL_EXCEPTION_NAME, exception.getClass().getSimpleName())
						.register(meterRegistry).increment();
			}
		}
	}

	private void startTimer() {
		timerSample = Timer.start(meterRegistry);
	}

	private void endTimer() {
		timerSample.stop(Timer.builder(REQUEST_LATENCY_TIMER_METRIC)
				.description("Timing of external and internal calls.")
				.tags(LABEL_PROCESS, SERVICE_ID,
						LABEL_PROCESS_CALLED, SERVICE_ID)
				.publishPercentileHistogram(true)
				.publishPercentiles(0.5, 0.75, 0.90, 0.99)
				.register(meterRegistry));
	}

	private Exception getException(Exchange exchange) {
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		if (exception == null && exchange.getException() != null) {
			exception = (Exception) exchange.getException().getCause();
		}
		return exception;
	}

	private Boolean isFunctionalException(Exception exception) {
		return exception instanceof DokoppFunctionalException || exception instanceof SchemaValidationException;
	}
}
