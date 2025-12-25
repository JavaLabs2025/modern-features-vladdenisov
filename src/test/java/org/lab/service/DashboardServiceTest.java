package org.lab.service;

import org.junit.jupiter.api.Test;
import org.lab.repo.BugRepo;
import org.lab.repo.MilestoneRepo;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {
    @Test
    void overviewContainsAssignedTickets() {
        var projectRepo = new ProjectRepo();
        var milestoneRepo = new MilestoneRepo();
        var ticketRepo = new TicketRepo();
        var bugRepo = new BugRepo();

        var projectService = new ProjectService(projectRepo);
        var milestoneService = new MilestoneService(projectRepo, milestoneRepo, ticketRepo);
        var ticketService = new TicketService(projectRepo, ticketRepo, milestoneService);
        var bugService = new BugService(projectRepo, bugRepo);
        var dashboard = new DashboardService(projectService, ticketService, bugService);

        var manager = java.util.UUID.randomUUID();
        var lead = java.util.UUID.randomUUID();
        var dev = java.util.UUID.randomUUID();
        var tester = java.util.UUID.randomUUID();

        var project = projectService.createProject(manager, "P");
        projectService.assignTeamLead(manager, project.id(), lead);
        projectService.addDeveloper(manager, project.id(), dev);
        projectService.addTester(manager, project.id(), tester);

        var ms = milestoneService.createMilestone(manager, project.id(), "M", LocalDate.now(), LocalDate.now().plusDays(1));
        milestoneService.activateMilestone(manager, ms.id());

        var ticket = ticketService.createTicket(lead, project.id(), ms.id(), "T");
        ticketService.assignDeveloper(lead, ticket.id(), dev);

        var overview = dashboard.overviewStructured(dev);
        assertEquals(1, overview.assignedTickets().size());
        assertEquals(ticket.id(), overview.assignedTickets().getFirst().id());
    }
}



