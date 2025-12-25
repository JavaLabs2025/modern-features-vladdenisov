package org.lab.service;

import org.lab.domain.User;
import org.lab.repo.UserRepo;

import java.util.UUID;

public final class AuthService {
    private final UserRepo userRepo;

    public AuthService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User register(String name) {
        var user = new User(UUID.randomUUID(), name);
        
        return userRepo.save(user);
    }
}




