package no.nav.dokopp.config.props;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("DOKOPP")
@Validated
public class DokoppProperties {

	@Valid
	private final Qopp001 qopp001 = new Qopp001();

	@Getter
	@Setter
	@ToString
	public static class Qopp001 {
		@Min(0)
		private int maximumredeliveries;
		@Min(60000)
		@Max(120000)
		private int maximumredeliverydelayms;
		@Min(-1)
		private int redeliverydelayms;
		@Min(1)
		private int backoffmultiplier;
	}
}
