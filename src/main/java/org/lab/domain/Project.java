package org.lab.domain;

import java.util.Set;
import java.util.UUID;

public record Project(
        ProjectId id,
        String name,
        UUID managerId,
        UUID teamLeadId,
        Set<UUID> developerIds,
        Set<UUID> testerIds,
        UUID activeMilestoneId
) {
    // Records

    public ProjectRole roleFor(UUID userId) {
        // Pattern Matching for switch
        return switch (userId) {
            case UUID id when id.equals(managerId) -> ProjectRole.MANAGER;
            case UUID id when teamLeadId != null && id.equals(teamLeadId) -> ProjectRole.TEAMLEAD;
            case UUID id when developerIds.contains(id) -> ProjectRole.DEVELOPER;
            case UUID id when testerIds.contains(id) -> ProjectRole.TESTER;
            default -> null;
        };
    }

    public boolean isParticipant(UUID userId) {
        return roleFor(userId) != null;
    }
}



