package org.lab.domain;

import java.util.UUID;

public value class ProjectId {
    // Valhalla: Value Classes

    private final UUID value;

    public ProjectId(UUID value) {
        this.value = value;
    }

    public static ProjectId random() {
        return new ProjectId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }
}



