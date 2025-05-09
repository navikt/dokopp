package no.nav.dokopp.consumer.azure;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@Validated
@ConfigurationProperties("azure")
public record AzureProperties(
		@NotEmpty String openidConfigTokenEndpoint,
		@NotEmpty String appClientId,
		@NotEmpty String appClientSecret
) {
	public static final String CLIENT_REGISTRATION_PDL = "azure-pdl";
	public static final String CLIENT_REGISTRATION_OPPGAVE = "azure-oppgave";
	public static final String CLIENT_REGISTRATION_SAF= "azure-saf";
}
