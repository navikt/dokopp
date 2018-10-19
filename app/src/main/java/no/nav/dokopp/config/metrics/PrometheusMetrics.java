package no.nav.dokopp.config.metrics;

import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_ERROR_TYPE;
import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_EVENT;
import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_EXCEPTION_NAME;
import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_NAME;
import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_PROCESS;
import static no.nav.dokopp.config.metrics.PrometheusLabels.LABEL_PROCESS_CALLED;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class PrometheusMetrics {

	private static final String DOK_NAMESPACE = "dok";

	// Health checks
	public static final Gauge isReady = Gauge.build()
			.namespace(DOK_NAMESPACE)
			.name("app_is_ready")
			.help("App is ready to receive traffic")
			.register();

	public static final Gauge dependencyPingable = Gauge.build()
			.namespace(DOK_NAMESPACE)
			.name("dependency_ping")
			.help("Dependency is pingable")
			.labelNames(LABEL_NAME)
			.register();

	// Requests
	public static final Histogram requestLatency = Histogram.build()
			.namespace(DOK_NAMESPACE)
			.name("request_latency_seconds_histogram")
			.help("Timing of external and internal calls")
			.labelNames(LABEL_PROCESS, LABEL_PROCESS_CALLED)
			.register();

	public static final Counter requestCounter = Counter.build()
			.namespace(DOK_NAMESPACE)
			.name("request_total_counter")
			.help("Counts total number of requests received per event.")
			.labelNames(LABEL_PROCESS, LABEL_EVENT)
			.register();

	public static final Counter requestExceptionCounter = Counter.build()
			.namespace(DOK_NAMESPACE)
			.name("request_exception_total_counter")
			.help("Exception counter.")
			.labelNames(LABEL_PROCESS, LABEL_ERROR_TYPE, LABEL_EXCEPTION_NAME)
			.register();

}
