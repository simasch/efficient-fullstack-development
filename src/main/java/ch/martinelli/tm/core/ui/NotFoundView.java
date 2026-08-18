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
		add(new H2(getTranslation("error.not.found.title")),
				new Paragraph(getTranslation("error.not.found.message", event.getLocation().getPath())),
				new RouterLink(getTranslation("error.not.found.link"), DashboardView.class));
		return HttpStatusCode.NOT_FOUND.getCode();
	}

}
