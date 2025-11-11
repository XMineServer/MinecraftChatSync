package ru.hackaton.chatsync.core;

import ru.hackaton.chatsync.api.ChatSyncPlatformAdapter;
import ru.hackaton.chatsync.core.db.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ChatSyncService {
    private final DataSourceProvider dsp;
    private final UserLinkRepository userLinks;
    private final GroupLinkRepository groupLinks;
    private final Map<String, ChatSyncPlatformAdapter> adapters = new ConcurrentHashMap<>();

    public ChatSyncService(DataSourceProvider dsp) {
        this.dsp = dsp;
        this.userLinks = new UserLinkRepository(dsp.get());
        this.groupLinks = new GroupLinkRepository(dsp.get());
    }

    public void registerAdapter(ChatSyncPlatformAdapter adapter) {
        adapters.put(adapter.getPlatformName(), adapter);
    }

    public void unregister(String platform) {
        adapters.remove(platform);
    }

    public PlatformCoreBridge bindFor(ChatSyncPlatformAdapter adapter) {
        return new PlatformCoreBridge(adapter.getPlatformName(), this);
    }

    void linkUser(String platform, String externalId, UUID playerUuid) throws SQLException {
        userLinks.link(platform, externalId, playerUuid);
    }

    void unlinkUser(String platform, String externalId) throws SQLException {
        userLinks.unlink(platform, externalId);
    }

    void linkGroup(String platform, List<String> path) throws SQLException {
        groupLinks.link(platform, path);
    }

    void unlinkGroup(String platform, List<String> path) throws SQLException {
        groupLinks.unlink(platform, path);
    }

    void dispatchPrivateMessage(String platform, String fromExternalId, UUID toPlayer, String text) {
        Optional<UUID> fromPlayer = userLinks.findPlayer(platform, fromExternalId);
        // FIXME: отправить лс Minecraft игроку (toPlayer)

        for (ChatSyncPlatformAdapter a : adapters.values()) {
            if (!a.getPlatformName().equals(platform)) {
                a.transmitPmToPlatform(fromExternalId, toPlayer.toString(), text);
            }
        }
    }

    void dispatchGroupMessage(String platform, String fromExternalId, String text, List<String> path) {
        Optional<UUID> fromPlayer = userLinks.findPlayer(platform, fromExternalId);
        // FIXME: отправить сообщение в общий чат Minecraft

        for (ChatSyncPlatformAdapter a : adapters.values()) {
            if (!a.getPlatformName().equals(platform)) {
                a.transmitGroupToPlatform(fromExternalId, text, path);
            }
        }
    }

    public void sendFromMinecraft(UUID fromPlayerUuid, String text) {
        for (ChatSyncPlatformAdapter a : adapters.values()) {
            a.transmitGroupToPlatform(fromPlayerUuid.toString(), text, List.of("server"));
        }
    }

    public void close() {
        dsp.close();
    }
}
