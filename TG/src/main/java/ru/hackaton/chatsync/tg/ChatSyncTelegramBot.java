package ru.hackaton.chatsync.tg;

import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hackaton.chatsync.ExternalUser;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.MinecraftUser;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;
import ru.hackaton.chatsync.core.service.UserLinkingService;

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
    private final UserLinkingService userLinkingService;
    private final MinecraftUserRepository minecraftUserRepository;

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
            Optional<Long> existing = userLinkRepository.findPlayerIdByExternal("telegram", tgUsername);
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
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    /**
     * Обработка обновления Telegram
     *
     * @param update    обновление Telegram
     * @param forceSync если true — событие вызывается синхронно (для тестов)
     */
    public void handleUpdate(Update update, boolean forceSync) {
        if (!update.hasMessage() || update.getMessage().getText() == null) return;

        Message msg = update.getMessage();
        String tgUsername = msg.getFrom().getUserName();
        String text = msg.getText();
        ExternalUser user = new ExternalUser(tgUsername, ChatSyncTGPlugin.color, "telegram");
        Long chatId = msg.getChatId();

        if (msg.getChat().isUserChat() && text.startsWith("/")) {

            if (text.startsWith("/link ")) {
                String playerName = text.substring(6).trim();
                Player player = Bukkit.getPlayer(playerName);

                if (player == null) {
                    sendPrivateMessage(chatId.toString(), "❌ Игрок " + playerName + " не найден на сервере.");
                    return;
                }

                try {
                    Optional<MinecraftUser> maybeUser = minecraftUserRepository.findMinecraftUser(playerName);
                    if (maybeUser.isEmpty()) {
                        sendPrivateMessage(chatId.toString(), "⚠️ Игрок не зарегистрирован в базе данных Minecraft.");
                        return;
                    }

                    long userId = maybeUser.get().getId();

                    String code = userLinkingService.initiateLink(userId, "telegram");

                    player.sendMessage("🔗 Ваш код подтверждения: " + code + "\nВведите в Telegram: /otp <код>");

                    sendPrivateMessage(chatId.toString(),
                            "✅ Код для привязки аккаунта отправлен в Minecraft игроку " + playerName);

                } catch (Exception e) {
                    sendPrivateMessage(chatId.toString(), "⚠️ Произошла ошибка при создании кода. Попробуйте позже.");
                    e.printStackTrace();
                }
                return;
            }

            else if (text.startsWith("/otp ")) {
                String code = text.substring(5).trim();
                try {
                    boolean success = userLinkingService.confirmLink(code, tgUsername);
                    if (success) {
                        sendPrivateMessage(chatId.toString(), "✅ Аккаунт успешно привязан!");
                    } else {
                        sendPrivateMessage(chatId.toString(), "❌ Неверный или просроченный код.");
                    }
                } catch (Exception e) {
                    sendPrivateMessage(chatId.toString(), "⚠️ Произошла ошибка при подтверждении кода.");
                    e.printStackTrace();
                }
                return;
            }
        }

        if (msg.getChat().isUserChat() && tgUsername != null) {
            cacheUserChat(tgUsername, chatId);
        } else if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            cacheGlobalChat(chatId);
        }

        Runnable fireEvent;
        if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            fireEvent = () -> callGlobalEvent(user, text);
        } else if (msg.getChat().isUserChat()) {
            fireEvent = () -> callPrivateEvent(tgUsername, user, text);
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
        Bukkit.getPluginManager().callEvent(new ExternalPrivateChatMessageEvent(false ,target, user, text));

    }


    public void sendPrivateMessage(String userId, String message) {
        try {
            execute(new SendMessage(userId, message));
        } catch (TelegramApiException e) {
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


    public void stopBot() {
    }
}
