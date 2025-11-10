package ru.hackaton.chatsync;

import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

@Scanner("ru.hackaton.chatsync")
public class ChatSyncPlugin extends JavaPlugin {

    private static ChatSyncPlugin INSTANCE;

    public ChatSyncPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        SpigotBootstrap.run(this);

    }

    @Override
    public void onDisable() {
    }

    public void loadConfiguration() {
        ConfigurationSection config = getConfig();
        //TODO: загрузка конфигурации
    }

    public static ChatSyncPlugin getInstance() {
        return INSTANCE;
    }

}
