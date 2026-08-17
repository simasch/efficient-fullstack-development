package ch.martinelli.tm.user.ui;

import ch.martinelli.tm.core.ui.AbstractBrowserlessTest;
import ch.martinelli.tm.core.domain.UserWithRoles;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Role;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Set;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static ch.martinelli.tm.db.tables.UserRole.USER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;

@WithMockUser(username = "admin", roles = Role.ADMIN)
class UserViewTest extends AbstractBrowserlessTest {

	@Autowired
	private DSLContext dsl;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(APP_USER).execute();

		Long adminId = dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "admin")
			.set(APP_USER.FULL_NAME, "Alex Keller")
			.set(APP_USER.EMAIL, new EmailAddress("admin@example.com"))
			.set(APP_USER.PASSWORD_HASH, "{noop}admin")
			.returningResult(APP_USER.ID)
			.fetchOne(APP_USER.ID);
		dsl.insertInto(USER_ROLE).set(USER_ROLE.USER_ID, adminId).set(USER_ROLE.ROLE, Role.ADMIN).execute();

		dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "alice")
			.set(APP_USER.FULL_NAME, "Alice Meyer")
			.set(APP_USER.EMAIL, new EmailAddress("alice@example.com"))
			.set(APP_USER.PASSWORD_HASH, "{noop}alice")
			.execute();
	}

	@Test
	void check_grid_size() {
		navigate(UserView.class);

		Grid<UserWithRoles> grid = find(Grid.class).single();
		assertThat(test(grid).size()).isEqualTo(2);
	}

	@Test
	void navigate_to_user() {
		navigate(UserView.class, "admin");

		Grid<UserWithRoles> grid = find(Grid.class).single();
		assertThat(test(grid).size()).isEqualTo(2);

		Set<UserWithRoles> selectedItems = grid.getSelectedItems();
		assertThat(selectedItems).hasSize(1)
			.first()
			.extracting(userWithRoles -> userWithRoles.getUser().getFullName())
			.isEqualTo("Alex Keller");

		TextField fullNameTextField = find(TextField.class).withCaption("Full Name").single();
		assertThat(fullNameTextField.getValue()).isEqualTo("Alex Keller");
	}

	@Test
	void save_new_user() {
		navigate(UserView.class);

		Grid<UserWithRoles> grid = find(Grid.class).single();
		int initialSize = test(grid).size();

		var addIcon = grid.getColumnByKey("actions").getHeaderComponent();
		test((com.vaadin.flow.component.icon.SvgIcon) addIcon).click();

		TextField usernameField = find(TextField.class).withCaption("Username").single();
		TextField fullNameField = find(TextField.class).withCaption("Full Name").single();
		TextField emailField = find(TextField.class).withCaption("Email").single();
		PasswordField passwordField = find(PasswordField.class).withCaption("Password").single();
		MultiSelectComboBox<String> roleMultiSelect = find(MultiSelectComboBox.class).withCaption("Roles").single();
		Checkbox activeCheckbox = find(Checkbox.class).withCaption("Active").single();

		test(usernameField).setValue("testuser");
		test(fullNameField).setValue("Test User");
		test(emailField).setValue("test@example.com");
		test(passwordField).setValue("password123");
		roleMultiSelect.setValue(Set.of(Role.USER));
		activeCheckbox.setValue(true);

		Button saveButton = find(Button.class).withText("Save").single();
		test(saveButton).click();

		assertThat(test(grid).size()).isEqualTo(initialSize + 1);
	}

	@Test
	void save_validation_fails_for_empty_required_fields() {
		navigate(UserView.class);

		Grid<UserWithRoles> grid = find(Grid.class).single();
		var addIcon = grid.getColumnByKey("actions").getHeaderComponent();
		test((com.vaadin.flow.component.icon.SvgIcon) addIcon).click();

		Button saveButton = find(Button.class).withText("Save").single();
		test(saveButton).click();

		TextField usernameField = find(TextField.class).withCaption("Username").single();
		TextField fullNameField = find(TextField.class).withCaption("Full Name").single();
		TextField emailField = find(TextField.class).withCaption("Email").single();
		PasswordField passwordField = find(PasswordField.class).withCaption("Password").single();

		assertThat(usernameField.isInvalid()).isTrue();
		assertThat(fullNameField.isInvalid()).isTrue();
		assertThat(emailField.isInvalid()).isTrue();
		assertThat(passwordField.isInvalid()).isTrue();
	}

	@Test
	void cancel_button_clears_form() {
		navigate(UserView.class);

		Grid<UserWithRoles> grid = find(Grid.class).single();
		var addIcon = grid.getColumnByKey("actions").getHeaderComponent();
		test((com.vaadin.flow.component.icon.SvgIcon) addIcon).click();

		TextField usernameField = find(TextField.class).withCaption("Username").single();
		test(usernameField).setValue("testuser");

		Button cancelButton = find(Button.class).withText("Cancel").single();
		test(cancelButton).click();

		assertThat(usernameField.getValue()).isEmpty();
		assertThat(usernameField.isReadOnly()).isFalse();
	}

}
