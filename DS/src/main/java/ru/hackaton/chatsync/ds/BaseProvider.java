package ru.hackaton.chatsync.ds;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncDSPlugin plugin() {
        return ChatSyncDSPlugin.getInstance();
    }

}
