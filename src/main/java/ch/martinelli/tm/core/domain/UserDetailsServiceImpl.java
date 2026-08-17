package ch.martinelli.tm.core.domain;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	public UserDetailsServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		var userWithRoles = userRepository.findWithRolesByUsername(username)
			.orElseThrow(() -> new UsernameNotFoundException("No user present with username: " + username));
		var user = userWithRoles.getUser();

		var authorities = userWithRoles.getRoles()
			.stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
			.toList();

		return User.withUsername(user.getUsername())
			.password(user.getPasswordHash())
			.disabled(!Boolean.TRUE.equals(user.getActive()))
			.authorities(authorities)
			.build();
	}

}
