package ru.hackaton.chatsync.event;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hackaton.chatsync.ExternalUser;

import java.util.Optional;

@Getter
public abstract class ExternalChatMessageEvent extends Event {

    @Nullable
    private final OfflinePlayer externalPlayer;
    @NotNull
    private final ExternalUser externalUser;
    @NotNull
    private final String message;

    /**
     * Сообщение, отправленное от неизвестного пользователя извне
     * */
    public ExternalChatMessageEvent(@NotNull ExternalUser user, @NotNull String message) {
        super(true);
        this.externalPlayer = null;
        this.externalUser = user;
        this.message = message;
    }

    /**
     * Сообщение, отправленное от авторизованного пользователя извне
     * */
    public ExternalChatMessageEvent(@NotNull OfflinePlayer player, @NotNull ExternalUser user, @NotNull String message) {
        super(true);
        this.externalPlayer = player;
        this.externalUser = user;
        this.message = message;
    }

    public Optional<OfflinePlayer> getExternalPlayer() {
        return Optional.ofNullable(externalPlayer);
    }

}
