package ch.martinelli.tm.task.domain;

import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.domain.TaskListItem;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.TaskSummary;
import ch.martinelli.tm.domain.User;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Records;
import org.jooq.exception.DataChangedException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.currentLocalDate;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.noCondition;

@Repository
public class TaskRepository {

	private final DSLContext dsl;

	public TaskRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	public List<TaskListItem> findTasks(TaskFilter filter, int offset, int limit, List<OrderField<?>> orderBy) {
		return dsl
			.select(TASK.ID, TASK.TITLE, TASK.DUE_DATE, TASK.STATUS, TASK.assignee().FULL_NAME, TASK.project().NAME)
			.from(TASK)
			.where(toCondition(filter))
			.orderBy(orderBy.isEmpty() ? List.of(TASK.DUE_DATE.asc(), TASK.ID.asc()) : orderBy)
			.offset(offset)
			.limit(limit)
			.fetch(Records.mapping(TaskListItem::new));
	}

	public int countTasks(TaskFilter filter) {
		return dsl.fetchCount(TASK, toCondition(filter));
	}

	public Optional<Task> findById(long id) {
		return dsl
			.select(TASK.ID, TASK.PROJECT_ID, TASK.TITLE, TASK.DESCRIPTION, TASK.STATUS, TASK.PRIORITY, TASK.DUE_DATE,
					TASK.ESTIMATE_HOURS, TASK.assignee().ID, TASK.assignee().USERNAME, TASK.assignee().FULL_NAME,
					TASK.VERSION)
			.from(TASK)
			.where(TASK.ID.eq(id))
			.fetchOptional(r -> new Task(r.value1(), r.value2(), r.value3(), r.value4(), r.value5(), r.value6(),
					r.value7(), r.value8(), r.value9() == null ? null : new User(r.value9(), r.value10(), r.value11()),
					r.value12()));
	}

	public long insert(Task task) {
		return dsl.insertInto(TASK)
			.set(TASK.PROJECT_ID, task.projectId())
			.set(TASK.TITLE, task.title())
			.set(TASK.DESCRIPTION, task.description())
			.set(TASK.STATUS, task.status())
			.set(TASK.PRIORITY, task.priority())
			.set(TASK.DUE_DATE, task.dueDate())
			.set(TASK.ESTIMATE_HOURS, task.estimateHours())
			.set(TASK.ASSIGNEE_ID, task.assignee() == null ? null : task.assignee().id())
			.returningResult(TASK.ID)
			.fetchSingle()
			.value1();
	}

	/**
	 * Updates the task with an explicit optimistic locking check. The record API's
	 * built-in locking works when the same record instance lives across the edit; here
	 * the task made a round trip through the UI, so the version is compared in the UPDATE
	 * itself.
	 */
	public void update(Task task) {
		int rows = dsl.update(TASK)
			.set(TASK.PROJECT_ID, task.projectId())
			.set(TASK.TITLE, task.title())
			.set(TASK.DESCRIPTION, task.description())
			.set(TASK.STATUS, task.status())
			.set(TASK.PRIORITY, task.priority())
			.set(TASK.DUE_DATE, task.dueDate())
			.set(TASK.ESTIMATE_HOURS, task.estimateHours())
			.set(TASK.ASSIGNEE_ID, task.assignee() == null ? null : task.assignee().id())
			.set(TASK.VERSION, TASK.VERSION.plus(1))
			.where(TASK.ID.eq(task.id()), TASK.VERSION.eq(task.version()))
			.execute();
		if (rows == 0) {
			throw new DataChangedException("Task %d was changed or deleted by another user".formatted(task.id()));
		}
	}

	public void assign(long taskId, long assigneeId) {
		dsl.update(TASK).set(TASK.ASSIGNEE_ID, assigneeId).where(TASK.ID.eq(taskId)).execute();
	}

	public Map<TaskStatus, Integer> countByStatus() {
		return dsl.select(TASK.STATUS, count()).from(TASK).groupBy(TASK.STATUS).fetchMap(TASK.STATUS, count());
	}

	public int countOverdue() {
		return dsl.fetchCount(TASK, TASK.DUE_DATE.lt(currentLocalDate()).and(TASK.STATUS.ne(TaskStatus.DONE)));
	}

	public List<TaskSummary> findDueSoon(int limit) {
		return dsl.select(TASK.ID, TASK.TITLE, TASK.DUE_DATE)
			.from(TASK)
			.where(TASK.STATUS.ne(TaskStatus.DONE), TASK.DUE_DATE.isNotNull())
			.orderBy(TASK.DUE_DATE.asc(), TASK.ID.asc())
			.limit(limit)
			.fetch(Records.mapping(TaskSummary::new));
	}

	private Condition toCondition(TaskFilter filter) {
		Condition condition = noCondition();

		if (StringUtils.hasText(filter.text())) {
			String pattern = "%" + filter.text().toLowerCase(Locale.ROOT) + "%";
			condition = condition.and(lower(TASK.TITLE).like(pattern).or(lower(TASK.DESCRIPTION).like(pattern)));
		}
		if (filter.status() != null) {
			condition = condition.and(TASK.STATUS.eq(filter.status()));
		}
		if (filter.assigneeId() != null) {
			condition = condition.and(TASK.ASSIGNEE_ID.eq(filter.assigneeId()));
		}
		if (filter.projectId() != null) {
			condition = condition.and(TASK.PROJECT_ID.eq(filter.projectId()));
		}
		if (filter.dueBefore() != null) {
			condition = condition.and(TASK.DUE_DATE.lt(filter.dueBefore()));
		}

		return condition;
	}

}
