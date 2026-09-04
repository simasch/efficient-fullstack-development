package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.User;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.flow.data.binder.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A component without a view: no Spring, no PostgreSQL, no routes -- the form's only
 * collaborators are the two lists passed into its constructor.
 */
class TaskFormTest {

	private static final List<User> USERS = List.of(new User(1L, "simon", "Simon Martinelli"));

	private static final List<ProjectListItem> PROJECTS = List.of(new ProjectListItem(10L, "Book", "Simon", 3));

	@Test
	void an_estimate_outside_the_allowed_range_is_rejected() {
		var form = new TaskForm(USERS, PROJECTS);

		try (var window = BrowserlessUIContext.forComponent(form)) {
			form.setTask(Task.newTask());

			window.test(form.title).setValue("Write chapter 6");
			window.test(form.project).selectItem("Book");
			window.test(form.estimateHours).setValue(5000);

			assertThatThrownBy(form::getTask).isInstanceOf(ValidationException.class);
			assertThat(form.estimateHours.isInvalid()).isTrue();
		}
	}

}
