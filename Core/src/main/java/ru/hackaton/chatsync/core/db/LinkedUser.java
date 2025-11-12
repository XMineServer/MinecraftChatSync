package ru.hackaton.chatsync.core.db;

import lombok.Value;

import java.util.UUID;

@Value
public class LinkedUser {
    private String username;
    private UUID uuid;
    private String externalId;
}
