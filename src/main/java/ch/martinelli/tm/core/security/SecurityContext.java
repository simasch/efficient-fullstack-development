package ch.martinelli.tm.core.security;

import ch.martinelli.tm.core.domain.UserRepository;
import ch.martinelli.tm.db.tables.records.AppUserRecord;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * A small facade around Spring Security and Vaadin's {@link AuthenticationContext}: who
 * is logged in, what are they allowed to do, and how do they leave.
 */
@Component
public class SecurityContext {

	private final AuthenticationContext authenticationContext;

	private final UserRepository userRepository;

	public SecurityContext(AuthenticationContext authenticationContext, UserRepository userRepository) {
		this.userRepository = userRepository;
		this.authenticationContext = authenticationContext;
	}

	public Optional<AppUserRecord> getLoggedInUser() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Optional.empty();
		}
		// After the login request the principal is a UserDetails; on every further
		// request it is the JWT that came back with the cookie.
		if (authentication.getPrincipal() instanceof Jwt jwt && jwt.getSubject() != null) {
			return userRepository.findByUsername(jwt.getSubject());
		}
		if (authentication.getPrincipal() instanceof UserDetails userDetails) {
			return userRepository.findByUsername(userDetails.getUsername());
		}
		return Optional.empty();
	}

	public boolean hasRole(String role) {
		return authenticationContext.hasRole(role);
	}

	public void logout() {
		authenticationContext.logout();
	}

}
