package ch.martinelli.tm.domain;

import org.jspecify.annotations.Nullable;

/**
 * A business rule the user broke. The domain does not know which language the user reads,
 * so the exception carries a message key and its parameters instead of a finished
 * sentence — the UI layer resolves both against the translation catalog.
 */
public class BusinessRuleException extends RuntimeException {

	private final String messageKey;

	private final Object[] messageParameters;

	public BusinessRuleException(String messageKey, Object... messageParameters) {
		this(null, messageKey, messageParameters);
	}

	public BusinessRuleException(@Nullable Throwable cause, String messageKey, Object... messageParameters) {
		// The key is the technical message: it is what ends up in the log
		super(messageKey, cause);
		this.messageKey = messageKey;
		this.messageParameters = messageParameters.clone();
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getMessageParameters() {
		return messageParameters.clone();
	}

}
