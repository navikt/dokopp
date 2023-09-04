package no.nav.dokopp.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
@ConfigurationProperties("arkiverdokumentproduksjon.v1")
@Validated
public class ArkiverDokumentproduksjonV1Alias {
	@NotEmpty
	private String endpointurl;
	private String description;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}
