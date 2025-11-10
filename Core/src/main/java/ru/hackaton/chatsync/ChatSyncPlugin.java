package ru.hackaton.chatsync;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatSyncPlugin extends JavaPlugin {

    @Override
    public void onEnable() {

    }

    public void loadConfiguration() {
        ConfigurationSection config = getConfig();
        //TODO: загрузка конфигурации
    }

}
