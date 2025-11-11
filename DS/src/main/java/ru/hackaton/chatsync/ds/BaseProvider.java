package ru.hackaton.chatsync.ds;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncDSPlugin plugin() {
        return ChatSyncDSPlugin.getInstance();
    }

    @Provide
    public Logger logger() {
        return ChatSyncDSPlugin.getInstance().getSLF4JLogger();
    }

}
