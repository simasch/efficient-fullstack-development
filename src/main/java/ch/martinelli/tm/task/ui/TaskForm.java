package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.domain.Priority;
import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.User;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.binder.ReadOnlyHasValue;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The task form is a reusable component: it works standalone in the detail view and
 * inside the editor dialog. Because {@link Task} is a record, the binder reads with
 * {@code readBean} and writes with {@code writeRecord}, which builds a new instance.
 */
public class TaskForm extends Composite<FormLayout> {

	final TextField title = new TextField("Title");

	final TextArea description = new TextArea("Description");

	final DatePicker dueDate = new DatePicker("Due date");

	final ComboBox<Priority> priority = new ComboBox<>("Priority");

	final ComboBox<TaskStatus> status = new ComboBox<>("Status");

	final ComboBox<User> assignee = new ComboBox<>("Assignee");

	final ComboBox<ProjectListItem> project = new ComboBox<>("Project");

	final IntegerField estimateHours = new IntegerField("Estimate (hours)");

	private final Binder<Task> binder = new Binder<>(Task.class);

	private final transient List<ProjectListItem> projects;

	public TaskForm(List<User> users, List<ProjectListItem> projects) {
		this.projects = projects;

		priority.setItems(Priority.values());
		status.setItems(TaskStatus.values());
		assignee.setItems(users);
		assignee.setItemLabelGenerator(User::fullName);
		assignee.setClearButtonVisible(true);
		project.setItems(projects);
		project.setItemLabelGenerator(ProjectListItem::name);

		// Records have no setters, so the bindings are by record component name — the
		// binder reads through the accessor and writeRecord() calls the canonical
		// constructor with the bound values.
		binder.forField(title)
			.asRequired("A title is required")
			.withValidator(new StringLengthValidator("Maximum 200 characters", 0, 200))
			.bind("title");

		binder.forField(description).withNullRepresentation("").bind("description");

		binder.forField(dueDate).bind("dueDate");

		binder.forField(priority).asRequired("Select a priority").bind("priority");

		binder.forField(status).asRequired("Select a status").bind("status");

		binder.forField(assignee).bind("assignee");

		binder.forField(project)
			.asRequired("Select a project")
			.withConverter(item -> item == null ? null : item.id(), this::toProjectListItem)
			.bind("projectId");

		binder.forField(estimateHours)
			.withValidator(new IntegerRangeValidator("Between 1 and 1000 hours", 1, 1000))
			.bind("estimateHours");

		// The id and the version are not editable, but writeRecord() requires every
		// record component to be bound — invisible bindings carry them through the edit.
		binder.forField(new ReadOnlyHasValue<Long>(_ -> {
		})).bind("id");
		binder.forField(new ReadOnlyHasValue<Integer>(_ -> {
		})).bind("version");

		getContent().add(title, project, status, priority, dueDate, assignee, estimateHours, description);
		getContent().setColspan(description, 2);
	}

	public void setTask(Task task) {
		binder.readRecord(task);
	}

	public Task getTask() throws ValidationException {
		return binder.writeRecord();
	}

	private @Nullable ProjectListItem toProjectListItem(@Nullable Long projectId) {
		if (projectId == null) {
			return null;
		}
		return projects.stream().filter(p -> p.id().equals(projectId)).findFirst().orElse(null);
	}

}
