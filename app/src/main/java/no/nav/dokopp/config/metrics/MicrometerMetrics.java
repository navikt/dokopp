package no.nav.dokopp.config.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

public class MicrometerMetrics {
	public static final String REQUEST_LATENCY_TIMER_METRIC = "dok_request_latency_seconds_histogram";
	public static final String REQUEST_COUNTER_METRIC = "dok_request_total_counter";
	public static final String REQUEST_EXCEPTION_COUNTER_METRIC = "dok_request_exception_total_counter";

	public static final Timer.Builder REQUEST_LATENCY_TIMER_BUILDER = Timer.builder(REQUEST_LATENCY_TIMER_METRIC)
			.description("Timing of external and internal calls.")
			.publishPercentileHistogram(true)
			.publishPercentiles(0.5, 0.75, 0.90, 0.99);

	public static final Counter.Builder REQUEST_COUNTER_BUILDER = Counter.builder(REQUEST_COUNTER_METRIC)
			.description("Counts total number of requests received per event.");
}
