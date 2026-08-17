package ch.martinelli.tm.project.domain;

import ch.martinelli.tm.db.tables.records.ProjectRecord;
import ch.martinelli.tm.domain.ProjectListItem;
import ch.martinelli.tm.domain.ProjectOverview;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;

	public ProjectService(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	@Transactional(readOnly = true)
	public List<ProjectListItem> findAll() {
		return projectRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Optional<ProjectRecord> findById(long id) {
		return projectRepository.findById(id);
	}

	@Transactional(readOnly = true)
	public List<ProjectOverview> findOverviewByOwner(long ownerId) {
		return projectRepository.findOverviewByOwner(ownerId);
	}

	@Transactional
	public void save(ProjectRecord project) {
		projectRepository.save(project);
	}

}
