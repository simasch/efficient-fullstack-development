package ch.martinelli.tm.core.ui;

import ch.martinelli.tm.TestcontainersConfiguration;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class PlaywrightIT {

	@LocalServerPort
	protected Integer localServerPort;

	private static Playwright playwright;

	private static Browser browser;

	protected Page page;

	private BrowserContext browserContext;

	@BeforeAll
	static void setUpClass() {
		playwright = Playwright.create();
		BrowserType browserType = playwright.chromium();
		BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
		// set to false if you want to see the browser during development
		launchOptions.headless = true;
		browser = browserType.launch(launchOptions);
	}

	@AfterAll
	static void tearDownClass() {
		browser.close();
		playwright.close();
	}

	@BeforeEach
	void setUp() {
		// The locale is pinned: the application is translated, and the assertions below
		// match the English texts
		browserContext = browser
			.newContext(new Browser.NewContextOptions().setBaseURL("http://localhost:%d/".formatted(localServerPort))
				.setLocale("en-US"));
		page = browserContext.newPage();
	}

	@AfterEach
	void tearDown() {
		page.close();
		browserContext.close();
	}

	/**
	 * Logs in through the real login form. The demo users all use their username as
	 * password. The assertion at the end makes the method block until the application
	 * shell is up, so a failure is reported here and not three steps later.
	 */
	protected void login(String username) {
		page.navigate("login");

		page.locator("input[name='username']").fill(username);
		page.locator("input[name='password']").fill(username);
		page.locator("vaadin-button[slot='submit']").click();

		assertThat(page.locator("div.app-name")).hasText("Task Management");
	}

	/**
	 * Blocks until Vaadin's client-side engine reports that no request is in flight. Only
	 * needed before a non-retrying call; Playwright's own assertions wait by themselves.
	 */
	protected void waitForVaadin() {
		page.waitForFunction(AbstractBasePlaywrightIT.WAIT_FOR_VAADIN_SCRIPT);
	}

	/**
	 * The cells of a grid that the user can actually see, matched by their text. A
	 * {@code vaadin-grid} keeps the cell content of earlier result sets in the DOM as
	 * hidden elements, so a plain locator still finds rows that are long gone — and the
	 * row lookups by index can resolve one of those recycled rows right after a reload.
	 * Matching on visible cells makes both the "is shown" and the "is gone" assertion
	 * reliable, and it retries like every other Playwright assertion.
	 */
	protected Locator visibleGridCells(String text) {
		return page.locator("vaadin-grid-cell-content:visible").filter(new Locator.FilterOptions().setHasText(text));
	}

}
