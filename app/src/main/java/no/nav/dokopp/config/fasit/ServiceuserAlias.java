package no.nav.dokopp.config.fasit;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

import static no.nav.modig.security.ws.AbstractSAMLOutInterceptor.SYSTEM_PROPERTY_APPCERT_ISSUER;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Getter
@Setter
@ToString
@ConfigurationProperties("serviceuser")
@Validated
public class ServiceuserAlias {
	@NotEmpty
	private String username;
	@NotEmpty
	private String password;

	@PostConstruct
	public void postConstruct() {
		System.setProperty("no.nav.modig.security.systemuser.username", username);
		System.setProperty("no.nav.modig.security.systemuser.password", password);
		System.setProperty(SYSTEM_PROPERTY_APPCERT_ISSUER, username);
	}
}
