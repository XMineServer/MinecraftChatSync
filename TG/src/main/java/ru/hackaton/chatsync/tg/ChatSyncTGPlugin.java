package ru.hackaton.chatsync.tg;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync.th")
public class ChatSyncTGPlugin extends JavaPlugin {

    private static ChatSyncTGPlugin INSTANCE;
    private BotService botService;

    public ChatSyncTGPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        String token = getConfig().getString("telegram.token");
        String username = getConfig().getString("telegram.username");

        botService = new BotService(token, username);
        try {
            botService.start();
        } catch (Exception e) {
            getLogger().severe("Telegram bot failed to start: " + e.getMessage());
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

    public static ChatSyncTGPlugin getInstance() {
        return INSTANCE;
    }
}
