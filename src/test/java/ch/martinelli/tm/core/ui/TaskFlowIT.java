package ch.martinelli.tm.core.ui;

import org.junit.jupiter.api.Test;
import org.vaadin.addons.dramafinder.element.ButtonElement;
import org.vaadin.addons.dramafinder.element.ComboBoxElement;
import org.vaadin.addons.dramafinder.element.DialogElement;
import org.vaadin.addons.dramafinder.element.GridElement;
import org.vaadin.addons.dramafinder.element.NotificationElement;
import org.vaadin.addons.dramafinder.element.TextFieldElement;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * The happy flow through the task list: filter, open a task, change it, save it. Every
 * wait in this test is an assertion — there is no sleep.
 */
class TaskFlowIT extends PlaywrightIT {

	@Test
	void a_user_can_filter_and_edit_a_task() {
		login("alice");

		page.navigate("tasks");

		var grid = GridElement.get(page);

		// The filter bar reacts lazily, so the assertions — not a sleep — wait for the
		// reload
		TextFieldElement.getByLabel(page, "Search tasks").setValue("Offline");

		grid.assertRowCount(1);
		grid.assertCellContent(0, "Title", "Offline mode");
		assertThat(visibleGridCells("Set up CMS")).hasCount(0);

		grid.select(0);

		var dialog = DialogElement.getByHeaderText(page, "Task");
		dialog.assertOpen();

		// Scoped to the dialog on purpose: the filter bar has a status combo box too, and
		// a page-wide lookup would find that one first
		var form = dialog.getContentLocator();
		TextFieldElement.getByLabel(form, "Title").setValue("Offline mode v2");
		ComboBoxElement.getByLabel(form, "Status").selectItem("IN_PROGRESS");

		ButtonElement.getByText(page, "Save").click();

		NotificationElement.getByText(page, "Task saved").assertOpen();
		dialog.assertClosed();

		// The grid reloads its data in a second round trip; this retrying assertion waits
		// for it
		assertThat(visibleGridCells("Offline mode v2")).isVisible();
	}

}
