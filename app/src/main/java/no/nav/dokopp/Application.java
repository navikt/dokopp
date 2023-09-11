package no.nav.dokopp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import static java.lang.System.getenv;
import static java.lang.System.setProperty;

@Import({
		ApplicationConfig.class
})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		setProperty("javax.net.ssl.keyStorePassword", getenv("DOKOPPCERT_PASSWORD"));
		SpringApplication.run(Application.class, args);
	}
}


