package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.db.tables.records.AppUserRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserWithRoles {

	private final AppUserRecord user;

	private Set<String> roles;

	public UserWithRoles() {
		this.user = new AppUserRecord();
		this.user.setActive(true);
		this.roles = new HashSet<>();
	}

	public UserWithRoles(AppUserRecord user, List<String> roles) {
		this.user = user;
		this.roles = new HashSet<>(roles);
	}

	public AppUserRecord getUser() {
		return user;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

}
