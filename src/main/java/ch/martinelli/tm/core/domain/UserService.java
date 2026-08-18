package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.domain.Role;
import ch.martinelli.tm.domain.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	// findAllActive is deliberately open to every authenticated user: the task form
	// needs the list of possible assignees. Everything that reads or writes accounts is
	// restricted here, not only on the route that happens to call it today.

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<User> findAllActive() {
		return userRepository.findAllActive();
	}

	@PreAuthorize("hasRole('" + Role.ADMIN + "')")
	@Transactional(readOnly = true)
	public List<UserWithRoles> findAllWithRoles(int offset, int limit) {
		return userRepository.findAllWithRoles(offset, limit);
	}

	@PreAuthorize("hasRole('" + Role.ADMIN + "')")
	@Transactional(readOnly = true)
	public Optional<UserWithRoles> findWithRolesByUsername(String username) {
		return userRepository.findWithRolesByUsername(username);
	}

	@PreAuthorize("hasRole('" + Role.ADMIN + "')")
	@Transactional
	public void save(UserWithRoles userWithRoles) {
		try {
			userRepository.save(userWithRoles);
		}
		catch (DuplicateKeyException e) {
			throw new UsernameAlreadyTakenException(userWithRoles.getUser().getUsername(), e);
		}
	}

}
