package ru.hackaton.chatsync.core.db;

import lombok.Value;

import java.time.Instant;

@Value
public class GroupLink {
    int id;
    String platform;
    String contextPath;
    Instant linkedAt;
}
