package ch.martinelli.tm.domain;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

public record TaskFilter(@Nullable String text, @Nullable TaskStatus status, @Nullable Long assigneeId,
		@Nullable Long projectId, @Nullable LocalDate dueBefore) {

	public static TaskFilter empty() {
		return new TaskFilter(null, null, null, null, null);
	}

	public TaskFilter withProjectId(@Nullable Long newProjectId) {
		return new TaskFilter(text, status, assigneeId, newProjectId, dueBefore);
	}

}
