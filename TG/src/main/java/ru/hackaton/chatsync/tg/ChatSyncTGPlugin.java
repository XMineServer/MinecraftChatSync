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

    @Override
    public void onEnable() {
        SpigotBootstrap.run(this);
    }

    @Override
    public void onDisable() {
    }

    public static ChatSyncTGPlugin getInstance() {
        return INSTANCE;
    }


}