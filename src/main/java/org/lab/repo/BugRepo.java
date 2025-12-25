package org.lab.repo;

import org.lab.domain.BugReport;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BugRepo {
    private final ConcurrentHashMap<UUID, BugReport> bugsById = new ConcurrentHashMap<>();

    public BugReport save(BugReport bug) {
        bugsById.put(bug.id(), bug);
        return bug;
    }

    public Optional<BugReport> findById(UUID id) {
        return Optional.ofNullable(bugsById.get(id));
    }

    public Collection<BugReport> findAll() {
        return bugsById.values();
    }
}




