package org.lab.service;

import org.junit.jupiter.api.Test;
import org.lab.domain.DomainException;
import org.lab.domain.TicketStatus;
import org.lab.repo.MilestoneRepo;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {
    @Test
    void ticketHappyPath() {
        var projectRepo = new ProjectRepo();
        var milestoneRepo = new MilestoneRepo();
        var ticketRepo = new TicketRepo();

        var projectService = new ProjectService(projectRepo);
        var milestoneService = new MilestoneService(projectRepo, milestoneRepo, ticketRepo);
        var ticketService = new TicketService(projectRepo, ticketRepo, milestoneService);

        var manager = java.util.UUID.randomUUID();
        var lead = java.util.UUID.randomUUID();
        var dev = java.util.UUID.randomUUID();

        var project = projectService.createProject(manager, "P");
        projectService.assignTeamLead(manager, project.id(), lead);
        projectService.addDeveloper(manager, project.id(), dev);

        var ms = milestoneService.createMilestone(manager, project.id(), "M", LocalDate.now(), LocalDate.now().plusDays(1));
        milestoneService.activateMilestone(manager, ms.id());

        var ticket = ticketService.createTicket(lead, project.id(), ms.id(), "T");
        var assigned = ticketService.assignDeveloper(lead, ticket.id(), dev);
        assertEquals(TicketStatus.ACCEPTED, assigned.status());

        var started = ticketService.startWork(dev, ticket.id());
        assertEquals(TicketStatus.IN_PROGRESS, started.status());

        var done = ticketService.complete(dev, ticket.id());
        assertEquals(TicketStatus.DONE, done.status());
    }

    @Test
    void onlyAssigneeCanStart() {
        var projectRepo = new ProjectRepo();
        var milestoneRepo = new MilestoneRepo();
        var ticketRepo = new TicketRepo();

        var projectService = new ProjectService(projectRepo);
        var milestoneService = new MilestoneService(projectRepo, milestoneRepo, ticketRepo);
        var ticketService = new TicketService(projectRepo, ticketRepo, milestoneService);

        var manager = java.util.UUID.randomUUID();
        var lead = java.util.UUID.randomUUID();
        var dev = java.util.UUID.randomUUID();
        var other = java.util.UUID.randomUUID();

        var project = projectService.createProject(manager, "P");
        projectService.assignTeamLead(manager, project.id(), lead);
        projectService.addDeveloper(manager, project.id(), dev);

        var ms = milestoneService.createMilestone(manager, project.id(), "M", LocalDate.now(), LocalDate.now().plusDays(1));
        milestoneService.activateMilestone(manager, ms.id());

        var ticket = ticketService.createTicket(lead, project.id(), ms.id(), "T");
        ticketService.assignDeveloper(lead, ticket.id(), dev);

        assertThrows(DomainException.class, () -> ticketService.startWork(other, ticket.id()));
    }
}




