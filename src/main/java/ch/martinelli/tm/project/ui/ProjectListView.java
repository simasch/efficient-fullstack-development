package ch.martinelli.tm.project.ui;

import ch.martinelli.tm.core.security.SecurityContext;
import ch.martinelli.tm.core.ui.components.Notifier;
import ch.martinelli.tm.db.tables.records.ProjectRecord;
import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.Role;
import ch.martinelli.tm.project.domain.ProjectService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("projects")
@PageTitle("Projects")
public class ProjectListView extends VerticalLayout {

	private final transient ProjectService projectService;

	final Grid<ProjectListItem> grid = new Grid<>();

	final Button newProject = new Button("New project");

	public ProjectListView(ProjectService projectService, SecurityContext securityContext) {
		this.projectService = projectService;

		grid.addColumn(ProjectListItem::name).setHeader("Name").setFlexGrow(1).setAutoWidth(true);
		grid.addColumn(ProjectListItem::ownerName).setHeader("Owner").setAutoWidth(true);
		grid.addColumn(ProjectListItem::openTasks).setHeader("Open tasks").setAutoWidth(true);
		grid.setItems(projectService.findAll());
		grid.addItemClickListener(event -> UI.getCurrent()
			.navigate(ProjectTasksView.class, new RouteParameters("projectId", String.valueOf(event.getItem().id()))));

		// Route-level access control is declared with annotations; in-view decisions —
		// like who may create a project — are made programmatically.
		newProject.setVisible(securityContext.hasRole(Role.ADMIN));
		newProject.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		newProject.addClickListener(_ -> openProjectDialog(securityContext));

		var toolbar = new HorizontalLayout(newProject);
		toolbar.setWidthFull();
		toolbar.setJustifyContentMode(JustifyContentMode.END);

		setSizeFull();
		add(toolbar, grid);
		expand(grid);
	}

	private void openProjectDialog(SecurityContext securityContext) {
		var dialog = new Dialog("New project");

		var name = new TextField("Name");
		name.setRequiredIndicatorVisible(true);
		name.setWidthFull();
		var description = new TextArea("Description");
		description.setWidthFull();
		dialog.add(new VerticalLayout(name, description));

		var save = new Button("Save", _ -> {
			if (name.getValue().isBlank()) {
				name.setInvalid(true);
				name.setErrorMessage("A name is required");
				return;
			}
			securityContext.getLoggedInUser().ifPresent(owner -> {
				var project = new ProjectRecord();
				project.setName(name.getValue());
				project.setDescription(description.getValue().isBlank() ? null : description.getValue());
				project.setOwnerId(owner.getId());
				projectService.save(project);

				grid.setItems(projectService.findAll());
				Notifier.success("Project saved");
				dialog.close();
			});
		});
		save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		dialog.getFooter().add(new Button("Cancel", _ -> dialog.close()), save);

		dialog.open();
	}

}
