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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hackaton.chatsync.ExternalUser;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ChatSyncTelegramBot extends TelegramLongPollingBot {

    private final String token;
    private final String username;
    private final Plugin plugin;

    private final UserLinkRepository userLinkRepository;
    private final GroupLinkRepository groupLinkRepository;

    public void sendGlobalMessage(String message) {
        try {
            var groupLinks = groupLinkRepository.findByPlatform("telegram");
            for (var group : groupLinks) {
                try {
                    execute(new SendMessage(group.getContextPath(), message));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cacheUserChat(String tgUsername, Long chatId) {
        try {
            Optional<Integer> existing = userLinkRepository.findPlayerIdByExternal("telegram", tgUsername);
            if (existing.isEmpty()) {
                //should be user_id = chat_id, external_id = username
                userLinkRepository.link(chatId, "telegram", tgUsername);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cacheGlobalChat(Long chatId) {
        try {
            boolean exists = groupLinkRepository.findByPlatform("telegram").stream()
                    .anyMatch(gl -> gl.getContextPath().equals(chatId.toString()));
            if (!exists) {
                groupLinkRepository.link("telegram", List.of(chatId.toString()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
        Long chatId = msg.getChatId();

        if (msg.getChat().isUserChat() && tgName != null) {
            cacheUserChat(tgName, chatId);
        } else if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            cacheGlobalChat(chatId);
        }

        Runnable fireEvent;

        if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            fireEvent = () -> callGlobalEvent(user, text);
        } else if (msg.getChat().isUserChat()) {
            fireEvent = () -> callPrivateEvent(tgName, user, text);
        } else {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, fireEvent);
    }


    private void callGlobalEvent(ExternalUser user, String text) {
       Bukkit.getPluginManager().callEvent(new ExternalGlobalChatMessageEvent(user, text));
    }

    private void callPrivateEvent(String tgName, ExternalUser user, String text) {
        Player target = Bukkit.getPlayerExact(tgName);
        if (target == null) return;
        Bukkit.getPluginManager().callEvent(new ExternalPrivateChatMessageEvent(target, user, text));

    }


    public void sendPrivateMessage(String username, String message) {
        try {
            Optional<Integer> maybeUserId = userLinkRepository.findPlayerIdByExternal("telegram", username);
            maybeUserId.ifPresent(userId -> {
                String chatId = maybeUserId.get().toString();
                try {
                    execute(new SendMessage(chatId, message));
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onUpdateReceived(Update update) {
        handleUpdate(update, false);
    }

    public void sendMessageToChannel(String message) {
        sendGlobalMessage(message);
    }


    public void stopBot() {}
}
