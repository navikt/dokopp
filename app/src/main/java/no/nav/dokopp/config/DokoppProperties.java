package no.nav.dokopp.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("dokopp")
@Validated
public class DokoppProperties {

	@Valid
	private final Endpoints endpoints = new Endpoints();

	@Data
	public static class Endpoints {
		@Valid
		@NotNull
		private AzureEndpoint dokarkiv;

		@Valid
		@NotNull
		private AzureEndpoint saf;

		@Valid
		@NotNull
		private AzureEndpoint pdl;

		@Valid
		@NotNull
		private AzureEndpoint oppgave;
	}

	@Data
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;
		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}
}
