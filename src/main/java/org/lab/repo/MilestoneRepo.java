package org.lab.repo;

import org.lab.domain.Milestone;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MilestoneRepo {
    private final ConcurrentHashMap<UUID, Milestone> milestonesById = new ConcurrentHashMap<>();

    public Milestone save(Milestone milestone) {
        milestonesById.put(milestone.id(), milestone);
        return milestone;
    }

    public Optional<Milestone> findById(UUID id) {
        return Optional.ofNullable(milestonesById.get(id));
    }

    public Collection<Milestone> findAll() {
        return milestonesById.values();
    }
}




