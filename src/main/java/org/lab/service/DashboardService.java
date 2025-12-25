package org.lab.service;

import org.lab.domain.DomainException;
import org.lab.domain.BugReport;
import org.lab.domain.Project;
import org.lab.domain.Ticket;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.Future;
import java.util.List;
import java.util.UUID;

public final class DashboardService {
    private final ProjectService projectService;
    private final TicketService ticketService;
    private final BugService bugService;

    public DashboardService(ProjectService projectService, TicketService ticketService, BugService bugService) {
        this.projectService = projectService;
        this.ticketService = ticketService;
        this.bugService = bugService;
    }

    public UserOverview overviewStructured(UUID userId) {
        // Structured Concurrency
        try (var scope = StructuredTaskScope.open()) {
            var projects = scope.fork(() -> projectService.projectsForUser(userId));
            var tickets = scope.fork(() -> ticketService.ticketsForAssignee(userId));
            var bugs = scope.fork(() -> bugService.bugsToFix(userId));
            scope.join();
            return new UserOverview(projects.get(), tickets.get(), bugs.get());
        } catch (InterruptedException _) {
            // Unnamed Variables and Patterns
            Thread.currentThread().interrupt();
            throw new DomainException("Interrupted");
        }
    }
}


