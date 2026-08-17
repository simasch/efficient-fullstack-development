package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.core.domain.UserService;
import ch.martinelli.tm.core.ui.components.Notifier;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.project.domain.ProjectService;
import ch.martinelli.tm.task.domain.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jooq.exception.DataChangedException;

import java.util.Objects;

/**
 * The view owns the orchestration, the components own the presentation. Components
 * communicate through events and never talk to services — only the view does.
 */
@PermitAll
@Route("tasks")
@PageTitle("Tasks")
public class TaskListView extends VerticalLayout {

	final TaskGrid taskGrid;

	final TaskFilterBar filterBar;

	final TaskEditorDialog editorDialog;

	final Button newTask = new Button("New task");

	public TaskListView(TaskService taskService, UserService userService, ProjectService projectService) {
		var users = userService.findAllActive();
		var projects = projectService.findAll();

		this.filterBar = new TaskFilterBar(users, projects);
		this.taskGrid = new TaskGrid();
		this.editorDialog = new TaskEditorDialog(users, projects);

		taskGrid.setItems(query -> taskService
			.findTasks(taskGrid.getFilter(), query.getOffset(), query.getLimit(),
					TaskGrid.toOrderFields(query.getSortOrders()))
			.stream(), query -> taskService.countTasks(taskGrid.getFilter()));

		filterBar.addFilterChangeListener(event -> taskGrid.setFilter(event.getFilter()));

		taskGrid.addSelectionListener(event -> event.getFirstSelectedItem()
			.flatMap(item -> taskService.findById(item.id()))
			.ifPresent(editorDialog::edit));

		editorDialog.addSaveListener(event -> {
			try {
				taskService.save(event.getTask());
				editorDialog.close();
				taskGrid.refresh();
				Notifier.success("Task saved");
			}
			catch (DataChangedException e) {
				Notifier.error("Someone else changed this task. Please reload and try again.");
			}
			catch (IllegalStateException e) {
				Notifier.error(Objects.requireNonNullElse(e.getMessage(), "The task could not be saved"));
			}
		});
		editorDialog.addOpenedChangeListener(event -> {
			if (!event.isOpened()) {
				taskGrid.deselectAll();
			}
		});

		newTask.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		newTask.addClickListener(_ -> editorDialog.edit(Task.newTask()));

		var toolbar = new HorizontalLayout(filterBar, newTask);
		toolbar.setWidthFull();
		toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

		setSizeFull();
		add(toolbar, taskGrid);
		expand(taskGrid);
	}

}
