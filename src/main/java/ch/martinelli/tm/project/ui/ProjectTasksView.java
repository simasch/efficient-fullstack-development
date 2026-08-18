package ch.martinelli.tm.project.ui;

import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.task.domain.TaskService;
import ch.martinelli.tm.task.ui.TaskGrid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import ch.martinelli.tm.project.domain.ProjectService;
import com.vaadin.flow.spring.annotation.RouteScopeOwner;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(value = "project/:projectId([0-9]+)/tasks", layout = ProjectLayout.class)
public class ProjectTasksView extends VerticalLayout implements BeforeEnterObserver, HasDynamicTitle {

	private final transient ProjectService projectService;

	private final transient SelectedProject selectedProject;

	final TaskGrid taskGrid = new TaskGrid();

	public ProjectTasksView(@RouteScopeOwner(ProjectLayout.class) SelectedProject selectedProject,
			TaskService taskService, ProjectService projectService) {
		this.selectedProject = selectedProject;
		this.projectService = projectService;

		taskGrid.setItems(query -> taskService
			.findTasks(taskGrid.getFilter(), query.getOffset(), query.getLimit(),
					TaskGrid.toOrderFields(query.getSortOrders()))
			.stream(), query -> taskService.countTasks(taskGrid.getFilter()));

		setSizeFull();
		add(taskGrid);
		expand(taskGrid);
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.project.tasks.title");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		event.getRouteParameters()
			.get("projectId")
			.map(Long::valueOf)
			.flatMap(projectService::findById)
			.ifPresentOrElse(project -> {
				selectedProject.setProjectId(project.getId());
				selectedProject.setName(project.getName());
				taskGrid.setFilter(TaskFilter.empty().withProjectId(project.getId()));
			}, () -> event.rerouteToError(NotFoundException.class));
	}

}
