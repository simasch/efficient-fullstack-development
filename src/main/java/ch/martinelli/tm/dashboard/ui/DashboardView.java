package ch.martinelli.tm.dashboard.ui;

import ch.martinelli.tm.core.security.SecurityContext;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.TaskSummary;
import ch.martinelli.tm.project.domain.ProjectService;
import ch.martinelli.tm.task.domain.TaskService;
import ch.martinelli.tm.task.ui.TaskDetailView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@PermitAll
@Route("")
public class DashboardView extends VerticalLayout implements HasDynamicTitle {

	final Grid<TaskSummary> dueSoonGrid = new Grid<>();

	public DashboardView(TaskService taskService, ProjectService projectService, SecurityContext securityContext) {
		var counts = taskService.countByStatus();

		var cards = new HorizontalLayout();
		cards.setWidthFull();
		for (TaskStatus status : TaskStatus.values()) {
			cards.add(statCard(getTranslation("task.status." + status.name()), counts.getOrDefault(status, 0), false));
		}
		cards.add(statCard(getTranslation("dashboard.status.overdue"), taskService.countOverdue(), true));
		add(cards);

		add(new H3(getTranslation("dashboard.due.soon")));
		dueSoonGrid.addColumn(TaskSummary::title)
			.setHeader(getTranslation("task.field.title"))
			.setFlexGrow(1)
			.setAutoWidth(true);
		dueSoonGrid.addColumn(new LocalDateRenderer<>(TaskSummary::dueDate, this::dateFormatter, ""))
			.setHeader(getTranslation("task.field.due.date"))
			.setAutoWidth(true);
		dueSoonGrid.setItems(taskService.findDueSoon(5));
		dueSoonGrid.setAllRowsVisible(true);
		dueSoonGrid.addItemClickListener(event -> UI.getCurrent()
			.navigate(TaskDetailView.class, new RouteParameters("taskId", String.valueOf(event.getItem().id()))));
		add(dueSoonGrid);

		securityContext.getLoggedInUser().ifPresent(user -> {
			var overviews = projectService.findOverviewByOwner(user.getId());
			if (!overviews.isEmpty()) {
				add(new H3(getTranslation("dashboard.my.projects")));
				var projects = new HorizontalLayout();
				projects.setWidthFull();
				for (var overview : overviews) {
					var card = new Div();
					card.addClassName("project-card");
					var title = new Div(overview.name());
					title.addClassName("project-card-title");
					card.add(title);
					card.add(new Div(getTranslation("dashboard.project.task.count", overview.tasks().size())));
					projects.add(card);
				}
				add(projects);
			}
		});
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.dashboard.title");
	}

	/**
	 * Resolved for every rendered cell, so the format follows the locale the user picked.
	 */
	private DateTimeFormatter dateFormatter() {
		return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(getLocale());
	}

	private Div statCard(String label, int value, boolean alert) {
		var card = new Div();
		card.addClassName("stat-card");

		var number = new Div(String.valueOf(value));
		number.addClassName("stat-number");
		if (alert && value > 0) {
			number.addClassName("alert");
		}

		var caption = new Span(label);
		caption.addClassName("stat-caption");

		card.add(number, caption);
		return card;
	}

}
