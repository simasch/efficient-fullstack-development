package ch.martinelli.tm.core.security;

import ch.martinelli.tm.core.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import com.vaadin.flow.spring.security.stateless.VaadinStatelessSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableWebSecurity
// Route annotations protect routes; @PreAuthorize on the services protects the
// operations, whoever calls them
@EnableMethodSecurity
public class SecurityConfiguration {

	private static final int JWT_LIFETIME_SECONDS = 1800;

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
		// login screen. The actuator endpoints are anonymous because the platform probes
		// them without a login; in production both live on a management port that is not
		// exposed publicly (application-prod.properties).
		http.authorizeHttpRequests(c -> c.requestMatchers("/*.css", "/images/*.png", "/line-awesome/*")
			.permitAll()
			.requestMatchers(EndpointRequest.to("health", "prometheus"))
			.permitAll());

		http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class));

		// The JWT is not revocable: deactivating a user or removing a role takes effect
		// when the current token expires, so the lifetime is set explicitly rather than
		// left at the default of 1800 seconds
		http.with(new VaadinStatelessSecurityConfigurer<>(),
				stateless -> stateless.issuer("ch.martinelli.tm")
					.expiresIn(JWT_LIFETIME_SECONDS)
					.withSecretKey()
					.secretKey(new SecretKeySpec(Base64.getDecoder().decode(authSecret), JwsAlgorithms.HS256)));

		return http.build();
	}

}
