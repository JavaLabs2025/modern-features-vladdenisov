package org.lab.domain;

import java.util.UUID;

public record Ticket(
        UUID id,
        UUID projectId,
        UUID milestoneId,
        String title,
        TicketStatus status,
        UUID assigneeId
) {
    // Records
}




