package no.nav.dokopp.config;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CamelUri {
	private final String uri;
	private final String routeId;
}
