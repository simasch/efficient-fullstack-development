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
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route("projects")
public class ProjectListView extends VerticalLayout implements HasDynamicTitle {

	private final transient ProjectService projectService;

	final Grid<ProjectListItem> grid = new Grid<>();

	final Button newProject = new Button(getTranslation("action.project.new"));

	public ProjectListView(ProjectService projectService, SecurityContext securityContext) {
		this.projectService = projectService;

		grid.addColumn(ProjectListItem::name)
			.setHeader(getTranslation("project.field.name"))
			.setFlexGrow(1)
			.setAutoWidth(true);
		grid.addColumn(ProjectListItem::ownerName).setHeader(getTranslation("project.field.owner")).setAutoWidth(true);
		grid.addColumn(ProjectListItem::openTasks)
			.setHeader(getTranslation("project.field.open.tasks"))
			.setAutoWidth(true);
		grid.setItems(projectService.findAll());
		grid.addItemClickListener(event -> UI.getCurrent()
			.navigate(ProjectTasksView.class, new RouteParameters("projectId", String.valueOf(event.getItem().id()))));

		// Route-level access control is declared with annotations; in-view decisions —
		// like who may create a project — are made programmatically.
		newProject.setVisible(securityContext.hasRole(Role.ADMIN));
		newProject.addThemeVariants(ButtonVariant.PRIMARY);
		newProject.addClickListener(_ -> openProjectDialog(securityContext));

		var toolbar = new HorizontalLayout(newProject);
		toolbar.setWidthFull();
		toolbar.setJustifyContentMode(JustifyContentMode.END);

		setSizeFull();
		add(toolbar, grid);
		expand(grid);
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.projects.title");
	}

	private void openProjectDialog(SecurityContext securityContext) {
		var dialog = new Dialog(getTranslation("action.project.new"));

		var name = new TextField(getTranslation("project.field.name"));
		name.setRequiredIndicatorVisible(true);
		name.setWidthFull();
		var description = new TextArea(getTranslation("project.field.description"));
		description.setWidthFull();
		dialog.add(new VerticalLayout(name, description));

		var save = new Button(getTranslation("action.save"), _ -> {
			if (name.getValue().isBlank()) {
				name.setInvalid(true);
				name.setErrorMessage(getTranslation("validation.project.name.required"));
				return;
			}
			securityContext.getLoggedInUser().ifPresent(owner -> {
				var project = new ProjectRecord();
				project.setName(name.getValue());
				project.setDescription(description.getValue().isBlank() ? null : description.getValue());
				project.setOwnerId(owner.getId());
				projectService.save(project);

				grid.setItems(projectService.findAll());
				Notifier.success(getTranslation("notification.project.saved"));
				dialog.close();
			});
		});
		save.addThemeVariants(ButtonVariant.PRIMARY);
		dialog.getFooter().add(new Button(getTranslation("action.cancel"), _ -> dialog.close()), save);

		dialog.open();
	}

}
