package ch.martinelli.tm.core.ui.i18n;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationProviderTest {

	private final Locale defaultLocale = Locale.getDefault();

	private final TranslationProvider translationProvider = new TranslationProvider();

	@AfterEach
	void restoreDefaultLocale() {
		Locale.setDefault(defaultLocale);
	}

	@Test
	void translates_the_provided_locales() {
		assertThat(translationProvider.getTranslation("action.save", Locale.ENGLISH)).isEqualTo("Save");
		assertThat(translationProvider.getTranslation("action.save", Locale.GERMAN)).isEqualTo("Speichern");
	}

	/**
	 * The regression this provider exists for: {@code ResourceBundle} falls back to the
	 * JVM default locale, so on a German server a request for English used to return the
	 * German bundle — and switching the language back to English did nothing.
	 */
	@Test
	void english_is_not_affected_by_a_german_default_locale() {
		Locale.setDefault(Locale.GERMANY);

		assertThat(translationProvider.getTranslation("action.save", Locale.ENGLISH)).isEqualTo("Save");
	}

	@Test
	void an_unknown_locale_falls_back_to_english() {
		Locale.setDefault(Locale.GERMANY);

		assertThat(translationProvider.getTranslation("action.save", Locale.FRENCH)).isEqualTo("Save");
	}

	@Test
	void a_missing_key_is_reported_instead_of_thrown() {
		assertThat(translationProvider.getTranslation("does.not.exist", Locale.ENGLISH))
			.isEqualTo("!en: does.not.exist");
	}

}
