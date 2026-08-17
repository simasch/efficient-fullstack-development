package ch.martinelli.tm.domain;

/**
 * A value type for email addresses. The database stores a plain varchar; the
 * {@code EmailAddressConverter} makes sure that everything above the JDBC layer works
 * with this type instead of a bare String.
 */
public record EmailAddress(String value) {

	public EmailAddress {
		if (!value.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
			throw new IllegalArgumentException("Not a valid email address: " + value);
		}
	}

}
