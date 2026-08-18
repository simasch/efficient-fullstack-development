package ch.martinelli.tm.core.ui.layout;

import ch.martinelli.tm.core.security.SecurityContext;
import ch.martinelli.tm.dashboard.ui.DashboardView;
import ch.martinelli.tm.project.ui.ProjectListView;
import ch.martinelli.tm.task.ui.TaskListView;
import ch.martinelli.tm.user.ui.UserView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.server.menu.MenuConfiguration;

import java.util.Locale;

@AnonymousAllowed
@Layout
public class MainLayout extends AppLayout implements AfterNavigationObserver {

	private final transient SecurityContext securityContext;

	private final AccessAnnotationChecker accessAnnotationChecker;

	private final H2 viewTitle = new H2();

	public MainLayout(SecurityContext securityContext, AccessAnnotationChecker accessAnnotationChecker) {
		this.securityContext = securityContext;
		this.accessAnnotationChecker = accessAnnotationChecker;

		setPrimarySection(Section.DRAWER);
		addDrawerContent();
		addHeaderContent();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		viewTitle.setText(getCurrentPageTitle());
	}

	private void addHeaderContent() {
		var toggle = new DrawerToggle();
		toggle.setAriaLabel(getTranslation("layout.menu.toggle"));

		viewTitle.addClassName("view-title");

		addToNavbar(true, toggle, viewTitle);
	}

	private void addDrawerContent() {
		var appName = new Div(getTranslation("app.name"));
		appName.addClassName("app-name");

		var header = new Header(appName);

		var scroller = new Scroller(createNavigation());

		addToDrawer(header, scroller, createFooter());
	}

	private SideNav createNavigation() {
		var nav = new SideNav();

		if (accessAnnotationChecker.hasAccess(DashboardView.class)) {
			nav.addItem(new SideNavItem(getTranslation("view.dashboard.title"), DashboardView.class,
					VaadinIcon.DASHBOARD.create()));
		}
		if (accessAnnotationChecker.hasAccess(TaskListView.class)) {
			nav.addItem(
					new SideNavItem(getTranslation("view.tasks.title"), TaskListView.class, VaadinIcon.TASKS.create()));
		}
		if (accessAnnotationChecker.hasAccess(ProjectListView.class)) {
			nav.addItem(new SideNavItem(getTranslation("view.projects.title"), ProjectListView.class,
					VaadinIcon.FOLDER.create()));
		}
		if (accessAnnotationChecker.hasAccess(UserView.class)) {
			nav.addItem(new SideNavItem(getTranslation("view.users.title"), UserView.class, VaadinIcon.USER.create()));
		}

		return nav;
	}

	private Footer createFooter() {
		var footer = new Footer();
		var verticalLayout = new VerticalLayout();
		footer.add(verticalLayout);

		var locale = UI.getCurrent().getSession().getLocale();

		var languageSwitchEn = new Button("EN");
		languageSwitchEn.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
		languageSwitchEn.setEnabled(!Locale.ENGLISH.getLanguage().equals(locale.getLanguage()));
		languageSwitchEn.addClickListener(_ -> switchLanguage(Locale.ENGLISH));

		var languageSwitchDe = new Button("DE");
		languageSwitchDe.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
		languageSwitchDe.setEnabled(!Locale.GERMAN.getLanguage().equals(locale.getLanguage()));
		languageSwitchDe.addClickListener(_ -> switchLanguage(Locale.GERMAN));

		var languageLayout = new HorizontalLayout(languageSwitchEn, languageSwitchDe);
		languageLayout.addClassName("language-switch");
		verticalLayout.add(languageLayout);

		var optionalUserRecord = securityContext.getLoggedInUser();
		if (optionalUserRecord.isPresent()) {
			var user = optionalUserRecord.get();

			var avatar = new Avatar(user.getFullName());
			avatar.setThemeName("xsmall");
			avatar.getElement().setAttribute("tabindex", "-1");

			var userMenu = new MenuBar();
			userMenu.addThemeVariants(MenuBarVariant.TERTIARY);

			var userName = userMenu.addItem("");

			var div = new Div();
			div.add(avatar);
			div.add(user.getFullName());
			div.addClassName("user-menu-item");
			userName.add(div);
			userName.getSubMenu().addItem(getTranslation("action.sign.out"), _ -> securityContext.logout());

			verticalLayout.add(userMenu);
		}
		else {
			var loginLink = new Anchor("login", getTranslation("action.sign.in"));
			verticalLayout.add(loginLink);
		}

		return footer;
	}

	/**
	 * Switches to one of the locales the {@code I18NProvider} provides. Setting it on the
	 * session propagates it to every UI of the session and makes it survive the reload;
	 * the reload itself is what re-creates the views with the new language, because the
	 * views read their texts once while they are built.
	 */
	private void switchLanguage(Locale locale) {
		UI.getCurrent().getSession().setLocale(locale);
		UI.getCurrent().getPage().reload();
	}

	private String getCurrentPageTitle() {
		if (getContent() instanceof HasDynamicTitle hasDynamicTitle) {
			return hasDynamicTitle.getPageTitle() == null ? "" : hasDynamicTitle.getPageTitle();
		}
		else if (getContent().getClass().getAnnotation(PageTitle.class) != null) {
			return getContent().getClass().getAnnotation(PageTitle.class).value();
		}
		else {
			return MenuConfiguration.getPageHeader(getContent()).orElse("");
		}
	}

}
