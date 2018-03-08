package no.nav.dokopp.config;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class CamelUri {
	private final String uri;
	private final String routeId;
}
