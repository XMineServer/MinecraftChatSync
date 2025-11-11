package ru.hackaton.chatsync.ds;

import lombok.Getter;

public class BotService {

    @Getter
    private static BotService instance;

    private DiscordBotsApi api;
    private ChatSyncDiscordBot bot;

    public BotService(String token, String username) {
        instance = this;
        this.bot = new ChatSyncDiscordBot(token, username);
    }

    public void start() throws Exception {
        this.api = new DiscordBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        System.out.println("[ChatSyncDS] Discord bot started.");
    }

    public void stop() {
        bot.stopBot();
        System.out.println("[ChatSyncDS] Discord bot stopped.");
    }

    public void sendToDiscord(String message) {
        bot.sendMessageToChannel(message);
    }
}
