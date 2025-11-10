package ru.hackaton.chatsync;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncPlugin plugin() {
        return ChatSyncPlugin.getInstance();
    }

}
