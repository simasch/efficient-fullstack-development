package ch.martinelli.tm.project.domain;

import ch.martinelli.tm.TestcontainersConfiguration;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.ProjectOverview;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.TaskSummary;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

import static ch.martinelli.tm.db.tables.AppUser.APP_USER;
import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ProjectRepositoryTest {

	@Autowired
	private DSLContext dsl;

	@Autowired
	private ProjectRepository projectRepository;

	private Long ownerId;

	@BeforeEach
	void setUp() {
		dsl.deleteFrom(TASK).execute();
		dsl.deleteFrom(PROJECT).execute();
		dsl.deleteFrom(APP_USER).execute();

		ownerId = dsl.insertInto(APP_USER)
			.set(APP_USER.USERNAME, "simon")
			.set(APP_USER.FULL_NAME, "Simon Martinelli")
			.set(APP_USER.EMAIL, new EmailAddress("simon@example.com"))
			.returningResult(APP_USER.ID)
			.fetchOne(APP_USER.ID);

		Long projectId = dsl.insertInto(PROJECT)
			.set(PROJECT.NAME, "Book")
			.set(PROJECT.OWNER_ID, ownerId)
			.returningResult(PROJECT.ID)
			.fetchOne(PROJECT.ID);

		for (int i = 0; i < 3; i++) {
			dsl.insertInto(TASK)
				.set(TASK.PROJECT_ID, projectId)
				.set(TASK.TITLE, "Task " + i)
				.set(TASK.STATUS, i == 0 ? TaskStatus.DONE : TaskStatus.OPEN)
				.set(TASK.PRIORITY, Priority.MEDIUM)
				.set(TASK.DUE_DATE, LocalDate.now().plusDays(i))
				.execute();
		}
	}

	@Test
	void findAll_countsOpenTasks() {
		var projects = projectRepository.findAll();

		assertThat(projects).hasSize(1);
		assertThat(projects.getFirst().name()).isEqualTo("Book");
		assertThat(projects.getFirst().ownerName()).isEqualTo("Simon Martinelli");
		// one of the three tasks is DONE
		assertThat(projects.getFirst().openTasks()).isEqualTo(2);
	}

	@Test
	void findOverviewByOwner_nestsTasksInOneQuery() {
		List<ProjectOverview> overview = projectRepository.findOverviewByOwner(ownerId);

		assertThat(overview).hasSize(1);
		assertThat(overview.getFirst().name()).isEqualTo("Book");
		assertThat(overview.getFirst().tasks()).hasSize(3)
			.extracting(TaskSummary::title)
			.containsExactly("Task 0", "Task 1", "Task 2");
	}

}
