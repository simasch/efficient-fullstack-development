package ch.martinelli.tm.project.ui;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.spring.annotation.RouteScopeOwner;
import ch.martinelli.tm.core.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.Objects;

/**
 * The owner of the project route scope. Views nested inside this layout share the
 * {@link SelectedProject} bean. The access annotation matters: Vaadin denies a view whose
 * parent layout allows less than the view itself.
 */
@PermitAll
@ParentLayout(MainLayout.class)
public class ProjectLayout extends VerticalLayout implements RouterLayout, AfterNavigationObserver, HasDynamicTitle {

	private final H3 projectName = new H3();

	private final transient SelectedProject selectedProject;

	public ProjectLayout(@RouteScopeOwner(ProjectLayout.class) SelectedProject selectedProject) {
		this.selectedProject = selectedProject;

		setSizeFull();
		setPadding(false);
		add(projectName);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		projectName.setText(Objects.requireNonNullElse(selectedProject.getName(), ""));
	}

	@Override
	public String getPageTitle() {
		return Objects.requireNonNullElse(selectedProject.getName(), "Project");
	}

}
