package ru.hackaton.chatsync.target;

import org.bukkit.command.CommandSender;

public interface MessageTarget {

    void sendMessage(CommandSender sender, String message);

}
