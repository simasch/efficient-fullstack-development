package ch.martinelli.tm.core.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end smoke test: a real browser logs in against the real application backed by a
 * real PostgreSQL. The seed data from V3 provides the demo users.
 */
class LoginIT extends PlaywrightIT {

	@Test
	void login_as_admin_shows_dashboard() {
		// the first request may trigger a frontend build in development mode
		page.setDefaultTimeout(120_000);
		page.navigate("http://localhost:%d/login".formatted(localServerPort));

		page.locator("input[name='username']").fill("admin");
		page.locator("input[name='password']").fill("admin");
		page.locator("vaadin-button[slot='submit']").click();

		mopo.waitForConnectionToSettle();

		var appName = page.locator("div.app-name");
		assertThat(appName.innerText()).isEqualTo("Task Management");
	}

}
