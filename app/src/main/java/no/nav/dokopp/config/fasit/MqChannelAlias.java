package no.nav.dokopp.config.fasit;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@ConfigurationProperties("dokopp.channel")
@Validated
public class MqChannelAlias {
	@NotEmpty
	private String securename;
}
