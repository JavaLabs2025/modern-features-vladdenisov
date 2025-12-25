package org.lab.service;

import org.lab.domain.DomainException;
import org.lab.domain.ProjectRole;
import org.lab.domain.Ticket;
import org.lab.domain.TicketStatus;
import org.lab.domain.ProjectId;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;

import java.util.List;
import java.util.UUID;

public final class TicketService {
    private final ProjectRepo projectRepo;
    private final TicketRepo ticketRepo;
    private final MilestoneService milestoneService;

    public TicketService(ProjectRepo projectRepo, TicketRepo ticketRepo, MilestoneService milestoneService) {
        this.projectRepo = projectRepo;
        this.ticketRepo = ticketRepo;
        this.milestoneService = milestoneService;
    }

    public Ticket createTicket(UUID actorId, ProjectId projectId, UUID milestoneId, String title) {
        var project = projectRepo.findById(projectId).orElseThrow(() -> new DomainException("Project not found"));
        
        var role = project.roleFor(actorId);
        
        if (role != ProjectRole.MANAGER && role != ProjectRole.TEAMLEAD) {
            throw new DomainException("Only manager/teamlead can create ticket");
        }
        var ticket = new Ticket(UUID.randomUUID(), projectId.value(), milestoneId, title, TicketStatus.NEW, null);
        
        ticketRepo.save(ticket);
        milestoneService.addTicket(milestoneId, ticket.id());
        return ticket;
    }

    public Ticket assignDeveloper(UUID actorId, UUID ticketId, UUID developerId) {
        var ticket = requireTicket(ticketId);
        
        var project = projectRepo.findById(new ProjectId(ticket.projectId())).orElseThrow(() -> new DomainException("Project not found"));
        
        var role = project.roleFor(actorId);
        
        if (role != ProjectRole.MANAGER && role != ProjectRole.TEAMLEAD) {
            throw new DomainException("Only manager/teamlead can assign developer");
        }
        if (!project.developerIds().contains(developerId)) {
            throw new DomainException("Assignee is not a developer in this project");
        }
        return ticketRepo.save(new Ticket(
                ticket.id(),
                ticket.projectId(),
                ticket.milestoneId(),
                ticket.title(),
                TicketStatus.ACCEPTED,
                developerId
        ));
    }

    public Ticket startWork(UUID actorId, UUID ticketId) {
        var ticket = requireTicket(ticketId);
        
        if (ticket.assigneeId() == null || !ticket.assigneeId().equals(actorId)) {
            throw new DomainException("Only assignee can start work");
        }
        // Pattern Matching for switch
        return switch (ticket.status()) {
            case NEW -> throw new DomainException("Ticket must be assigned before starting");
            case ACCEPTED -> ticketRepo.save(new Ticket(
                    ticket.id(),
                    ticket.projectId(),
                    ticket.milestoneId(),
                    ticket.title(),
                    TicketStatus.IN_PROGRESS,
                    ticket.assigneeId()
            ));
            case IN_PROGRESS, DONE -> throw new DomainException("Cannot start from status " + ticket.status());
        };
    }

    public Ticket complete(UUID actorId, UUID ticketId) {
        var ticket = requireTicket(ticketId);
        
        if (ticket.assigneeId() == null || !ticket.assigneeId().equals(actorId)) {
            throw new DomainException("Only assignee can complete ticket");
        }
        // Pattern Matching for switch
        return switch (ticket.status()) {
            case IN_PROGRESS -> ticketRepo.save(new Ticket(
                    ticket.id(),
                    ticket.projectId(),
                    ticket.milestoneId(),
                    ticket.title(),
                    TicketStatus.DONE,
                    ticket.assigneeId()
            ));
            case NEW, ACCEPTED, DONE -> throw new DomainException("Cannot complete from status " + ticket.status());
        };
    }

    public List<Ticket> ticketsForAssignee(UUID userId) {
        // Streams
        return ticketRepo.findAll().stream()
                .filter(t -> t.assigneeId() != null && t.assigneeId().equals(userId))
                .toList();
    }

    public Ticket requireTicket(UUID id) {
        return ticketRepo.findById(id).orElseThrow(() -> new DomainException("Ticket not found"));
    }
}



