package no.nav.dokopp.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

@Configuration
@EnableCaching
public class LokalCacheConfig {

	public static final String STS_CACHE = "stsCache";
	public static final String AZURE_CACHE = "azureCache";

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(List.of(
				new CaffeineCache(STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(55, MINUTES)
						.build()),
				new CaffeineCache(AZURE_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, MINUTES)
						.maximumSize(10)
						.build())
				));
		return manager;
	}

}
