package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;
import org.slf4j.Logger;

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

}
