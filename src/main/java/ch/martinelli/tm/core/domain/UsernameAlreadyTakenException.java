package ch.martinelli.tm.core.domain;

public class UsernameAlreadyTakenException extends RuntimeException {

	public UsernameAlreadyTakenException(String username, Throwable cause) {
		super("The username '%s' is already taken".formatted(username), cause);
	}

}
