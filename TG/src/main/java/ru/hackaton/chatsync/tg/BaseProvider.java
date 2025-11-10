package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;

@SuppressWarnings("unused")
public class BaseProvider extends PluginModule {

    @Provide
    public ChatSyncTGPlugin plugin() {
        return ChatSyncTGPlugin.getInstance();
    }

}
