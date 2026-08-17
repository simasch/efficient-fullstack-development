package ch.martinelli.tm.task.domain;

import ch.martinelli.tm.TestcontainersConfiguration;
import ch.martinelli.tm.db.tables.records.TaskRecord;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.domain.TaskListItem;
import ch.martinelli.tm.domain.TaskStatus;
import org.jooq.DSLContext;
import org.jooq.exception.DataChangedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TaskRepositoryTest {

	@Autowired
	private DSLContext dsl;

	@Autowired
	private TaskRepository taskRepository;

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
	}

	@Test
	void findTasks_filtersByStatusAndPagesCorrectly() {
		insertTasks(15, TaskStatus.OPEN);
		insertTasks(5, TaskStatus.DONE);

		TaskFilter filter = new TaskFilter(null, TaskStatus.OPEN, null, projectId, null);

		List<TaskListItem> firstPage = taskRepository.findTasks(filter, 0, 10, List.of());
		List<TaskListItem> secondPage = taskRepository.findTasks(filter, 10, 10, List.of());

		assertThat(firstPage).hasSize(10);
		assertThat(secondPage).hasSize(5);
		assertThat(taskRepository.countTasks(filter)).isEqualTo(15);
		assertThat(firstPage).extracting(TaskListItem::id)
			.doesNotContainAnyElementsOf(secondPage.stream().map(TaskListItem::id).toList());
	}

	@Test
	void findTasks_filtersByTextInTitleAndDescription() {
		insertTasks(3, TaskStatus.OPEN);

		TaskFilter filter = new TaskFilter("task 1", null, null, projectId, null);

		List<TaskListItem> result = taskRepository.findTasks(filter, 0, 10, List.of());

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().title()).isEqualTo("Task 1");
	}

	@Test
	void findTasks_filtersByDueBefore() {
		insertTasks(5, TaskStatus.OPEN);

		TaskFilter filter = new TaskFilter(null, null, null, projectId, LocalDate.now().plusDays(2));

		assertThat(taskRepository.countTasks(filter)).isEqualTo(2);
	}

	@Test
	void store_withStaleVersion_throwsDataChangedException() {
		Long taskId = insertTasks(1, TaskStatus.OPEN).getFirst();

		TaskRecord first = dsl.fetchSingle(TASK, TASK.ID.eq(taskId));
		TaskRecord second = dsl.fetchSingle(TASK, TASK.ID.eq(taskId));

		first.setTitle("Changed by the first user");
		first.store();

		second.setTitle("Changed by the second user");

		assertThatThrownBy(second::store).isInstanceOf(DataChangedException.class);
	}

	@Test
	void update_withStaleVersion_throwsDataChangedException() {
		Long taskId = insertTasks(1, TaskStatus.OPEN).getFirst();

		var task = taskRepository.findById(taskId).orElseThrow();
		taskRepository.update(task);

		// the version in hand is now stale
		assertThatThrownBy(() -> taskRepository.update(task)).isInstanceOf(DataChangedException.class);
	}

	private List<Long> insertTasks(int count, TaskStatus status) {
		List<Long> ids = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			ids.add(dsl.insertInto(TASK)
				.set(TASK.PROJECT_ID, projectId)
				.set(TASK.TITLE, "Task " + i)
				.set(TASK.STATUS, status)
				.set(TASK.PRIORITY, Priority.MEDIUM)
				.set(TASK.DUE_DATE, LocalDate.now().plusDays(i))
				.returningResult(TASK.ID)
				.fetchOne(TASK.ID));
		}
		return ids;
	}

}
