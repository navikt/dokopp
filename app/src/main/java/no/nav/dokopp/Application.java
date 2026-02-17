package no.nav.dokopp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Import({
		ApplicationConfig.class
})
@SpringBootApplication
public class Application {

	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}


