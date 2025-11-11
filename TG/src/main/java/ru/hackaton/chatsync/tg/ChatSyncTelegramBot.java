package ru.hackaton.chatsync.tg;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class ChatSyncTelegramBot extends TelegramLongPollingBot {

    private final String token;
    private final String username;

    @Override
    public String getBotUsername() { return username; }

    @Override
    public String getBotToken() { return token; }

    @Override
    public void onUpdateReceived(Update update) {
    }

    public void sendMessageToChannel(String message) {
        String chatId = "-1000000000000";
        try {
            execute(new SendMessage(chatId, message));
        } catch (Exception ignored) {}
    }

    public void stopBot() {
    }
}
