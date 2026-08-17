package ch.martinelli.tm.core.security;

import ch.martinelli.tm.core.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import com.vaadin.flow.spring.security.stateless.VaadinStatelessSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private final String authSecret;

	public SecurityConfiguration(@Value("${jwt.auth.secret}") String authSecret) {
		this.authSecret = authSecret;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) {
		// The stylesheets imported by styles.css are fetched by the browser, also on the
		// login screen
		http.authorizeHttpRequests(c -> c.requestMatchers("/*.css", "/images/*.png", "/line-awesome/*")
			.permitAll()
			.requestMatchers(EndpointRequest.to(HealthEndpoint.class))
			.permitAll());

		http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));

		http.with(new VaadinStatelessSecurityConfigurer<>(),
				stateless -> stateless.issuer("ch.martinelli.tm")
					.withSecretKey()
					.secretKey(new SecretKeySpec(Base64.getDecoder().decode(authSecret), JwsAlgorithms.HS256)));

		return http.build();
	}

}
