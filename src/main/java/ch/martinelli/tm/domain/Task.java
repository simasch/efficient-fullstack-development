package ch.martinelli.tm.domain;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * The task as the application works with it: the editable columns of the task table plus
 * the assignee resolved to a {@link User}. The id is null for a task that has not been
 * saved yet, and the version carries the optimistic locking counter through an edit.
 */
public record Task(@Nullable Long id, @Nullable Long projectId, String title, @Nullable String description,
		TaskStatus status, Priority priority, @Nullable LocalDate dueDate, @Nullable Integer estimateHours,
		@Nullable User assignee, Integer version) {

	public static Task newTask() {
		return new Task(null, null, "", null, TaskStatus.OPEN, Priority.MEDIUM, null, null, null, 0);
	}

}
