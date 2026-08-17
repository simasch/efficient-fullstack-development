package ch.martinelli.tm.core.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginViewTest extends AbstractBrowserlessTest {

	@Test
	void login_overlay_is_shown() {
		var loginView = navigate(LoginView.class);

		assertThat(loginView.isOpened()).isTrue();
	}

}
