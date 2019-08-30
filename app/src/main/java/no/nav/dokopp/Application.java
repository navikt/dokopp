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
		// Skru av runtime bytecode optimalisering i jaxb.
		// Denne funksjonaliteten vil bli fjernet i sin helhet i JAXB referanse implementasjonen
		// org.glassfish.jaxb:jaxb-runtime:2.4.0 (ikke publisert pr d.d.).
		System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");
		SpringApplication.run(Application.class, args);
	}
}


