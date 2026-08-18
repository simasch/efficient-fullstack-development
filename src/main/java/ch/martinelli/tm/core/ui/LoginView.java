package ch.martinelli.tm.core.ui;

import ch.martinelli.tm.core.security.SecurityContext;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@Route(value = "login", autoLayout = false)
public class LoginView extends LoginOverlay implements BeforeEnterObserver, HasDynamicTitle {

	private final transient SecurityContext securityContext;

	public LoginView(SecurityContext securityContext) {
		this.securityContext = securityContext;
		setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

		var i18n = LoginI18n.createDefault();
		i18n.setHeader(new LoginI18n.Header());
		i18n.getHeader().setTitle(getTranslation("app.name"));
		i18n.getHeader().setDescription(getTranslation("login.hint"));
		i18n.getForm().setTitle(getTranslation("login.form.title"));
		i18n.getForm().setUsername(getTranslation("user.field.username"));
		i18n.getForm().setPassword(getTranslation("user.field.password"));
		i18n.getForm().setSubmit(getTranslation("action.sign.in"));
		i18n.getErrorMessage().setTitle(getTranslation("login.error.title"));
		i18n.getErrorMessage().setMessage(getTranslation("login.error.message"));
		i18n.setAdditionalInformation(null);
		setI18n(i18n);

		setForgotPasswordButtonVisible(false);
		setOpened(true);
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.login.title");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		if (securityContext.getLoggedInUser().isPresent()) {
			// Already logged in
			setOpened(false);
			event.forwardTo("");
		}

		setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
	}

}
