package ru.hackaton.chatsync.api;

import java.util.List;

/**
 * реализации - tg, ds и проч.
 */
public interface ChatSyncPlatformAdapter {
    String getPlatformName();
    void onEnable();
    void onDisable();

    // core -> платформа
    void transmitPmToPlatform(String fromExternalId, String toExternalId, String text);
    void transmitGroupToPlatform(String fromExternalId, String text, List<String> path);
}
