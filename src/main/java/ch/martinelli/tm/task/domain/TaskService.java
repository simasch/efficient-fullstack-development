package ch.martinelli.tm.task.domain;

import ch.martinelli.tm.domain.BusinessRuleException;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.domain.TaskListItem;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.TaskSummary;
import org.jooq.OrderField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService {

	private final TaskRepository taskRepository;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Transactional(readOnly = true)
	public List<TaskListItem> findTasks(TaskFilter filter, int offset, int limit, List<OrderField<?>> orderBy) {
		return taskRepository.findTasks(filter, offset, limit, orderBy);
	}

	@Transactional(readOnly = true)
	public int countTasks(TaskFilter filter) {
		return taskRepository.countTasks(filter);
	}

	@Transactional(readOnly = true)
	public Optional<Task> findById(long id) {
		return taskRepository.findById(id);
	}

	/**
	 * Saves the task. The status transition rule is enforced here, on the server —
	 * whatever the form did, the service is the boundary that guarantees it.
	 */
	@Transactional
	public void save(Task task) {
		Long id = task.id();
		if (id == null) {
			taskRepository.insert(task);
		}
		else {
			var current = taskRepository.findById(id)
				.orElseThrow(() -> new BusinessRuleException("error.task.no.longer.exists", id));
			if (!current.status().canTransitionTo(task.status())) {
				throw new BusinessRuleException("error.task.invalid.status.transition", current.status(),
						task.status());
			}
			taskRepository.update(task);
		}
	}

	@Transactional
	public void assign(long taskId, long assigneeId) {
		taskRepository.assign(taskId, assigneeId);
	}

	@Transactional(readOnly = true)
	public Map<TaskStatus, Integer> countByStatus() {
		return taskRepository.countByStatus();
	}

	@Transactional(readOnly = true)
	public int countOverdue() {
		return taskRepository.countOverdue();
	}

	@Transactional(readOnly = true)
	public List<TaskSummary> findDueSoon(int limit) {
		return taskRepository.findDueSoon(limit);
	}

}
