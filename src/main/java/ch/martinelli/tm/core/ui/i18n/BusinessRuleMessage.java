package ch.martinelli.tm.core.ui.i18n;

import ch.martinelli.tm.domain.BusinessRuleException;
import com.vaadin.flow.i18n.I18NProvider;

import java.util.Arrays;
import java.util.Locale;

/**
 * Turns a {@link BusinessRuleException} into a sentence in the language of the current
 * UI. A parameter that is an enum constant is a translation key of its own —
 * {@code TaskStatus.IN_PROGRESS} becomes {@code task.status.IN_PROGRESS} — so a rule that
 * names a status reads correctly in every language.
 */
public final class BusinessRuleMessage {

	private BusinessRuleMessage() {
	}

	public static String translate(BusinessRuleException exception) {
		Object[] parameters = Arrays.stream(exception.getMessageParameters())
			.map(parameter -> parameter instanceof Enum<?> constant ? I18NProvider.translate(keyOf(constant))
					: parameter)
			.toArray();
		return I18NProvider.translate(exception.getMessageKey(), parameters);
	}

	/**
	 * {@code TaskStatus.OPEN} becomes {@code task.status.OPEN}: the simple name of the
	 * enum, split at every camel case hump, is the prefix of the constant's key.
	 */
	private static String keyOf(Enum<?> constant) {
		String prefix = constant.getDeclaringClass()
			.getSimpleName()
			.replaceAll("(?<!^)(?=[A-Z])", ".")
			.toLowerCase(Locale.ROOT);
		return prefix + "." + constant.name();
	}

}
