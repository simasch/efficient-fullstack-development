package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.core.ui.AbstractBrowserlessTest;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.Role;
import ch.martinelli.tm.domain.TaskStatus;
import com.vaadin.flow.component.UI;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Locale;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static ch.martinelli.tm.db.tables.UserRole.USER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The grid must not print an ISO date: the due date is formatted for the locale the user
 * picked with the language switcher, and so are the column headers.
 */
@WithMockUser(username = "alice", roles = Role.USER)
class TaskGridI18nTest extends AbstractBrowserlessTest {

	private static final int DUE_DATE_COLUMN = 1;

	@Autowired
	private DSLContext dsl;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(USER_ROLE).execute();
		dsl.deleteFrom(APP_USER).execute();

		Long ownerId = dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "alice")
			.set(APP_USER.FULL_NAME, "Alice Meyer")
			.set(APP_USER.EMAIL, new EmailAddress("alice@example.com"))
			.set(APP_USER.PASSWORD_HASH, "{noop}alice")
			.returningResult(APP_USER.ID)
			.fetchOne(APP_USER.ID);
		Long projectId = dsl.insertInto(PROJECT)
			.set(PROJECT.NAME, "Website Relaunch")
			.set(PROJECT.OWNER_ID, ownerId)
			.returningResult(PROJECT.ID)
			.fetchOne(PROJECT.ID);

		dsl.insertInto(TASK)
			.set(TASK.PROJECT_ID, projectId)
			.set(TASK.TITLE, "Offline mode")
			.set(TASK.STATUS, TaskStatus.OPEN)
			.set(TASK.PRIORITY, Priority.HIGH)
			.set(TASK.DUE_DATE, LocalDate.of(2026, 3, 9))
			.execute();
	}

	@Test
	void due_date_is_formatted_for_the_english_locale() {
		UI.getCurrent().setLocale(Locale.ENGLISH);

		navigate(TaskListView.class);

		TaskGrid grid = find(TaskGrid.class).single();
		assertThat(test(grid).getHeaderCell(DUE_DATE_COLUMN)).isEqualTo("Due date");
		assertThat(test(grid).getCellText(0, DUE_DATE_COLUMN)).isEqualTo("Mar 9, 2026");
	}

	@Test
	void due_date_is_formatted_for_the_german_locale() {
		UI.getCurrent().setLocale(Locale.GERMAN);

		navigate(TaskListView.class);

		TaskGrid grid = find(TaskGrid.class).single();
		assertThat(test(grid).getHeaderCell(DUE_DATE_COLUMN)).isEqualTo("Fälligkeitsdatum");
		assertThat(test(grid).getCellText(0, DUE_DATE_COLUMN)).isEqualTo("09.03.2026");
	}

}
