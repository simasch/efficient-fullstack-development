package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.User;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

import java.util.List;

/**
 * The filter bar does not know that a grid exists — it fires a {@link FilterChangeEvent}
 * and whoever is interested listens.
 */
public class TaskFilterBar extends Composite<HorizontalLayout> {

	final TextField text = new TextField();

	final ComboBox<TaskStatus> status = new ComboBox<>();

	final ComboBox<User> assignee = new ComboBox<>();

	final ComboBox<ProjectListItem> project = new ComboBox<>();

	final DatePicker dueBefore = new DatePicker();

	public TaskFilterBar(List<User> users, List<ProjectListItem> projects) {
		// Every control has an aria label as well as a placeholder. A placeholder is not
		// an accessible name — a screen reader loses it as soon as the field has a value
		// —
		// and a bare "Status" would collide with the status field of the editor form.
		text.setPlaceholder("Search");
		text.setAriaLabel("Search tasks");
		text.setClearButtonVisible(true);
		text.setValueChangeMode(ValueChangeMode.LAZY);

		status.setPlaceholder("Status");
		status.setAriaLabel("Filter by status");
		status.setItems(TaskStatus.values());
		status.setClearButtonVisible(true);

		assignee.setPlaceholder("Assignee");
		assignee.setAriaLabel("Filter by assignee");
		assignee.setItems(users);
		assignee.setItemLabelGenerator(User::fullName);
		assignee.setClearButtonVisible(true);

		project.setPlaceholder("Project");
		project.setAriaLabel("Filter by project");
		project.setItems(projects);
		project.setItemLabelGenerator(ProjectListItem::name);
		project.setClearButtonVisible(true);

		dueBefore.setPlaceholder("Due before");
		dueBefore.setAriaLabel("Filter by due date");
		dueBefore.setClearButtonVisible(true);

		text.addValueChangeListener(_ -> fireFilterChange());
		status.addValueChangeListener(_ -> fireFilterChange());
		assignee.addValueChangeListener(_ -> fireFilterChange());
		project.addValueChangeListener(_ -> fireFilterChange());
		dueBefore.addValueChangeListener(_ -> fireFilterChange());

		getContent().add(text, status, assignee, project, dueBefore);
	}

	private void fireFilterChange() {
		fireEvent(new FilterChangeEvent(this, buildFilter()));
	}

	TaskFilter buildFilter() {
		return new TaskFilter(text.getValue().isBlank() ? null : text.getValue(), status.getValue(),
				assignee.getValue() == null ? null : assignee.getValue().id(),
				project.getValue() == null ? null : project.getValue().id(), dueBefore.getValue());
	}

	public static class FilterChangeEvent extends ComponentEvent<TaskFilterBar> {

		private final transient TaskFilter filter;

		public FilterChangeEvent(TaskFilterBar source, TaskFilter filter) {
			super(source, false);
			this.filter = filter;
		}

		public TaskFilter getFilter() {
			return filter;
		}

	}

	public Registration addFilterChangeListener(ComponentEventListener<FilterChangeEvent> listener) {
		return addListener(FilterChangeEvent.class, listener);
	}

}
