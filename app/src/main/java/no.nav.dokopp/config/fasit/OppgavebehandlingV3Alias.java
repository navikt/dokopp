package no.nav.dokopp.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("VIRKSOMHET_OPPGAVEBEHANDLING_V3")
@Validated
public class OppgavebehandlingV3Alias {
	@NotEmpty
	private String endpointurl;
	private String description;
	@Min(1)
	private int readtimeoutms;
	@Min(1)
	private int connecttimeoutms;
}