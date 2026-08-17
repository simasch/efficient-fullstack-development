package ch.martinelli.tm.core.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceInitListener.class);

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.getSource()
			.addSessionInitListener(
					sessionInitEvent -> sessionInitEvent.getSession().setErrorHandler(this::handleError));
	}

	@SuppressWarnings("FutureReturnValueIgnored")
	private void handleError(ErrorEvent errorEvent) {
		LOGGER.error("Unhandled exception in the UI", errorEvent.getThrowable());

		UI ui = UI.getCurrent();
		if (ui != null) {
			ui.access(() -> {
				Notification notification = Notification.show(
						"Something went wrong. Please try again or contact support.", 5000,
						Notification.Position.MIDDLE);
				notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
			});
		}
	}

}
