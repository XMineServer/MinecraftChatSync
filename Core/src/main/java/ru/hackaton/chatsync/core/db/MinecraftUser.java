package ru.hackaton.chatsync.core.db;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MinecraftUser {
    private final long id;
    private final UUID uuid;
    private final String username;
    private final Instant createdAt;
}
