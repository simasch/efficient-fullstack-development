package ch.martinelli.tm.core.ui.i18n;

import com.vaadin.flow.i18n.DefaultI18NProvider;
import com.vaadin.flow.i18n.I18NProvider;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * All the texts are written in English: {@code translations.properties} holds the English
 * texts, {@code translations_<language>.properties} the translations.
 * <p>
 * This behaves like {@link DefaultI18NProvider} except for one detail:
 * {@link ResourceBundle} falls back to {@link Locale#getDefault()} when there is no
 * bundle for the requested locale. On a server running with a German default locale that
 * turns a request for English into {@code translations_de.properties}, so switching the
 * language back to English changes nothing and the language switcher looks broken. The
 * control below removes that fallback, which makes an unmatched locale fall through to
 * the English base bundle instead.
 */
@Component
public class TranslationProvider implements I18NProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationProvider.class);

	private static final List<Locale> PROVIDED_LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);

	private static final ResourceBundle.Control NO_DEFAULT_LOCALE_FALLBACK = new ResourceBundle.Control() {
		@Override
		public @Nullable Locale getFallbackLocale(String baseName, Locale locale) {
			return null;
		}
	};

	@Override
	public List<Locale> getProvidedLocales() {
		return PROVIDED_LOCALES;
	}

	@Override
	public String getTranslation(String key, Locale locale, Object... params) {
		if (key == null) {
			LOGGER.warn("Got translation request for a null key!");
			return "";
		}

		String value;
		try {
			value = getBundle(locale).getString(key);
		}
		catch (MissingResourceException e) {
			LOGGER.debug("Missing translation for key {} and locale {}", key, locale, e);
			return "!" + locale.getLanguage() + ": " + key;
		}
		return params.length == 0 ? value : new MessageFormat(value, locale).format(params);
	}

	@Override
	public Map<String, String> getAllTranslations(Locale locale) {
		return getTranslations(getBundle(locale).keySet(), locale);
	}

	private ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle(DefaultI18NProvider.BUNDLE_PREFIX, locale, getClass().getClassLoader(),
				NO_DEFAULT_LOCALE_FALLBACK);
	}

}
