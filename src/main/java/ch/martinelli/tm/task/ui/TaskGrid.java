package ch.martinelli.tm.task.ui;

import ch.martinelli.tm.domain.TaskFilter;
import ch.martinelli.tm.domain.TaskListItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import org.jooq.Field;
import org.jooq.OrderField;

import java.util.List;

import static ch.martinelli.tm.db.tables.Task.TASK;

/**
 * The task grid: presentation only. The data comes from whoever owns the grid — the view
 * wires the lazy loading callbacks, the grid keeps the current filter so the callbacks
 * can ask for it.
 */
public class TaskGrid extends Grid<TaskListItem> {

	private TaskFilter filter = TaskFilter.empty();

	public TaskGrid() {
		addColumn(TaskListItem::title).setHeader("Title")
			.setSortProperty(TASK.TITLE.getName())
			.setFlexGrow(1)
			.setAutoWidth(true);
		addColumn(TaskListItem::dueDate).setHeader("Due date")
			.setSortProperty(TASK.DUE_DATE.getName())
			.setAutoWidth(true);
		addColumn(LitRenderer.<TaskListItem>of("<span theme=\"badge ${item.theme}\">${item.status}</span>")
			.withProperty("status", item -> item.status().name())
			.withProperty("theme", item -> switch (item.status()) {
				case OPEN -> "contrast";
				case IN_PROGRESS -> "";
				case BLOCKED -> "error";
				case DONE -> "success";
			})).setHeader("Status").setSortProperty(TASK.STATUS.getName()).setAutoWidth(true);
		addColumn(TaskListItem::assigneeName).setHeader("Assignee").setAutoWidth(true);
		addColumn(TaskListItem::projectName).setHeader("Project").setAutoWidth(true);
	}

	public void setFilter(TaskFilter filter) {
		this.filter = filter;
		refresh();
	}

	public TaskFilter getFilter() {
		return filter;
	}

	public void refresh() {
		getDataProvider().refreshAll();
	}

	/**
	 * Translates the grid's sort orders into jOOQ order fields. The lookup in the
	 * generated table is deliberate: a property name that came from the client must never
	 * reach the SQL as a string.
	 */
	public static List<OrderField<?>> toOrderFields(List<QuerySortOrder> sortOrders) {
		if (sortOrders.isEmpty()) {
			return List.of(TASK.DUE_DATE.asc().nullsLast(), TASK.ID.asc());
		}
		return sortOrders.stream().<OrderField<?>>map(order -> {
			Field<?> field = TASK.field(order.getSorted());
			if (field == null) {
				throw new IllegalArgumentException("Unknown sort property: " + order.getSorted());
			}
			return order.getDirection() == SortDirection.ASCENDING ? field.asc() : field.desc();
		}).toList();
	}

}
