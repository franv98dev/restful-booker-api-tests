package io.github.franv98dev.booker.model;

/** Request body of {@code POST /auth}. */
public record Credentials(String username, String password) {}
