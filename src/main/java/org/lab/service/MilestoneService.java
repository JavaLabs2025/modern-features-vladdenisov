package org.lab.service;

import org.lab.domain.DomainException;
import org.lab.domain.Milestone;
import org.lab.domain.MilestoneStatus;
import org.lab.domain.ProjectId;
import org.lab.domain.ProjectRole;
import org.lab.repo.MilestoneRepo;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public final class MilestoneService {
    private final ProjectRepo projectRepo;
    private final MilestoneRepo milestoneRepo;
    private final TicketRepo ticketRepo;

    public MilestoneService(ProjectRepo projectRepo, MilestoneRepo milestoneRepo, TicketRepo ticketRepo) {
        this.projectRepo = projectRepo;
        this.milestoneRepo = milestoneRepo;
        this.ticketRepo = ticketRepo;
    }

    public Milestone createMilestone(UUID actorId, ProjectId projectId, String name, LocalDate start, LocalDate end) {
        var project = projectRepo.findById(projectId).orElseThrow(() -> new DomainException("Project not found"));
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can create milestone");
        }
        if (project.activeMilestoneId() != null) {
            throw new DomainException("Active milestone already exists");
        }
        var milestone = new Milestone(UUID.randomUUID(), projectId.value(), name, start, end, MilestoneStatus.OPEN, new ArrayList<>());
        
        return milestoneRepo.save(milestone);
    }

    public void activateMilestone(UUID actorId, UUID milestoneId) {
        var milestone = requireMilestone(milestoneId);
        
        var project = projectRepo.findById(new ProjectId(milestone.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can activate milestone");
        }
        if (project.activeMilestoneId() != null && !project.activeMilestoneId().equals(milestoneId)) {
            throw new DomainException("Another active milestone already exists");
        }
        if (milestone.status() == MilestoneStatus.CLOSED) {
            throw new DomainException("Milestone already closed");
        }
        projectRepo.save(new org.lab.domain.Project(
                project.id(),
                project.name(),
                project.managerId(),
                project.teamLeadId(),
                project.developerIds(),
                project.testerIds(),
                milestoneId
        ));
        milestoneRepo.save(new Milestone(
                milestone.id(),
                milestone.projectId(),
                milestone.name(),
                milestone.startDate(),
                milestone.endDate(),
                MilestoneStatus.ACTIVE,
                milestone.ticketIds()
        ));
    }

    public Milestone closeMilestone(UUID actorId, UUID milestoneId) {
        var milestone = requireMilestone(milestoneId);
        
        var project = projectRepo.findById(new ProjectId(milestone.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can close milestone");
        }

        var allDone = milestone.ticketIds().stream()
                .map(id -> ticketRepo.findById(id).orElseThrow(() -> new DomainException("Ticket not found")))
                .allMatch(t -> t.status() == org.lab.domain.TicketStatus.DONE);
        // Streams

        if (!allDone) {
            throw new DomainException("Cannot close milestone with unfinished tickets");
        }

        projectRepo.save(new org.lab.domain.Project(
                project.id(),
                project.name(),
                project.managerId(),
                project.teamLeadId(),
                project.developerIds(),
                project.testerIds(),
                null
        ));

        return milestoneRepo.save(new Milestone(
                milestone.id(),
                milestone.projectId(),
                milestone.name(),
                milestone.startDate(),
                milestone.endDate(),
                MilestoneStatus.CLOSED,
                milestone.ticketIds()
        ));
    }

    public void addTicket(UUID milestoneId, UUID ticketId) {
        var milestone = requireMilestone(milestoneId);
        
        var updated = new ArrayList<>(milestone.ticketIds());
        
        updated.add(ticketId);
        milestoneRepo.save(new Milestone(
                milestone.id(),
                milestone.projectId(),
                milestone.name(),
                milestone.startDate(),
                milestone.endDate(),
                milestone.status(),
                java.util.List.copyOf(updated)
        ));
    }

    public Milestone requireMilestone(UUID id) {
        return milestoneRepo.findById(id).orElseThrow(() -> new DomainException("Milestone not found"));
    }
}



