package ru.hackaton.chatsync.tg;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync.tg")
public class ChatSyncTGPlugin extends JavaPlugin {

    private static ChatSyncTGPlugin INSTANCE;
    public static final TextColor color = TextColor.color(0x54, 0xa8, 0xde);

    public ChatSyncTGPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!isTestEnvironment()) {
            SpigotBootstrap.run(this);
        }
    }

    @Override
    public void onDisable() {
        var botService = (BotService) SpigotBootstrap.of(this.getClass()).getEntity(BotService.class).getInstance();
        botService.stop();
    }

    private boolean isTestEnvironment() {
        return "true".equals(System.getProperty("mockbukkit"));
    }

    public static ChatSyncTGPlugin getInstance() {
        return INSTANCE;
    }
}
