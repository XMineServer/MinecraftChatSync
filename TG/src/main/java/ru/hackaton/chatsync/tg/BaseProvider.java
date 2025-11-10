package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Provide;

@SuppressWarnings("unused")
public class BaseProvider {

    @Provide
    public ChatSyncTGPlugin plugin() {
        return ChatSyncTGPlugin.getInstance();
    }

}
