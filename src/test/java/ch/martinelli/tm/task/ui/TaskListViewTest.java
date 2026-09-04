package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.core.ui.AbstractBrowserlessTest;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.Role;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.user.ui.UserView;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The tests of Chapter 6. They drive the whole vertical slice -- view, components,
 * service, jOOQ, PostgreSQL -- without a browser.
 */
@WithMockUser(username = "simon", roles = Role.USER)
class TaskListViewTest extends AbstractBrowserlessTest {

	@Autowired
	private DSLContext dsl;

	private Long projectId;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(APP_USER).execute();

		Long userId = dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "simon")
			.set(APP_USER.FULL_NAME, "Simon Martinelli")
			.set(APP_USER.EMAIL, new EmailAddress("simon@example.com"))
			.returningResult(APP_USER.ID)
			.fetchOne(APP_USER.ID);

		projectId = dsl.insertInto(PROJECT)
			.set(PROJECT.NAME, "Book")
			.set(PROJECT.OWNER_ID, userId)
			.returningResult(PROJECT.ID)
			.fetchOne(PROJECT.ID);

		insertTask("Write chapter 6", TaskStatus.OPEN, LocalDate.of(2026, 3, 1), userId);
		insertTask("Review chapter 5", TaskStatus.IN_PROGRESS, LocalDate.of(2026, 4, 1), userId);
	}

	private void insertTask(String title, TaskStatus status, LocalDate dueDate, Long assigneeId) {
		dsl.insertInto(TASK)
			.set(TASK.PROJECT_ID, projectId)
			.set(TASK.ASSIGNEE_ID, assigneeId)
			.set(TASK.TITLE, title)
			.set(TASK.STATUS, status)
			.set(TASK.PRIORITY, Priority.MEDIUM)
			.set(TASK.DUE_DATE, dueDate)
			.execute();
	}

	@Test
	void grid_shows_the_tasks_ordered_by_due_date() {
		var view = navigate(TaskListView.class);

		assertThat(test(view.taskGrid).size()).isEqualTo(2);
		assertThat(test(view.taskGrid).getCellText(0, 0)).isEqualTo("Write chapter 6");
		assertThat(test(view.taskGrid).getCellText(1, 0)).isEqualTo("Review chapter 5");
	}

	@Test
	void status_filter_narrows_the_grid() {
		var view = navigate(TaskListView.class);

		test(view.filterBar.status).selectItem("In progress");

		assertThat(test(view.taskGrid).size()).isEqualTo(1);
		assertThat(test(view.taskGrid).getCellText(0, 0)).isEqualTo("Review chapter 5");
	}

	@Test
	void text_filter_matches_the_title() {
		var view = navigate(TaskListView.class);

		test(view.filterBar.text).setValue("chapter 6");

		assertThat(test(view.taskGrid).size()).isEqualTo(1);
	}

	@Test
	void selecting_a_row_opens_the_editor_with_the_task() {
		var view = navigate(TaskListView.class);

		test(view.taskGrid).select(0);
		roundTrip();

		assertThat(view.editorDialog.isOpened()).isTrue();
		assertThat(view.editorDialog.form.title.getValue()).isEqualTo("Write chapter 6");
		assertThat(view.editorDialog.form.project.getValue().name()).isEqualTo("Book");
	}

	@Test
	void saving_a_task_closes_the_dialog_refreshes_the_grid_and_notifies() {
		var view = navigate(TaskListView.class);
		test(view.taskGrid).select(0);
		roundTrip();

		test(view.editorDialog.form.title).setValue("Write chapter 6 and 7");
		test(view.editorDialog.save).click();

		assertThat(view.editorDialog.isOpened()).isFalse();
		assertThat(test(find(Notification.class).single()).getText()).isEqualTo("Task saved");
		assertThat(test(view.taskGrid).getCellText(0, 0)).isEqualTo("Write chapter 6 and 7");
	}

	@Test
	void an_empty_form_is_rejected_before_the_service_is_called() {
		var view = navigate(TaskListView.class);

		test(view.newTask).click();
		roundTrip();
		test(view.editorDialog.save).click();

		assertThat(view.editorDialog.form.title.isInvalid()).isTrue();
		assertThat(view.editorDialog.form.project.isInvalid()).isTrue();
		assertThat(view.editorDialog.isOpened()).isTrue();
		assertThat(test(view.taskGrid).size()).isEqualTo(2);
	}

	@Test
	void a_forbidden_status_transition_is_reported_to_the_user() {
		var view = navigate(TaskListView.class);
		test(view.taskGrid).select(0); // "Write chapter 6" is OPEN
		roundTrip();

		test(view.editorDialog.form.status).selectItem("Blocked");
		test(view.editorDialog.save).click();

		assertThat(view.editorDialog.isOpened()).isTrue();
		assertThat(find(NativeLabel.class).withText("A task in status Open cannot move to Blocked.").exists()).isTrue();
	}

	@Test
	@WithMockUser(username = "simon", roles = Role.USER)
	void a_plain_user_cannot_reach_the_user_administration() {
		assertThatThrownBy(() -> navigate(UserView.class)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@WithMockUser(username = "admin", roles = Role.ADMIN)
	void an_administrator_can_reach_the_user_administration() {
		assertThat(navigate(UserView.class)).isNotNull();
	}

}
