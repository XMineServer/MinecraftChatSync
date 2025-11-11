package ru.hackaton.chatsync.event;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hackaton.chatsync.ExternalUser;

@Getter
public class ExternalPrivateChatMessageEvent extends ExternalChatMessageEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;

    public ExternalPrivateChatMessageEvent(@NotNull Player player, @Nullable OfflinePlayer userPlayer, @NotNull ExternalUser user, @NotNull String message) {
        super(userPlayer, user, message);
        this.player = player;
    }

    public ExternalPrivateChatMessageEvent(@NotNull Player player, @NotNull ExternalUser user, @NotNull String message) {
        super(user, message);
        this.player = player;
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
