package ru.hackaton.chatsync.tg;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync.th")
public class ChatSyncTGPlugin extends JavaPlugin {

    private static ChatSyncTGPlugin INSTANCE;

    public ChatSyncTGPlugin() {
        INSTANCE = this;
    }

    private BotService botService;

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

        SpigotBootstrap.run(this);
    }

    @Override
    public void onDisable() {
        if (botService != null) {
            botService.stop();
        }
    }

    public static ChatSyncTGPlugin getInstance() {
        return INSTANCE;
    }


}