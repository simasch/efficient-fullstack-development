package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.domain.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<User> findAllActive() {
		return userRepository.findAllActive();
	}

	@Transactional(readOnly = true)
	public List<UserWithRoles> findAllWithRoles(int offset, int limit) {
		return userRepository.findAllWithRoles(offset, limit);
	}

	@Transactional(readOnly = true)
	public Optional<UserWithRoles> findWithRolesByUsername(String username) {
		return userRepository.findWithRolesByUsername(username);
	}

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
