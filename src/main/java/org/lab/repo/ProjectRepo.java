package org.lab.repo;

import org.lab.domain.Project;
import org.lab.domain.ProjectId;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ProjectRepo {
    private final ConcurrentHashMap<ProjectId, Project> projectsById = new ConcurrentHashMap<>();

    public Project save(Project project) {
        projectsById.put(project.id(), project);
        return project;
    }

    public Optional<Project> findById(ProjectId id) {
        return Optional.ofNullable(projectsById.get(id));
    }

    public Collection<Project> findAll() {
        return projectsById.values();
    }
}



