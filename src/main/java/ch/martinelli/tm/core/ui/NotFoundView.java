package ch.martinelli.tm.core.ui;

import ch.martinelli.tm.dashboard.ui.DashboardView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.HttpStatusCode;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
public class NotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

	public NotFoundView() {
		setSizeFull();
		setAlignItems(Alignment.CENTER);
		setJustifyContentMode(JustifyContentMode.CENTER);
	}

	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
		add(new H2("Page not found"), new Paragraph("The page " + event.getLocation().getPath() + " does not exist."),
				new RouterLink("Back to the dashboard", DashboardView.class));
		return HttpStatusCode.NOT_FOUND.getCode();
	}

}
