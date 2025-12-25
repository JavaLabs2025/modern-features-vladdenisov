package org.lab.domain;

public sealed interface CommandResult<T> permits CommandResult.Ok, CommandResult.Error {
    // Sealed Classes

    record Ok<T>(T value) implements CommandResult<T> {
        // Records
    }

    record Error<T>(String message) implements CommandResult<T> {
        // Records
    }
}



