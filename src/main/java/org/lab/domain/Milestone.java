package org.lab.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Milestone(
        UUID id,
        UUID projectId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        MilestoneStatus status,
        List<UUID> ticketIds
) {
    // Records
}




