package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.core.domain.UserService;
import ch.martinelli.tm.core.ui.components.Notifier;
import ch.martinelli.tm.core.ui.i18n.BusinessRuleMessage;
import ch.martinelli.tm.domain.BusinessRuleException;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.project.domain.ProjectService;
import ch.martinelli.tm.task.domain.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jooq.exception.DataChangedException;

/**
 * The view owns the orchestration, the components own the presentation. Components
 * communicate through events and never talk to services — only the view does.
 */
@PermitAll
@Route("tasks")
public class TaskListView extends VerticalLayout implements HasDynamicTitle {

	final TaskGrid taskGrid;

	final TaskFilterBar filterBar;

	final TaskEditorDialog editorDialog;

	final Button newTask = new Button(getTranslation("action.task.new"));

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
				Notifier.success(getTranslation("notification.task.saved"));
			}
			catch (DataChangedException e) {
				Notifier.error(getTranslation("notification.task.concurrent.modification"));
			}
			catch (BusinessRuleException e) {
				Notifier.error(BusinessRuleMessage.translate(e));
			}
		});
		editorDialog.addOpenedChangeListener(event -> {
			if (!event.isOpened()) {
				taskGrid.deselectAll();
			}
		});

		newTask.addThemeVariants(ButtonVariant.PRIMARY);
		newTask.addClickListener(_ -> editorDialog.edit(Task.newTask()));

		var toolbar = new HorizontalLayout(filterBar, newTask);
		toolbar.setWidthFull();
		toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

		setSizeFull();
		add(toolbar, taskGrid);
		expand(taskGrid);
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.tasks.title");
	}

}
