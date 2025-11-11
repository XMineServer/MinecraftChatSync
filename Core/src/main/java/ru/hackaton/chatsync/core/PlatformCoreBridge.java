package ru.hackaton.chatsync.core;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * для взаимодействия платформа -> core
 */
public final class PlatformCoreBridge {
    private final String platform;
    private final ChatSyncService core;

    PlatformCoreBridge(String platform, ChatSyncService core) {
        this.platform = platform;
        this.core = core;
    }

    public void linkUser(String externalId, UUID playerUuid) throws SQLException {
        core.linkUser(platform, externalId, playerUuid);
    }

    public void unlinkUser(String externalId) throws SQLException {
        core.unlinkUser(platform, externalId);
    }

    public void linkGroup(List<String> path) throws SQLException {
        core.linkGroup(platform, path);
    }

    public void unlinkGroup(List<String> path) throws SQLException {
        core.unlinkGroup(platform, path);
    }

    public void sendPrivateMessage(String fromExternalId, UUID toPlayerUuid, String text) {
        core.dispatchPrivateMessage(platform, fromExternalId, toPlayerUuid, text);
    }

    public void sendGroupMessage(String fromExternalId, String text, List<String> path) {
        core.dispatchGroupMessage(platform, fromExternalId, text, path);
    }
}
