package no.nav.dokopp.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
@ConfigurationProperties("dokopp.channel")
@Validated
public class MqChannelAlias {
	@NotEmpty
	private String name;
	private String securename;
	private boolean enabletls;
}
