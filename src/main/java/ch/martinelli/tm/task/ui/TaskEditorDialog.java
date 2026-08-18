package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.Task;
import ch.martinelli.tm.domain.User;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

import java.util.List;

/**
 * The dialog is only a frame around the reusable {@link TaskForm}. It fires a
 * {@link SaveEvent} with the validated task; the view decides what saving means and
 * whether the dialog closes.
 */
public class TaskEditorDialog extends Dialog {

	final TaskForm form;

	final Button save = new Button(getTranslation("action.save"));

	final Button cancel = new Button(getTranslation("action.cancel"));

	public TaskEditorDialog(List<User> users, List<ProjectListItem> projects) {
		setHeaderTitle(getTranslation("view.task.title"));

		form = new TaskForm(users, projects);
		add(form);

		save.addThemeVariants(ButtonVariant.PRIMARY);
		save.addClickListener(_ -> {
			try {
				fireEvent(new SaveEvent(this, form.getTask()));
			}
			catch (ValidationException e) {
				// the form marks the invalid fields itself
			}
		});
		cancel.addClickListener(_ -> close());
		getFooter().add(cancel, save);
	}

	public void edit(Task task) {
		form.setTask(task);
		open();
	}

	public static class SaveEvent extends ComponentEvent<TaskEditorDialog> {

		private final transient Task task;

		public SaveEvent(TaskEditorDialog source, Task task) {
			super(source, false);
			this.task = task;
		}

		public Task getTask() {
			return task;
		}

	}

	public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
		return addListener(SaveEvent.class, listener);
	}

}
