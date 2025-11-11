package ru.hackaton.chatsync.ds;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync.ds")
public class ChatSyncDSPlugin extends JavaPlugin {

    private static ChatSyncDSPlugin INSTANCE;

    public ChatSyncDSPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        SpigotBootstrap.run(this);
    }

    @Override
    public void onDisable() {
        var botService = (BotService) SpigotBootstrap.of(this.getClass()).getEntity(BotService.class).getInstance();
        botService.stop();
    }

    public static ChatSyncDSPlugin getInstance() {
        return INSTANCE;
    }
}
