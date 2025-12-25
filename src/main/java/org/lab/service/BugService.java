package org.lab.service;

import org.lab.domain.BugReport;
import org.lab.domain.BugStatus;
import org.lab.domain.DomainException;
import org.lab.domain.ProjectId;
import org.lab.domain.ProjectRole;
import org.lab.repo.BugRepo;
import org.lab.repo.ProjectRepo;

import java.util.List;
import java.util.UUID;

public final class BugService {
    private final ProjectRepo projectRepo;
    private final BugRepo bugRepo;

    public BugService(ProjectRepo projectRepo, BugRepo bugRepo) {
        this.projectRepo = projectRepo;
        this.bugRepo = bugRepo;
    }

    public BugReport createBug(UUID actorId, ProjectId projectId, String title) {
        var project = projectRepo.findById(projectId).orElseThrow(() -> new DomainException("Project not found"));
        
        var role = project.roleFor(actorId);
        
        if (role != ProjectRole.DEVELOPER && role != ProjectRole.TESTER) {
            throw new DomainException("Only developer/tester can create bug report");
        }
        var bug = new BugReport(UUID.randomUUID(), projectId.value(), title, BugStatus.NEW, actorId, null);
        
        return bugRepo.save(bug);
    }

    public BugReport assignFixer(UUID actorId, UUID bugId, UUID developerId) {
        var bug = requireBug(bugId);
        
        var project = projectRepo.findById(new ProjectId(bug.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        var role = project.roleFor(actorId);
        
        if (role != ProjectRole.MANAGER && role != ProjectRole.TEAMLEAD) {
            throw new DomainException("Only manager/teamlead can assign fixer");
        }
        if (!project.developerIds().contains(developerId)) {
            throw new DomainException("Fixer must be a developer in this project");
        }
        if (bug.status() != BugStatus.NEW) {
            throw new DomainException("Bug is not NEW");
        }
        return bugRepo.save(new BugReport(
                bug.id(),
                bug.projectId(),
                bug.title(),
                bug.status(),
                bug.reporterId(),
                developerId
        ));
    }

    public BugReport markFixed(UUID actorId, UUID bugId) {
        var bug = requireBug(bugId);
        
        if (bug.fixerId() == null || !bug.fixerId().equals(actorId)) {
            throw new DomainException("Only fixer can mark bug fixed");
        }
        // Pattern Matching for switch
        return switch (bug.status()) {
            case NEW -> bugRepo.save(new BugReport(
                    bug.id(),
                    bug.projectId(),
                    bug.title(),
                    BugStatus.FIXED,
                    bug.reporterId(),
                    bug.fixerId()
            ));
            case FIXED, TESTED, CLOSED -> throw new DomainException("Cannot fix from status " + bug.status());
        };
    }

    public BugReport markTested(UUID actorId, UUID bugId) {
        var bug = requireBug(bugId);
        
        var project = projectRepo.findById(new ProjectId(bug.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        if (project.roleFor(actorId) != ProjectRole.TESTER) {
            throw new DomainException("Only tester can mark bug tested");
        }
        if (bug.status() != BugStatus.FIXED) {
            throw new DomainException("Bug must be FIXED to be TESTED");
        }
        return bugRepo.save(new BugReport(
                bug.id(),
                bug.projectId(),
                bug.title(),
                BugStatus.TESTED,
                bug.reporterId(),
                bug.fixerId()
        ));
    }

    public BugReport close(UUID actorId, UUID bugId) {
        var bug = requireBug(bugId);
        
        var project = projectRepo.findById(new ProjectId(bug.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        if (project.roleFor(actorId) != ProjectRole.MANAGER) {
            throw new DomainException("Only manager can close bug");
        }
        if (bug.status() != BugStatus.TESTED) {
            throw new DomainException("Bug must be TESTED to be CLOSED");
        }
        return bugRepo.save(new BugReport(
                bug.id(),
                bug.projectId(),
                bug.title(),
                BugStatus.CLOSED,
                bug.reporterId(),
                bug.fixerId()
        ));
    }

    public List<BugReport> bugsToFix(UUID userId) {
        // Streams
        return bugRepo.findAll().stream()
                .filter(b -> b.fixerId() != null && b.fixerId().equals(userId))
                .filter(b -> b.status() != BugStatus.CLOSED)
                .toList();
    }

    public BugReport requireBug(UUID id) {
        return bugRepo.findById(id).orElseThrow(() -> new DomainException("Bug not found"));
    }
}


