package ru.hackaton.chatsync.tg;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.reactions.SetMessageReaction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.reactions.ReactionTypeEmoji;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.hackaton.chatsync.ExternalUser;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.MinecraftUser;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.core.service.UserLinkingService;
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
    private final UserLinkingService userLinkingService;
    private final MinecraftUserRepository minecraftUserRepository;
    private final Logger logger;

    public void sendGlobalMessage(String message) {
        try {
            var groupLinks = groupLinkRepository.findByPlatform("telegram");
            for (var group : groupLinks) {
                try {
                    SendMessage action;
                    String[] parts = group.getContextPath().split("/", 2);
                    action = new SendMessage(parts[0], message);
                    if (parts.length >= 2) {
                        action.setMessageThreadId(Integer.parseInt(parts[1]));
                    }
                    execute(action);

                } catch (Exception e) {
                    logger.warn("Global message send error", e);
                }
            }
        } catch (SQLException e) {
            logger.warn("Fail to send global message", e);
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
        String text = msg.getText();
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
                    logger.warn("Opt cod creation error", e);
                }
                return;
            } else if (text.startsWith("/otp ")) {
                String code = text.substring(5).trim();
                try {
                    boolean success = userLinkingService.confirmLink(code, Long.toString(chatId));
                    if (success) {
                        sendPrivateMessage(chatId.toString(), "✅ Аккаунт успешно привязан!");
                    } else {
                        sendPrivateMessage(chatId.toString(), "❌ Неверный или просроченный код.");
                    }
                } catch (Exception e) {
                    sendPrivateMessage(chatId.toString(), "⚠️ Произошла ошибка при подтверждении кода.");
                    logger.warn("Opt code confirm error", e);
                }
                return;
            } else if (text.startsWith("/msg ")) {
                String body = text.substring(5).trim();
                String[] arguments = body.split("\\s", 2);
                if (arguments.length != 2) {
                    sendPrivateMessage(chatId.toString(), "Используйте /msg <ник игрока> <сообщение>");
                    return;
                }
                String targetName = arguments[0];
                String message = arguments[1];
                Player target = Bukkit.getPlayer(targetName);
                if (target == null) {
                    sendPrivateMessage(chatId.toString(), "Игрок не на сервере");
                    return;
                }

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> callPrivateEvent(target, msg, message));

                okReaction(msg.getChatId().toString(), msg.getMessageId());
            }
        }

        if (msg.getChat().isGroupChat() || msg.getChat().isSuperGroupChat()) {
            List<String> target;
            if (msg.getMessageThreadId() != null) {
                target = List.of(chatId.toString(), msg.getMessageThreadId().toString());
            } else {
                target = List.of(chatId.toString());
            }
            try {
                if (groupLinkRepository.exists("telegram", target)) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> callGlobalEvent(msg, text));
                }
            } catch (SQLException e) {
                logger.error("Group check failed", e);
            }
        }
    }


    private void callGlobalEvent(Message msg, String text) {
        var sourcePlayer = findInternalUser(msg);
        var user = createExternalUser(msg);
        Bukkit.getPluginManager().callEvent(new ExternalGlobalChatMessageEvent(sourcePlayer, user, text));
    }

    private void callPrivateEvent(Player target, Message msg, String text) {
        var sourcePlayer = findInternalUser(msg);
        var user = createExternalUser(msg);
        Bukkit.getPluginManager().callEvent(new ExternalPrivateChatMessageEvent(false, target, sourcePlayer, user, text));
    }

    private ExternalUser createExternalUser(Message msg) {
        return new ExternalUser(msg.getFrom().getUserName(), ChatSyncTGPlugin.color, "telegram");
    }

    private OfflinePlayer findInternalUser(Message msg) {
        OfflinePlayer source = null;
        try {
            var optId = userLinkRepository.findPlayerIdByExternal("telegram", msg.getFrom().getId().toString());
            if (optId.isPresent()) {
                var optPlayer = minecraftUserRepository.findById(optId.get());
                source = optPlayer
                        .map(mp -> Bukkit.getOfflinePlayer(mp.getUuid()))
                        .orElse(null);
            }
        } catch (SQLException e) {
            logger.warn("Fail to find internal player", e);
        }
        return source;
    }


    public void sendPrivateMessage(String userId, String message) {
        try {
            execute(new SendMessage(userId, message));
        } catch (TelegramApiException e) {
            logger.warn("Fail to send private message", e);
        }
    }

    public void okReaction(String chatId, Integer messageId) {
        try {
            SetMessageReaction messageReaction = new SetMessageReaction(chatId, messageId);
            messageReaction.setReactionTypes(List.of(new ReactionTypeEmoji("emoji" ,"\uD83D\uDC4D")));
            execute(messageReaction);
        } catch (TelegramApiException e) {
            logger.warn("Fail to send message reaction", e);
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
