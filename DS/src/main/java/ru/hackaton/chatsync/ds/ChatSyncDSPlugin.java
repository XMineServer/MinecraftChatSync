package ru.hackaton.chatsync.ds;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync.th")
public class ChatSyncDSPlugin extends JavaPlugin {

    private static ChatSyncDSPlugin INSTANCE;
    private BotService botService;

    public ChatSyncDSPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String token = getConfig().getString("discord.token");
        String username = getConfig().getString("discord.username");

        botService = new BotService(token, username);
        try {
            botService.start();
        } catch (Exception e) {
            getLogger().severe("Discord bot failed to start: " + e.getMessage());
        }

        if (!isTestEnvironment()) {
            SpigotBootstrap.run(this);
        }
    }

    @Override
    public void onDisable() {
        if (botService != null) {
            botService.stop();
        }
    }

    private boolean isTestEnvironment() {
        return "true".equals(System.getProperty("mockbukkit"));
    }

    public static ChatSyncDSPlugin getInstance() {
        return INSTANCE;
    }
}
