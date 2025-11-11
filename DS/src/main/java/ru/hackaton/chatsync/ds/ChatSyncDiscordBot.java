package ru.hackaton.chatsync.ds;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import ru.hackaton.chatsync.ExternalUser;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;

@RequiredArgsConstructor
public class ChatSyncDiscordBot extends DiscordLongPollingBot {

    private final String token;
    private final String username;
    private final Plugin plugin = ChatSyncDSPlugin.getInstance();
    private String globalChatId = "-1000000000000";

    @Override
    public String getBotUsername() { return username; }

    @Override
    public String getBotToken() { return token; }

    /**
     * Обработка обновления Discord
     * @param update обновление Discord
     * @param forceSync если true — событие вызывается синхронно (для тестов)
     */
    public void handleUpdate(Update update, boolean forceSync) {
        if (!update.hasMessage() || update.getMessage().getText() == null) return;

        Message msg = update.getMessage();
        String dsName = msg.getFrom().getUserName();
        String text = msg.getText();
        ExternalUser user = new ExternalUser(dsName, TextColor.color(0x58, 0x65, 0xf2), "discord");

        Runnable fireEvent;

        if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            fireEvent = () -> callGlobalEvent(user, text);
        } else if (msg.getChat().isUserChat()) {
            fireEvent = () -> callPrivateEvent(dsName, user, text);
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

    private void callPrivateEvent(String dsName, ExternalUser user, String text) {
        Player target = Bukkit.getPlayerExact(dsName);
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
