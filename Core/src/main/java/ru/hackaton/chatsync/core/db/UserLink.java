package ru.hackaton.chatsync.core.db;

import lombok.Value;
import java.time.Instant;

@Value
public class UserLink {
    int id;
    long userId;
    String platform;
    String externalId;
    Instant linkedAt;
}
