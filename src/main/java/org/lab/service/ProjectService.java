package org.lab.service;

import org.lab.domain.CommandResult;
import org.lab.domain.DomainException;
import org.lab.domain.Project;
import org.lab.domain.ProjectId;
import org.lab.domain.ProjectRole;
import org.lab.repo.ProjectRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ProjectService {
    private final ProjectRepo projectRepo;

    public ProjectService(ProjectRepo projectRepo) {
        this.projectRepo = projectRepo;
    }

    public Project createProject(UUID managerId, String name) {
        var project = new Project(
                ProjectId.random(),
                name,
                managerId,
                null,
                new HashSet<>(),
                new HashSet<>(),
                null
        );
        
        return projectRepo.save(project);
    }

    public CommandResult<Project> assignTeamLead(UUID actorId, ProjectId projectId, UUID teamLeadId) {
        var project = requireProject(projectId);
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            return new CommandResult.Error<>("Only manager can assign teamlead");
        }
        return new CommandResult.Ok<>(projectRepo.save(new Project(
                project.id(),
                project.name(),
                project.managerId(),
                teamLeadId,
                project.developerIds(),
                project.testerIds(),
                project.activeMilestoneId()
        )));
    }

    public Project addDeveloper(UUID actorId, ProjectId projectId, UUID developerId) {
        var project = requireProject(projectId);
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can add developer");
        }
        var updatedDevs = new HashSet<>(project.developerIds());
        
        updatedDevs.add(developerId);
        return projectRepo.save(new Project(
                project.id(),
                project.name(),
                project.managerId(),
                project.teamLeadId(),
                Set.copyOf(updatedDevs),
                project.testerIds(),
                project.activeMilestoneId()
        ));
    }

    public Project addTester(UUID actorId, ProjectId projectId, UUID testerId) {
        var project = requireProject(projectId);
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can add tester");
        }
        var updatedTesters = new HashSet<>(project.testerIds());
        
        updatedTesters.add(testerId);
        return projectRepo.save(new Project(
                project.id(),
                project.name(),
                project.managerId(),
                project.teamLeadId(),
                project.developerIds(),
                Set.copyOf(updatedTesters),
                project.activeMilestoneId()
        ));
    }

    public List<Project> projectsForUser(UUID userId) {
        // Streams
        return projectRepo.findAll().stream()
                .filter(p -> p.isParticipant(userId) || p.managerId().equals(userId))
                .toList();
    }

    public Project requireProject(ProjectId projectId) {
        return projectRepo.findById(projectId)
                .orElseThrow(() -> new DomainException("Project not found"));
    }
}



