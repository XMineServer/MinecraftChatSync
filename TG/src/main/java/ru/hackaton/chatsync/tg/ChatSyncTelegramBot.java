package ru.hackaton.chatsync.tg;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.hackaton.chatsync.ExternalUser;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;

@RequiredArgsConstructor
public class ChatSyncTelegramBot extends TelegramLongPollingBot {

    private final String token;
    private final String username;
    private final Plugin plugin;
    private String globalChatId = "-1000000000000";

    @Override
    public String getBotUsername() { return username; }

    @Override
    public String getBotToken() { return token; }

    /**
     * Обработка обновления Telegram
     * @param update обновление Telegram
     * @param forceSync если true — событие вызывается синхронно (для тестов)
     */
    public void handleUpdate(Update update, boolean forceSync) {
        if (!update.hasMessage() || update.getMessage().getText() == null) return;

        Message msg = update.getMessage();
        String tgName = msg.getFrom().getUserName();
        String text = msg.getText();
        ExternalUser user = new ExternalUser(tgName, TextColor.color(0x54, 0xa8, 0xde), "telegram");

        Runnable fireEvent;

        if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            fireEvent = () -> callGlobalEvent(user, text);
        } else if (msg.getChat().isUserChat()) {
            fireEvent = () -> callPrivateEvent(tgName, user, text);
        } else {
            return;
        }

        if (forceSync || isTestEnvironment()) {
            fireEvent.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, fireEvent);
        }
    }

    private void callGlobalEvent(ExternalUser user, String text) {
        if (isTestEnvironment()) {
            Bukkit.getServer().getPluginManager().callEvent(new ExternalGlobalChatMessageEvent(user, text));
        } else {
            Bukkit.getPluginManager().callEvent(new ExternalGlobalChatMessageEvent(user, text));
        }
    }

    private void callPrivateEvent(String tgName, ExternalUser user, String text) {
        Player target = Bukkit.getPlayerExact(tgName);
        if (target == null) return;

        if (isTestEnvironment()) {
            Bukkit.getServer().getPluginManager().callEvent(new ExternalPrivateChatMessageEvent(target, user, text));
        } else {
            Bukkit.getPluginManager().callEvent(new ExternalPrivateChatMessageEvent(target, user, text));
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        handleUpdate(update, false);
    }

    public void sendMessageToChannel(String message) {
        try { execute(new SendMessage(globalChatId, message)); } catch (Exception ignored) {}
    }

    private boolean isTestEnvironment() {
        return "true".equals(System.getProperty("mockbukkit"));
    }

    public void stopBot() {}
}
