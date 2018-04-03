package no.nav.dokopp.nais;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.isReady;
import static no.nav.dokopp.qopp001.Qopp001Route.SERVICE_ID;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.nais.checks.BehandleOppgaveV1Check;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.nais.checks.Tjoark122Check;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@RestController
public class NaisContract {
	
	public static final String APPLICATION_ALIVE = "Application is alive!";
	public static final String APPLICATION_READY = "Application is ready for traffic!";
	private static final int MAX_READY_FAIL = 3;
	public static final String ROUTE_SUSPENDED = "Suspended";
	public static final String ROUTE_STARTED = "Started";
	
	private final ProducerTemplate producerTemplate;
	private final Qopp001QueueCheck qopp001;
	private final BehandleOppgaveV1Check behandleOppgaveV1;
	private final Tjoark110Check tjoark110;
	private final Tjoark122Check tjoark122;
	
	
	@Inject
	public NaisContract(ProducerTemplate producerTemplate,
						Qopp001QueueCheck qopp001,
						BehandleOppgaveV1Check behandleOppgaveV1,
						Tjoark110Check tjoark110,
						Tjoark122Check tjoark122
	) {
		this.producerTemplate = producerTemplate;
		this.qopp001 = qopp001;
		this.behandleOppgaveV1 = behandleOppgaveV1;
		this.tjoark110 = tjoark110;
		this.tjoark122 = tjoark122;
	}
	
	@GetMapping("/isAlive")
	public String isAlive() {
		return APPLICATION_ALIVE;
	}
	
	@ResponseBody
	@RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity isReady() {
		try {
			String routeStatus = qmot004RouteStatus();
			qopp001.check();
//			behandleOppgaveV1.check();
			tjoark110.check();
			tjoark122.check();
			isReady.set(1);
			if (ROUTE_SUSPENDED.equals(routeStatus)) {
				resumeQopp001();
			}
		} catch (ApplicationNotReadyException e) {
			String errorMsg = "Application not ready to accept traffic.";
			log.error(errorMsg, e);
			if (Math.abs(isReady.get()) >= MAX_READY_FAIL) {
				String routeStatus = qmot004RouteStatus();
				if (ROUTE_STARTED.equals(routeStatus)) {
					suspendQopp001();
				}
			}
			isReady.dec();
			return new ResponseEntity<>(errorMsg + " reason=" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
	
	private String qmot004RouteStatus() {
		return controlbus("status");
	}
	
	private void suspendQopp001() {
		log.error("App is unhealthy. Suspending " + SERVICE_ID);
		controlbus("suspend");
		log.error(SERVICE_ID + " JMS read suspended.");
	}
	
	private void resumeQopp001() {
		log.info("App is healthy. Resuming " + SERVICE_ID);
		controlbus("resume");
		log.info(SERVICE_ID + " JMS read resumed.");
	}
	
	private String controlbus(final String action) {
		return producerTemplate.requestBody("controlbus:route?routeId=" + SERVICE_ID + "&action=" + action, null, String.class);
	}
}
