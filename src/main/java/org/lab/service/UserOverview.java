package org.lab.service;

import org.lab.domain.BugReport;
import org.lab.domain.Project;
import org.lab.domain.Ticket;

import java.util.List;

public record UserOverview(
        List<Project> projects,
        List<Ticket> assignedTickets,
        List<BugReport> bugsToFix
) {
    // Records
}




