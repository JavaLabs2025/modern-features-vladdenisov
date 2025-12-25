package org.lab.service;

import org.junit.jupiter.api.Test;
import org.lab.domain.BugStatus;
import org.lab.domain.DomainException;
import org.lab.repo.BugRepo;
import org.lab.repo.ProjectRepo;

import static org.junit.jupiter.api.Assertions.*;

class BugServiceTest {
    @Test
    void bugHappyPath() {
        var projectRepo = new ProjectRepo();
        var bugRepo = new BugRepo();

        var projectService = new ProjectService(projectRepo);
        var bugService = new BugService(projectRepo, bugRepo);

        var manager = java.util.UUID.randomUUID();
        var lead = java.util.UUID.randomUUID();
        var dev = java.util.UUID.randomUUID();
        var tester = java.util.UUID.randomUUID();

        var project = projectService.createProject(manager, "P");
        projectService.assignTeamLead(manager, project.id(), lead);
        projectService.addDeveloper(manager, project.id(), dev);
        projectService.addTester(manager, project.id(), tester);

        var bug = bugService.createBug(tester, project.id(), "B");
        assertEquals(BugStatus.NEW, bug.status());

        var assigned = bugService.assignFixer(lead, bug.id(), dev);
        assertEquals(dev, assigned.fixerId());

        var fixed = bugService.markFixed(dev, bug.id());
        assertEquals(BugStatus.FIXED, fixed.status());

        var tested = bugService.markTested(tester, bug.id());
        assertEquals(BugStatus.TESTED, tested.status());

        var closed = bugService.close(manager, bug.id());
        assertEquals(BugStatus.CLOSED, closed.status());
    }

    @Test
    void onlyTesterCanMarkTested() {
        var projectRepo = new ProjectRepo();
        var bugRepo = new BugRepo();

        var projectService = new ProjectService(projectRepo);
        var bugService = new BugService(projectRepo, bugRepo);

        var manager = java.util.UUID.randomUUID();
        var lead = java.util.UUID.randomUUID();
        var dev = java.util.UUID.randomUUID();
        var tester = java.util.UUID.randomUUID();

        var project = projectService.createProject(manager, "P");
        projectService.assignTeamLead(manager, project.id(), lead);
        projectService.addDeveloper(manager, project.id(), dev);
        projectService.addTester(manager, project.id(), tester);

        var bug = bugService.createBug(tester, project.id(), "B");
        bugService.assignFixer(lead, bug.id(), dev);
        bugService.markFixed(dev, bug.id());

        assertThrows(DomainException.class, () -> bugService.markTested(dev, bug.id()));
    }
}




