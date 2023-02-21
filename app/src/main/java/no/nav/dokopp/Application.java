package no.nav.dokopp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static java.lang.System.getenv;
import static java.lang.System.setProperty;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", getenv("DOKOPPCERT_PASSWORD"));
		// Skru av runtime bytecode optimalisering i jaxb.
		// Denne funksjonaliteten vil bli fjernet i sin helhet i JAXB referanse implementasjonen
		// org.glassfish.jaxb:jaxb-runtime:2.4.0 (ikke publisert pr d.d.).
		setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

		SpringApplication.run(Application.class, args);
	}
}


