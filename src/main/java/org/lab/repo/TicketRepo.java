package org.lab.repo;

import org.lab.domain.Ticket;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class TicketRepo {
    private final ConcurrentHashMap<UUID, Ticket> ticketsById = new ConcurrentHashMap<>();

    public Ticket save(Ticket ticket) {
        ticketsById.put(ticket.id(), ticket);
        return ticket;
    }

    public Optional<Ticket> findById(UUID id) {
        return Optional.ofNullable(ticketsById.get(id));
    }

    public Collection<Ticket> findAll() {
        return ticketsById.values();
    }
}




