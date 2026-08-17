package ch.martinelli.tm.project.domain;

import ch.martinelli.tm.db.tables.records.ProjectRecord;
import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.ProjectOverview;
import ch.martinelli.tm.domain.TaskStatus;
import ch.martinelli.tm.domain.TaskSummary;
import org.jooq.DSLContext;
import org.jooq.Records;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static ch.martinelli.tm.db.tables.Project.PROJECT;
import static ch.martinelli.tm.db.tables.Task.TASK;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectCount;

@Repository
public class ProjectRepository {

	private final DSLContext dsl;

	public ProjectRepository(DSLContext dsl) {
		this.dsl = dsl;
	}

	public List<ProjectListItem> findAll() {
		return dsl
			.select(PROJECT.ID, PROJECT.NAME, PROJECT.owner().FULL_NAME,
					field(selectCount().from(TASK)
						.where(TASK.PROJECT_ID.eq(PROJECT.ID).and(TASK.STATUS.ne(TaskStatus.DONE)))))
			.from(PROJECT)
			.orderBy(PROJECT.NAME.asc())
			.fetch(Records.mapping(ProjectListItem::new));
	}

	public Optional<ProjectRecord> findById(long id) {
		return dsl.fetchOptional(PROJECT, PROJECT.ID.eq(id));
	}

	/**
	 * One query, one round trip: the projects of an owner with their open tasks nested
	 * via MULTISET.
	 */
	public List<ProjectOverview> findOverviewByOwner(long ownerId) {
		return dsl
			.select(PROJECT.NAME, PROJECT.owner().FULL_NAME,
					multiset(select(TASK.ID, TASK.TITLE, TASK.DUE_DATE).from(TASK)
						.where(TASK.PROJECT_ID.eq(PROJECT.ID))
						.orderBy(TASK.DUE_DATE.asc())).convertFrom(r -> r.map(Records.mapping(TaskSummary::new))))
			.from(PROJECT)
			.where(PROJECT.OWNER_ID.eq(ownerId))
			.fetch(Records.mapping(ProjectOverview::new));
	}

	public void save(ProjectRecord project) {
		dsl.attach(project);
		project.store();
	}

}
