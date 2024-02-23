package no.nav.dokopp;

import io.micrometer.context.ContextRegistry;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

import static no.nav.dokopp.util.MDCOperations.ALL_KEYS;
import static reactor.core.publisher.Hooks.enableAutomaticContextPropagation;

public class ApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		registerReactorContextPropagation();
	}

	private static void registerReactorContextPropagation() {
		enableAutomaticContextPropagation();
		ALL_KEYS.forEach(ApplicationStartedEventListener::registerMDCKey);
	}

	private static void registerMDCKey(String key) {
		ContextRegistry.getInstance().registerThreadLocalAccessor(
				key,
				() -> MDC.get(key),
				value -> MDC.put(key, value),
				() -> MDC.remove(key));
	}
}
