package no.nav.dokopp.qopp001.itest;

import no.nav.dokopp.consumer.azure.AzureTokenConsumer;
import no.nav.dokopp.consumer.azure.TokenResponse;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.ArgumentMatchers.anyString;

@Configuration
@Profile("itest")
public class ApplicationItestConfig {

	static class Config {
		@Bean
		@Primary
		AzureTokenConsumer azureTokenConsumer() {
			AzureTokenConsumer azureTokenConsumer = Mockito.mock(AzureTokenConsumer.class);
			Mockito.when(azureTokenConsumer.getClientCredentialToken(anyString())).thenReturn(
					TokenResponse.builder()
							.access_token("dummy")
							.build()
			);

			return azureTokenConsumer;
		}

	}
}
