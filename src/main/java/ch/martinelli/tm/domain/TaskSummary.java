package ch.martinelli.tm.domain;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * A compact task projection: id, title and due date. Used where a whole
 * {@link TaskListItem} would be too much, for example in the dashboard.
 */
public record TaskSummary(Long id, String title, @Nullable LocalDate dueDate) {
}
