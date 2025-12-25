package org.lab;

import org.lab.domain.CommandResult;
import org.lab.domain.DomainException;
import org.lab.domain.Ticket;
import org.lab.repo.BugRepo;
import org.lab.repo.MilestoneRepo;
import org.lab.repo.ProjectRepo;
import org.lab.repo.TicketRepo;
import org.lab.repo.UserRepo;
import org.lab.service.AuthService;
import org.lab.service.BugService;
import org.lab.service.Context;
import org.lab.service.DashboardService;
import org.lab.service.MilestoneService;
import org.lab.service.ProjectService;
import org.lab.service.TicketService;

import java.time.LocalDate;
import java.util.stream.Gatherers;

public class Main {
    public static void main(String[] args) {
        var userRepo = new UserRepo();
        var projectRepo = new ProjectRepo();
        var milestoneRepo = new MilestoneRepo();
        var ticketRepo = new TicketRepo();
        var bugRepo = new BugRepo();
        

        var authService = new AuthService(userRepo);
        var projectService = new ProjectService(projectRepo);
        var milestoneService = new MilestoneService(projectRepo, milestoneRepo, ticketRepo);
        var ticketService = new TicketService(projectRepo, ticketRepo, milestoneService);
        var bugService = new BugService(projectRepo, bugRepo);
        var dashboardService = new DashboardService(projectService, ticketService, bugService);
        

        var manager = authService.register("manager");
        var teamlead = authService.register("teamlead");
        var dev = authService.register("dev");
        var tester = authService.register("tester");
        

        var project = projectService.createProject(manager.id(), "Modern Java Project");
        var assignLead = projectService.assignTeamLead(manager.id(), project.id(), teamlead.id());
        

        // Pattern matching for switch
        var assignLeadMsg = switch (assignLead) {
            case CommandResult.Ok(var updatedProject) -> "teamlead=" + updatedProject.teamLeadId();
            case CommandResult.Error(var message) -> "error=" + message;
        };
        IO.println(assignLeadMsg);
        // IO

        projectService.addDeveloper(manager.id(), project.id(), dev.id());
        projectService.addTester(manager.id(), project.id(), tester.id());

        var milestone = milestoneService.createMilestone(
                manager.id(),
                project.id(),
                "M1",
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );
        milestoneService.activateMilestone(manager.id(), milestone.id());

        var ticket = ticketService.createTicket(teamlead.id(), project.id(), milestone.id(), "Implement ticket flow");
        ticketService.assignDeveloper(teamlead.id(), ticket.id(), dev.id());
        ticketService.startWork(dev.id(), ticket.id());
        ticketService.complete(dev.id(), ticket.id());

        var bug = bugService.createBug(tester.id(), project.id(), "NullPointerException in report");
        bugService.assignFixer(teamlead.id(), bug.id(), dev.id());
        bugService.markFixed(dev.id(), bug.id());
        bugService.markTested(tester.id(), bug.id());
        bugService.close(manager.id(), bug.id());

        try {
            // Scoped Values
            ScopedValue.where(Context.CURRENT_USER_ID, dev.id()).run(() -> {
                var overview = dashboardService.overviewStructured(Context.CURRENT_USER_ID.get());
                

                // Stream Gatherers
                var windows = overview.assignedTickets().stream()
                        .map(Ticket::title)
                        .gather(Gatherers.windowFixed(2))
                        .toList();

                // Pattern Matching for instanceof
                if (windows instanceof java.util.List<?> list) {
                    var report = """
                            user=%s
                            projects=%d
                            assignedTickets=%d
                            bugsToFix=%d
                            ticketTitleWindows=%s
                            """.formatted(
                            dev.name(),
                            overview.projects().size(),
                            overview.assignedTickets().size(),
                            overview.bugsToFix().size(),
                            list
                    );
                    // Text Blocks
                    IO.println(report);
                }
            });
        } catch (DomainException e) {
            IO.println("Domain error: " + e.getMessage());
        }
    }
}

