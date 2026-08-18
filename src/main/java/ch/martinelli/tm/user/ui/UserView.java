package ch.martinelli.tm.user.ui;

import ch.martinelli.tm.core.domain.UserService;
import ch.martinelli.tm.core.domain.UserWithRoles;
import ch.martinelli.tm.core.domain.UsernameAlreadyTakenException;
import ch.martinelli.tm.core.ui.components.Notifier;
import ch.martinelli.tm.core.ui.i18n.BusinessRuleMessage;
import ch.martinelli.tm.domain.EmailAddress;
import ch.martinelli.tm.domain.Role;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.jspecify.annotations.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.lineawesome.LineAwesomeIcon;

import java.util.Objects;
import java.util.Set;

@RolesAllowed(Role.ADMIN)
@Route(value = "users")
public class UserView extends Div implements HasUrlParameter<String>, HasDynamicTitle {

	private final transient UserService userService;

	private final transient PasswordEncoder passwordEncoder;

	private final Grid<UserWithRoles> grid = new Grid<>();

	private final Button cancel = new Button(getTranslation("action.cancel"));

	private final Button save = new Button(getTranslation("action.save"));

	private final Binder<UserWithRoles> binder = new Binder<>();

	private final TextField usernameField = new TextField(getTranslation("user.field.username"));

	@Nullable private transient UserWithRoles user;

	public UserView(UserService userService, PasswordEncoder passwordEncoder) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;

		setSizeFull();

		var splitLayout = new SplitLayout();
		splitLayout.setSizeFull();
		splitLayout.setSplitterPosition(75);
		add(splitLayout);

		splitLayout.addToPrimary(createGrid());
		splitLayout.addToSecondary(createForm());
	}

	@Override
	public String getPageTitle() {
		return getTranslation("view.users.title");
	}

	@Override
	public void setParameter(BeforeEvent beforeEvent, @Nullable @OptionalParameter String username) {
		if (username != null) {
			userService.findWithRolesByUsername(username).ifPresent(userWithRoles -> user = userWithRoles);
		}
		else {
			user = null;
		}
		binder.readBean(user);
		grid.select(user);

		if (user != null && user.getUser().getUsername() != null) {
			usernameField.setReadOnly(true);
		}
	}

	private VerticalLayout createGrid() {
		grid.setSizeFull();
		grid.addThemeVariants(GridVariant.NO_BORDER);

		grid.addColumn(u -> u.getUser().getUsername())
			.setHeader(getTranslation("user.field.username"))
			.setAutoWidth(true);
		grid.addColumn(u -> u.getUser().getFullName())
			.setHeader(getTranslation("user.field.full.name"))
			.setAutoWidth(true);
		grid.addColumn(u -> u.getUser().getEmail() == null ? "" : u.getUser().getEmail().value())
			.setHeader(getTranslation("user.field.email"))
			.setAutoWidth(true);
		grid.addColumn(u -> String.join(", ", u.getRoles()))
			.setHeader(getTranslation("user.field.roles"))
			.setAutoWidth(true);
		grid.addComponentColumn(u -> {
			var active = new Checkbox(Boolean.TRUE.equals(u.getUser().getActive()));
			active.setReadOnly(true);
			return active;
		}).setHeader(getTranslation("user.field.active")).setAutoWidth(true).setKey("active");

		var addIcon = LineAwesomeIcon.PLUS_SOLID.create();
		addIcon.addClickListener(_ -> clearForm());
		grid.addComponentColumn(_ -> new Div()).setHeader(addIcon).setKey("actions").setAutoWidth(true);

		grid.setItems(query -> userService.findAllWithRoles(query.getOffset(), query.getLimit()).stream());

		grid.asSingleSelect().addValueChangeListener(event -> {
			if (event.getValue() != null) {
				UI.getCurrent().navigate(UserView.class, event.getValue().getUser().getUsername());
			}
			else {
				clearForm();
				UI.getCurrent().navigate(UserView.class);
			}
		});

		var gridLayout = new VerticalLayout(grid);
		gridLayout.setSizeFull();

		return gridLayout;
	}

	private void clearForm() {
		usernameField.setReadOnly(false);
		user = new UserWithRoles();
		binder.readBean(user);
	}

	private VerticalLayout createForm() {
		var formLayout = new FormLayout();

		binder.forField(usernameField)
			.asRequired()
			.bind(u -> u.getUser().getUsername(), (u, s) -> u.getUser().setUsername(s));

		var fullNameField = new TextField(getTranslation("user.field.full.name"));
		binder.forField(fullNameField)
			.asRequired()
			.bind(u -> u.getUser().getFullName(), (u, s) -> u.getUser().setFullName(s));

		var emailField = new TextField(getTranslation("user.field.email"));
		binder.forField(emailField)
			.asRequired()
			.withConverter(EmailAddress::new, email -> email == null ? "" : email.value(),
					getTranslation("validation.email.invalid"))
			.bind(u -> u.getUser().getEmail(), (u, e) -> u.getUser().setEmail(e));

		var passwordField = new PasswordField(getTranslation("user.field.password"));
		binder.forField(passwordField).asRequired().bind(_ -> "", (u, s) -> {
			String encoded = passwordEncoder.encode(s);
			if (encoded != null) {
				u.getUser().setPasswordHash(encoded);
			}
		});

		var roleMultiSelect = new MultiSelectComboBox<String>(getTranslation("user.field.roles"));
		roleMultiSelect.setItems(Set.of(Role.ADMIN, Role.USER));
		binder.forField(roleMultiSelect).bind(UserWithRoles::getRoles, UserWithRoles::setRoles);

		var activeCheckbox = new Checkbox(getTranslation("user.field.active"));
		binder.forField(activeCheckbox)
			.bind(u -> Boolean.TRUE.equals(u.getUser().getActive()), (u, active) -> u.getUser().setActive(active));

		formLayout.add(usernameField, fullNameField, emailField, passwordField, roleMultiSelect, activeCheckbox);

		var buttons = createButtonLayout();

		var verticalLayout = new VerticalLayout(formLayout, buttons);
		verticalLayout.setSizeFull();
		return verticalLayout;
	}

	private HorizontalLayout createButtonLayout() {
		var buttonLayout = new HorizontalLayout();

		cancel.addClickListener(_ -> {
			clearForm();
			refreshGrid();
		});

		save.addClickListener(_ -> {
			var validationStatus = binder.validate();
			if (user != null && validationStatus.isOk()) {
				try {
					binder.writeChangedBindingsToBean(user);

					try {
						userService.save(user);
						Notifier.success(getTranslation("notification.user.saved"));

						clearForm();
						refreshGrid();

						UI.getCurrent().navigate(UserView.class);
					}
					catch (UsernameAlreadyTakenException e) {
						Notifier.error(BusinessRuleMessage.translate(e));
					}
				}
				catch (ValidationException ex) {
					Notifier.error(getTranslation("notification.validation.errors"));
				}
			}
		});

		cancel.addThemeVariants(ButtonVariant.TERTIARY);
		save.addThemeVariants(ButtonVariant.PRIMARY);

		buttonLayout.add(save, cancel);

		return buttonLayout;
	}

	private void refreshGrid() {
		grid.select(null);
		grid.getDataProvider().refreshAll();
	}

}
