package ch.martinelli.tm.core.ui.i18n;

import ch.martinelli.tm.core.domain.UsernameAlreadyTakenException;
import ch.martinelli.tm.core.ui.AbstractBrowserlessTest;
import ch.martinelli.tm.domain.BusinessRuleException;
import ch.martinelli.tm.domain.TaskStatus;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The domain throws keys, not sentences — this is where they become a message the user
 * can read, in the language the language switcher selected.
 */
class BusinessRuleMessageTest extends AbstractBrowserlessTest {

	@Test
	void translates_the_key_and_its_enum_parameters() {
		UI.getCurrent().setLocale(Locale.GERMAN);

		var exception = new BusinessRuleException("error.task.invalid.status.transition", TaskStatus.OPEN,
				TaskStatus.BLOCKED);

		assertThat(BusinessRuleMessage.translate(exception))
			.isEqualTo("Eine Aufgabe im Status Offen kann nicht nach Blockiert wechseln.");
	}

	@Test
	void leaves_a_plain_parameter_untouched() {
		UI.getCurrent().setLocale(Locale.ENGLISH);

		var exception = new UsernameAlreadyTakenException("simon", new RuntimeException("duplicate key"));

		assertThat(BusinessRuleMessage.translate(exception)).isEqualTo("The username \"simon\" is already taken.");
	}

}
