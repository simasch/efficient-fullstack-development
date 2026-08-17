package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.db.tables.records.AppUserRecord;
import ch.martinelli.tm.domain.User;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Records;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.UserRole.USER_ROLE;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Repository
public class UserRepository {

	private final DSLContext dsl;

	public UserRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	public List<User> findAllActive() {
		return dsl.select(APP_USER.ID, APP_USER.USERNAME, APP_USER.FULL_NAME)
			.from(APP_USER)
			.where(APP_USER.ACTIVE.isTrue())
			.orderBy(APP_USER.FULL_NAME.asc())
			.fetch(Records.mapping(User::new));
	}

	public Optional<AppUserRecord> findByUsername(String username) {
		return dsl.fetchOptional(APP_USER, APP_USER.USERNAME.eq(username));
	}

	public Optional<UserWithRoles> findWithRolesByUsername(String username) {
		return dsl
			.select(APP_USER,
					multiset(select(USER_ROLE.ROLE).from(USER_ROLE).where(USER_ROLE.USER_ID.eq(APP_USER.ID)))
						.convertFrom(r -> r.map(Record1::value1)))
			.from(APP_USER)
			.where(APP_USER.USERNAME.eq(username))
			.fetchOptional(mapping(UserWithRoles::new));
	}

	public List<UserWithRoles> findAllWithRoles(int offset, int limit) {
		return dsl
			.select(APP_USER,
					multiset(select(USER_ROLE.ROLE).from(USER_ROLE).where(USER_ROLE.USER_ID.eq(APP_USER.ID)))
						.convertFrom(r -> r.map(Record1::value1)))
			.from(APP_USER)
			.orderBy(APP_USER.USERNAME.asc())
			.offset(offset)
			.limit(limit)
			.fetch(mapping(UserWithRoles::new));
	}

	/**
	 * Saves the user and replaces its roles. Throws Spring's
	 * {@code DuplicateKeyException} if the username is already taken — the service layer
	 * translates that into a business exception.
	 */
	public void save(UserWithRoles userWithRoles) {
		var user = userWithRoles.getUser();
		dsl.attach(user);
		user.store();

		dsl.deleteFrom(USER_ROLE).where(USER_ROLE.USER_ID.eq(user.getId())).execute();
		for (var role : userWithRoles.getRoles()) {
			dsl.insertInto(USER_ROLE).set(USER_ROLE.USER_ID, user.getId()).set(USER_ROLE.ROLE, role).execute();
		}
	}

}
