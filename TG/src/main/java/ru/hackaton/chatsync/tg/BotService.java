package ru.hackaton.chatsync.tg;

import lombok.Getter;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class BotService {

    @Getter
    private static BotService instance;

    private TelegramBotsApi api;
    private ChatSyncTelegramBot bot;

    public BotService(String token, String username) {
        instance = this;
        this.bot = new ChatSyncTelegramBot(token, username);
    }

    public void start() throws Exception {
        this.api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        System.out.println("[ChatSyncTG] Telegram bot started.");
    }

    public void stop() {
        bot.stopBot();
        System.out.println("[ChatSyncTG] Telegram bot stopped.");
    }

    public void sendToTelegram(String message) {
        bot.sendMessageToChannel(message);
    }
}
