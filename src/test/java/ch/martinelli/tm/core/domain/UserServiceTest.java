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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

/**
 * Every test declares the user it runs as: the user administration is restricted with
 * {@code @PreAuthorize}, so who is logged in is part of the test case, not of the setup.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserServiceTest {

	@Autowired
	private DSLContext dsl;

	@Autowired
	private UserService userService;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(APP_USER).execute();
	}

	@Test
	@WithMockUser(username = "admin", roles = Role.ADMIN)
	void save_storesUserWithRoles() {
		userService.save(newUser("simon"));

		var saved = userService.findWithRolesByUsername("simon").orElseThrow();
		assertThat(saved.getUser().getFullName()).isEqualTo("Simon Martinelli");
		assertThat(saved.getRoles()).containsExactly(Role.USER);
	}

	@Test
	@WithMockUser(username = "admin", roles = Role.ADMIN)
	void save_withTakenUsername_throwsUsernameAlreadyTakenException() {
		userService.save(newUser("simon"));

		var duplicate = newUser("simon");
		assertThatThrownBy(() -> userService.save(duplicate)).isInstanceOf(UsernameAlreadyTakenException.class)
			.asInstanceOf(type(UsernameAlreadyTakenException.class))
			.satisfies(e -> {
				assertThat(e.getMessageKey()).isEqualTo("error.username.already.taken");
				assertThat(e.getMessageParameters()).containsExactly("simon");
			});
	}

	@Test
	@WithMockUser(username = "alice", roles = Role.USER)
	void save_asPlainUser_isDenied() {
		var user = newUser("simon");

		assertThatThrownBy(() -> userService.save(user)).isInstanceOf(AccessDeniedException.class);
	}

	@Test
	@WithMockUser(username = "alice", roles = Role.USER)
	void findAllWithRoles_asPlainUser_isDenied() {
		assertThatThrownBy(() -> userService.findAllWithRoles(0, 10)).isInstanceOf(AccessDeniedException.class);
	}

	private UserWithRoles newUser(String username) {
		var userWithRoles = new UserWithRoles();
		userWithRoles.getUser().setUsername(username);
		userWithRoles.getUser().setFullName("Simon Martinelli");
		userWithRoles.getUser().setEmail(new EmailAddress(username + "@example.com"));
		userWithRoles.getUser().setPasswordHash("{noop}secret");
		userWithRoles.setRoles(Set.of(Role.USER));
		return userWithRoles;
	}

}
