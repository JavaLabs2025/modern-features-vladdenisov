package org.lab.domain;

import java.util.UUID;

public record BugReport(
        UUID id,
        UUID projectId,
        String title,
        BugStatus status,
        UUID reporterId,
        UUID fixerId
) {
    // Records
}




