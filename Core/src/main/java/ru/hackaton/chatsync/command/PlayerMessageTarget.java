package ru.hackaton.chatsync.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.hackaton.chatsync.MinecraftMessagingService;
import ru.hackaton.chatsync.target.MessageTarget;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class PlayerMessageTarget implements MessageTarget {

    private final MinecraftMessagingService minecraftMessagingService;
    private final Player target;

    @Override
    public void sendMessage(CommandSender sender, String message) {
        minecraftMessagingService.sendMsg(sender, target, message);
    }
}
