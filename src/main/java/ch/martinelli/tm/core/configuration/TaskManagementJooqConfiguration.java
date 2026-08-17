package ch.martinelli.tm.core.configuration;

import org.springframework.boot.jooq.autoconfigure.DefaultConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskManagementJooqConfiguration {

	@Bean
	DefaultConfigurationCustomizer jooqCustomizer() {
		return configuration -> configuration.settings()
			.withRenderFormatted(true)
			.withRenderSchema(false)
			.withExecuteWithOptimisticLocking(true)
			.withBatchSize(500);
	}

}
