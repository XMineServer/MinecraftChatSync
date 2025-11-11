package ru.hackaton.chatsync.core;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import com.hakan.spinjection.listener.annotations.EventListener;
import lombok.RequiredArgsConstructor;
import ru.hackaton.chatsync.api.ChatSyncPlatformAdapter;
import ru.hackaton.chatsync.api.event.ExternalUserLinkEvent;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public final class ChatSyncService {
    private final UserLinkRepository userLinks;
    private final GroupLinkRepository groupLinks;
    private final Map<String, ChatSyncPlatformAdapter> adapters = new ConcurrentHashMap<>();

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

    @EventListener
    public void onUserLink(ExternalUserLinkEvent e) throws SQLException {
        userLinks.link(e.getPlatform(), e.getExternalId(), e.getPlayer().getUniqueId());
    }

}
