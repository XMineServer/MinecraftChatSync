package ru.hackaton.chatsync.api.event;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class ExternalUserLinkEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final OfflinePlayer player;
    @NotNull
    private final String platform;
    @NotNull
    private final String externalId;
    @Nullable
    private final String externalUsername;

    public ExternalUserLinkEvent(
            @NotNull OfflinePlayer player,
            @NotNull String platform,
            @NotNull String externalId,
            @Nullable String externalUsername
    ) {
        super(true);
        this.player = player;
        this.platform = platform;
        this.externalId = externalId;
        this.externalUsername = externalUsername;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

}
