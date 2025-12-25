package org.lab.repo;

import org.lab.domain.User;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserRepo {
    private final ConcurrentHashMap<UUID, User> usersById = new ConcurrentHashMap<>();

    public User save(User user) {
        usersById.put(user.id(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(usersById.get(id));
    }

    public Collection<User> findAll() {
        return usersById.values();
    }
}




