package ru.hackaton.chatsync;

import com.hakan.spinjection.listener.annotations.EventListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import ru.hackaton.chatsync.event.ExternalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;

@com.hakan.basicdi.annotations.Component
public class MinecraftMessagingService {

    private static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static final String ANONYMOUS_EXTERNAL_USER = "<bold>ANONYMOUS</bold>";
    private static final String UNKNOWN_EXTERNAL_USER = "<external_nickname>";
    private static final String KNOWN_EXTERNAL_USER = "<nickname> aka <external_nickname>";

    private static final String GLOBAL_MESSAGE = "\\<<nickname>> <message>";

    private static final String PRIVATE_TO_MESSAGE = "[<nickname> -> <red>you</red>] <message>";
    private static final String PRIVATE_FROM_MESSAGE = "[<red>you</red> -> <nickname>] <message>";

    public void sendMsg(CommandSender sender, CommandSender target, String message) {
        var messageFrom = MiniMessage.miniMessage().deserialize(
                PRIVATE_FROM_MESSAGE,
                Placeholder.unparsed("nickname", target.getName()),
                Placeholder.unparsed("message", message)
        );
        sender.sendMessage(messageFrom);
        var messageTo = MiniMessage.miniMessage().deserialize(
                PRIVATE_FROM_MESSAGE,
                Placeholder.unparsed("nickname", sender.getName()),
                Placeholder.unparsed("message", message)
        );
        target.sendMessage(messageTo);
    }

    //TODO: remove (sample)
    @EventListener(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMessage(AsyncChatEvent e) {
        String message = PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(e.originalMessage());
        var player = e.getPlayer();
        var event1 = new ExternalGlobalChatMessageEvent(
                player,
                new ExternalUser("test1", TextColor.color(125, 0, 125), "test1"),
                message
        );
        var event2 = new ExternalGlobalChatMessageEvent(
                new ExternalUser("test2", TextColor.color(125, 125, 0), "test2"),
                message
        );
        var event3 = new ExternalGlobalChatMessageEvent(
                new ExternalUser(null, TextColor.color(0, 125, 125), "test3"),
                message
        );
        Bukkit.getPluginManager().callEvent(event1);
        Bukkit.getPluginManager().callEvent(event2);
        Bukkit.getPluginManager().callEvent(event3);
    }

    @EventListener(priority = EventPriority.MONITOR)
    public void onExternalGlobalMessage(ExternalGlobalChatMessageEvent e) {
        final Component nickname = getExternalNickname(e);
        var message = MiniMessage.miniMessage().deserialize(
                GLOBAL_MESSAGE,
                Placeholder.component("nickname", nickname),
                Placeholder.unparsed("message", e.getMessage())
        );
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
    }

    @EventListener(priority = EventPriority.MONITOR)
    public void onExternalPrivateMessage(ExternalPrivateChatMessageEvent e) {
        var nickname = getExternalNickname(e);
        var message = MiniMessage.miniMessage().deserialize(
                PRIVATE_TO_MESSAGE,
                Placeholder.component("nickname", nickname),
                Placeholder.unparsed("message", e.getMessage())
        );
        e.getPlayer().sendMessage(message);
    }

    private Component getExternalNickname(ExternalChatMessageEvent event) {
        var user = event.getExternalUser();
        return event.getExternalPlayer()
                .map((player) -> getExternalNickname(user, player))
                .orElseGet(() -> getExternalNickname(user));
    }

    private Component getExternalNickname(@NotNull ExternalUser user, @NotNull OfflinePlayer player) {
        Component externalNickname = getExternalNickname(user);
        if (player.getName() != null) {
            return MiniMessage.miniMessage().deserialize(
                    KNOWN_EXTERNAL_USER,
                    Placeholder.component("external_nickname", externalNickname),
                    Placeholder.unparsed("nickname", player.getName())
            );
        } else {
            return externalNickname;
        }
    }

    private Component getExternalNickname(@NotNull ExternalUser user) {
        if (user.username() == null) {
            return MiniMessage.miniMessage()
                    .deserialize(ANONYMOUS_EXTERNAL_USER)
                    .color(user.color());
        } else {
            return MiniMessage.miniMessage()
                    .deserialize(
                            UNKNOWN_EXTERNAL_USER,
                            Placeholder.unparsed("external_nickname", user.username())
                    )
                    .color(user.color());
        }

    }

}
