package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.core.domain.UserService;
import ch.martinelli.tm.core.ui.components.Notifier;
import ch.martinelli.tm.core.ui.i18n.BusinessRuleMessage;
import ch.martinelli.tm.domain.BusinessRuleException;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.project.domain.ProjectService;
import ch.martinelli.tm.task.domain.TaskService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.jooq.exception.DataChangedException;

/**
 * A URL that identifies a task is a URL a user can bookmark and paste into a chat
 * message. The route parameter is validated in beforeEnter; an unknown id reroutes to the
 * 404 view.
 */
@PermitAll
@Route("task/:taskId([0-9]+)")
public class TaskDetailView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	private final transient TaskService taskService;

	final TaskForm form;

	final Button save = new Button(getTranslation("action.save"));

	public TaskDetailView(TaskService taskService, UserService userService, ProjectService projectService) {
		this.taskService = taskService;

		form = new TaskForm(userService.findAllActive(), projectService.findAll());

		save.addThemeVariants(ButtonVariant.PRIMARY);
		save.addClickListener(_ -> save());

		add(form, save);
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.task.title");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		event.getRouteParameters()
			.get("taskId")
			.map(Long::valueOf)
			.flatMap(taskService::findById)
			.ifPresentOrElse(this::showTask, () -> event.rerouteToError(NotFoundException.class));
	}

	private void showTask(Task task) {
		form.setTask(task);
	}

	private void save() {
		try {
			taskService.save(form.getTask());
			Notifier.success(getTranslation("notification.task.saved"));
			UI.getCurrent().navigate(TaskListView.class);
		}
		catch (ValidationException e) {
			// the form marks the invalid fields itself
		}
		catch (DataChangedException e) {
			Notifier.error(getTranslation("notification.task.concurrent.modification"));
		}
		catch (BusinessRuleException e) {
			Notifier.error(BusinessRuleMessage.translate(e));
		}
	}

}
