package org.lab.service;

import org.junit.jupiter.api.Test;
import org.lab.domain.CommandResult;
import org.lab.domain.ProjectRole;
import org.lab.repo.ProjectRepo;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {
    @Test
    void managerCanAssignTeamlead() {
        var projectRepo = new ProjectRepo();
        var projectService = new ProjectService(projectRepo);

        var managerId = java.util.UUID.randomUUID();
        var teamleadId = java.util.UUID.randomUUID();

        var project = projectService.createProject(managerId, "P1");
        var res = projectService.assignTeamLead(managerId, project.id(), teamleadId);

        var msg = switch (res) {
            case CommandResult.Ok(var updated) -> updated.teamLeadId().toString();
            case CommandResult.Error(var m) -> m;
        };
        assertEquals(teamleadId.toString(), msg);

        assertEquals(ProjectRole.MANAGER, projectService.requireProject(project.id()).roleFor(managerId));
        assertEquals(ProjectRole.TEAMLEAD, projectService.requireProject(project.id()).roleFor(teamleadId));
    }

    @Test
    void nonManagerCannotAssignTeamlead() {
        var projectRepo = new ProjectRepo();
        var projectService = new ProjectService(projectRepo);

        var managerId = java.util.UUID.randomUUID();
        var actorId = java.util.UUID.randomUUID();
        var teamleadId = java.util.UUID.randomUUID();

        var project = projectService.createProject(managerId, "P1");
        var res = projectService.assignTeamLead(actorId, project.id(), teamleadId);

        assertTrue(res instanceof CommandResult.Error<?>);
    }
}




