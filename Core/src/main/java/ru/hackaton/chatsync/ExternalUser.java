package ru.hackaton.chatsync;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ExternalUser(
        @Nullable String username,
        @NotNull TextColor color,
        @NotNull String source
) {
}
