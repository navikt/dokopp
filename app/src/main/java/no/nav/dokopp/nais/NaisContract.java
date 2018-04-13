package no.nav.dokopp.nais;

import static no.nav.dokopp.config.metrics.PrometheusMetrics.isReady;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokopp.nais.checks.BehandleOppgaveV1Check;
import no.nav.dokopp.nais.checks.FunctionalBoqCheck;
import no.nav.dokopp.nais.checks.Qopp001QueueCheck;
import no.nav.dokopp.nais.checks.Tjoark110Check;
import no.nav.dokopp.nais.checks.Tjoark122Check;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
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
	
	private final Qopp001QueueCheck qopp001;
	private final FunctionalBoqCheck functionalBoq;
	private final BehandleOppgaveV1Check behandleOppgaveV1;
	private final Tjoark110Check tjoark110;
	private final Tjoark122Check tjoark122;
	
	@Inject
	public NaisContract(Qopp001QueueCheck qopp001,
						FunctionalBoqCheck functionalBoq,
						BehandleOppgaveV1Check behandleOppgaveV1,
						Tjoark110Check tjoark110,
						Tjoark122Check tjoark122
	) {
		this.qopp001 = qopp001;
		this.functionalBoq = functionalBoq;
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
			qopp001.check();
			functionalBoq.check();
			behandleOppgaveV1.check();
			tjoark110.check();
			tjoark122.check();
			isReady.set(1);
			
		} catch (ApplicationNotReadyException e) {
			String errorMsg = "Application not ready to accept traffic.";
			log.error(errorMsg, e);
			
			isReady.dec();
			return new ResponseEntity<>(errorMsg + " reason=" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(APPLICATION_READY, HttpStatus.OK);
	}
}
