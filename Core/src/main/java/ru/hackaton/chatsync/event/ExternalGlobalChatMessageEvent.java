package ru.hackaton.chatsync.event;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hackaton.chatsync.ExternalUser;

@Getter
public class ExternalGlobalChatMessageEvent extends ExternalChatMessageEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Сообщение, отправленное от неизвестного пользователя извне
     * */
    public ExternalGlobalChatMessageEvent(@NotNull ExternalUser user, @NotNull String message) {
        super(user, message);
    }

    /**
     * Сообщение, отправленное от авторизованного пользователя извне
     * */
    public ExternalGlobalChatMessageEvent(@Nullable OfflinePlayer player, @NotNull ExternalUser user, @NotNull String message) {
        super(player, user, message);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @SuppressWarnings("unused")
    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }


}
