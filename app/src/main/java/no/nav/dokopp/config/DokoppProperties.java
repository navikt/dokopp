package no.nav.dokopp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties("dokopp")
@Validated
public class DokoppProperties {

	private final Proxy proxy = new Proxy();
	private final Endpoints endpoints = new Endpoints();
	private final DokCert cert = new DokCert();

	@Data
	@Validated
	public static class Proxy {
		private String host;
		private int port;

		public boolean isSet() {
			return (host != null && !host.equals(""));
		}
	}

	@Data
	@Validated
	public static class Endpoints {

		@NotNull
		private AzureEndpoint saf;

		@NotEmpty
		private String pdl;

		@NotEmpty
		private String oppgave;

		@NotEmpty
		private String sts;
	}


	@Data
	@Validated
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

	@Data
	@Validated
	public class DokCert {
		@NotEmpty
		private String keystore;
		@NotEmpty
		private String keystorealias;
		@NotEmpty
		private String password;
	}
}
