package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.TestcontainersConfiguration;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Role;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static ch.martinelli.tm.db.tables.UserRole.USER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserDetailsServiceImplTest {

	@Autowired
	private DSLContext dsl;

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(APP_USER).execute();

		Long userId = dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "simon")
			.set(APP_USER.FULL_NAME, "Simon Martinelli")
			.set(APP_USER.EMAIL, new EmailAddress("simon@example.com"))
			.set(APP_USER.PASSWORD_HASH, "{noop}secret")
			.returningResult(APP_USER.ID)
			.fetchOne(APP_USER.ID);
		dsl.insertInto(USER_ROLE).set(USER_ROLE.USER_ID, userId).set(USER_ROLE.ROLE, Role.ADMIN).execute();

		dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "inactive")
			.set(APP_USER.FULL_NAME, "No Longer Here")
			.set(APP_USER.EMAIL, new EmailAddress("inactive@example.com"))
			.set(APP_USER.PASSWORD_HASH, "{noop}secret")
			.set(APP_USER.ACTIVE, false)
			.execute();
	}

	@Test
	void loadUserByUsername_returnsUserWithRoles() {
		var userDetails = userDetailsService.loadUserByUsername("simon");

		assertThat(userDetails.getUsername()).isEqualTo("simon");
		assertThat(userDetails.isEnabled()).isTrue();
		assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority)
			.containsExactly("ROLE_ADMIN");
	}

	@Test
	void loadUserByUsername_deactivatedUserIsDisabled() {
		var userDetails = userDetailsService.loadUserByUsername("inactive");

		assertThat(userDetails.isEnabled()).isFalse();
	}

	@Test
	void loadUserByUsername_unknownUserThrows() {
		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nobody"))
			.isInstanceOf(UsernameNotFoundException.class);
	}

}
