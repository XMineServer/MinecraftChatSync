package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.core.service.UserLinkingService;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncTGPlugin plugin() {
        return ChatSyncTGPlugin.getInstance();
    }

    @Provide
    public Logger logger() {
        return ChatSyncTGPlugin.getInstance().getSLF4JLogger();
    }

    @Provide
    public UserLinkingService userLinkingService() {
        var repo = Bukkit.getServicesManager().load(UserLinkingService.class);
        if (repo == null) {
            throw new IllegalStateException("Can't found UserLinkingService");
        }
        return repo;
    }

    @Provide
    public UserLinkRepository userRepository() {
        var repo = Bukkit.getServicesManager().load(UserLinkRepository.class);
        if (repo == null) {
            throw new IllegalStateException("Can't found UserLinkRepository");
        }
        return repo;
    }

    @Provide
    public GroupLinkRepository groupRepository() {
        var repo = Bukkit.getServicesManager().load(GroupLinkRepository.class);
        if (repo == null) {
            throw new IllegalStateException("Can't found GroupLinkRepository");
        }
        return repo;
    }

    @Provide
    public MinecraftUserRepository minecraftUserRepository() {
        var repo = Bukkit.getServicesManager().load(MinecraftUserRepository.class);
        if (repo == null) {
            throw new IllegalStateException("Can't found MinecraftUserRepository");
        }
        return repo;
    }

}
