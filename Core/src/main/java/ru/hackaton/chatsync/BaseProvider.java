package ru.hackaton.chatsync;

import com.hakan.basicdi.annotations.Provide;

@SuppressWarnings("unused")
public class BaseProvider {

    @Provide
    public ChatSyncPlugin plugin() {
        return ChatSyncPlugin.getInstance();
    }

}
