package ch.martinelli.tm.core.domain;

import ch.martinelli.tm.domain.BusinessRuleException;

public class UsernameAlreadyTakenException extends BusinessRuleException {

	public UsernameAlreadyTakenException(String username, Throwable cause) {
		super(cause, "error.username.already.taken", username);
	}

}
