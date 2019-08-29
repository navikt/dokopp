package no.nav.dokopp;


import no.nav.dokopp.util.DokoppConfigSetter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import(value = {ApplicationConfig.class})
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		DokoppConfigSetter configSetter = new DokoppConfigSetter();
		configSetter.configureSsl();
		configSetter.setAppConfig();
		SpringApplication.run(Application.class, args);
	}
}


