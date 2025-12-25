package org.lab.service;

import org.junit.jupiter.api.Test;
import org.lab.domain.DomainException;
import org.lab.domain.MilestoneStatus;
import org.lab.repo.MilestoneRepo;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class MilestoneServiceTest {
    @Test
    void cannotCloseMilestoneWithUnfinishedTickets() {
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
        ticketService.assignDeveloper(lead, ticket.id(), dev);

        assertThrows(DomainException.class, () -> milestoneService.closeMilestone(manager, ms.id()));
    }

    @Test
    void milestoneClosesWhenAllTicketsDone() {
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
        ticketService.assignDeveloper(lead, ticket.id(), dev);
        ticketService.startWork(dev, ticket.id());
        ticketService.complete(dev, ticket.id());

        var closed = milestoneService.closeMilestone(manager, ms.id());
        assertEquals(MilestoneStatus.CLOSED, closed.status());
    }
}




