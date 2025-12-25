package org.lab.service;

import java.util.UUID;

public final class Context {
    private Context() {}

    public static final ScopedValue<UUID> CURRENT_USER_ID = ScopedValue.newInstance();
    // Scoped Values
}




