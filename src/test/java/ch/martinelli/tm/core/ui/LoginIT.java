package ch.martinelli.tm.core.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke test: a real browser logs in against the real application backed by a
 * real PostgreSQL. The seed data from V999 provides the demo users.
 */
class LoginIT extends PlaywrightIT {

	@Test
	void login_as_admin_shows_dashboard() {
		// the first request may trigger a frontend build in development mode
		page.setDefaultTimeout(120_000);
		page.navigate("login");

		page.locator("input[name='username']").fill("admin");
		page.locator("input[name='password']").fill("admin");
		page.locator("vaadin-button[slot='submit']").click();

		waitForVaadin();

		var appName = page.locator("div.app-name");
		assertThat(appName.innerText()).isEqualTo("Task Management");
	}

}
