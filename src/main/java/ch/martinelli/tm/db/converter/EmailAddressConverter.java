package ch.martinelli.tm.db.converter;

import ch.martinelli.tm.domain.EmailAddress;
import org.jooq.impl.AbstractConverter;
import org.jspecify.annotations.Nullable;

public class EmailAddressConverter extends AbstractConverter<String, EmailAddress> {

	public EmailAddressConverter() {
		super(String.class, EmailAddress.class);
	}

	@Override
	public @Nullable EmailAddress from(@Nullable String databaseObject) {
		return databaseObject == null ? null : new EmailAddress(databaseObject);
	}

	@Override
	public @Nullable String to(@Nullable EmailAddress userObject) {
		return userObject == null ? null : userObject.value();
	}

}
