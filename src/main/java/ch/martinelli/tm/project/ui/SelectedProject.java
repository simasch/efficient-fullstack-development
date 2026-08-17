package ch.martinelli.tm.project.ui;

import com.vaadin.flow.spring.annotation.RouteScope;
import com.vaadin.flow.spring.annotation.RouteScopeOwner;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jspecify.annotations.Nullable;

/**
 * State shared between all views under the {@link ProjectLayout}: the bean lives as long
 * as the user stays on routes owned by that layout.
 */
@SpringComponent
@RouteScope
@RouteScopeOwner(ProjectLayout.class)
public class SelectedProject {

	@Nullable private Long projectId;

	@Nullable private String name;

	public @Nullable Long getProjectId() {
		return projectId;
	}

	public void setProjectId(@Nullable Long projectId) {
		this.projectId = projectId;
	}

	public @Nullable String getName() {
		return name;
	}

	public void setName(@Nullable String name) {
		this.name = name;
	}

}
