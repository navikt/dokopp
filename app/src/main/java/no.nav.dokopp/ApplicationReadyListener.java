package no.nav.dokopp;

import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {
	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		startPrometheus();
	}

	private void startPrometheus() {
		DefaultExports.initialize();
	}
}
