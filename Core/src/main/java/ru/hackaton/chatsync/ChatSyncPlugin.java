package ru.hackaton.chatsync;

import com.hakan.basicdi.annotations.Service;
import com.hakan.spinjection.SpigotBootstrap;
import com.hakan.spinjection.annotations.Scanner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hackaton.chatsync.core.db.DataSourceProvider;

@Scanner("ru.hackaton.chatsync")
public class ChatSyncPlugin extends JavaPlugin {

    private static ChatSyncPlugin INSTANCE;

    public ChatSyncPlugin() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        SpigotBootstrap.run(this).getEntities().stream()
                .filter(e -> e.getType().isAnnotationPresent(Service.class))
                .forEach(e -> registerService(e.getType(), e.getInstance()));
    }

    @SuppressWarnings("unchecked")
    private <T> void registerService(Class<T> type, Object object) {
        Bukkit.getServicesManager().register(type, (T) object, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        var dataSourceProvider = (DataSourceProvider) SpigotBootstrap.of(this.getClass()).getEntity(DataSourceProvider.class).getInstance();
        dataSourceProvider.close();
    }

    public static ChatSyncPlugin getInstance() {
        return INSTANCE;
    }

}
