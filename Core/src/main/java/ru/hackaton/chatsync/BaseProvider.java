package ru.hackaton.chatsync;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;
import org.slf4j.Logger;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncPlugin plugin() {
        return ChatSyncPlugin.getInstance();
    }

    @Provide
    public Logger logger() {
        return ChatSyncPlugin.getInstance().getSLF4JLogger();
    }

}
