package ch.martinelli.tm.domain;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * The projection for the task grid: exactly the six columns the grid displays.
 */
public record TaskListItem(Long id, String title, @Nullable LocalDate dueDate, TaskStatus status,
		@Nullable String assigneeName, String projectName) {
}
