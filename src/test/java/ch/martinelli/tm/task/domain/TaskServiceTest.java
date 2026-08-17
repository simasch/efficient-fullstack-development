package ch.martinelli.tm.task.domain;

import ch.martinelli.tm.TestcontainersConfiguration;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.TaskStatus;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TaskServiceTest {

	@Autowired
	private DSLContext dsl;

	@Autowired
	private TaskService taskService;

	private Long taskId;

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

		Long projectId = dsl.insertInto(PROJECT)
			.set(PROJECT.NAME, "Book")
			.set(PROJECT.OWNER_ID, userId)
			.returningResult(PROJECT.ID)
			.fetchOne(PROJECT.ID);

		taskId = dsl.insertInto(TASK)
			.set(TASK.PROJECT_ID, projectId)
			.set(TASK.TITLE, "Write chapter 5")
			.set(TASK.STATUS, TaskStatus.OPEN)
			.set(TASK.PRIORITY, Priority.HIGH)
			.returningResult(TASK.ID)
			.fetchOne(TASK.ID);
	}

	@Test
	void save_allowsValidStatusTransition() {
		Task task = taskService.findById(taskId).orElseThrow();

		taskService.save(withStatus(task, TaskStatus.IN_PROGRESS));

		assertThat(taskService.findById(taskId).orElseThrow().status()).isEqualTo(TaskStatus.IN_PROGRESS);
	}

	@Test
	void save_rejectsInvalidStatusTransition() {
		Task task = taskService.findById(taskId).orElseThrow();

		// OPEN -> BLOCKED is not an allowed transition
		Task blocked = withStatus(task, TaskStatus.BLOCKED);
		assertThatThrownBy(() -> taskService.save(blocked)).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("cannot move");
	}

	private Task withStatus(Task task, TaskStatus status) {
		return new Task(task.id(), task.projectId(), task.title(), task.description(), status, task.priority(),
				task.dueDate(), task.estimateHours(), task.assignee(), task.version());
	}

}
