package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Component;
import com.hakan.basicdi.annotations.PostConstruct;
import com.hakan.spinjection.listener.annotations.EventListener;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.target.MessageTarget;


@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BotService {

    private static final PlainTextComponentSerializer PLAIN_TEXT_COMPONENT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private final JavaPlugin plugin;
    private final Logger logger;
    private ChatSyncTelegramBot bot;

    private TelegramBotsApi api;

    private final UserLinkRepository userLinkRepository;

    private final GroupLinkRepository groupLinkRepository;

    @PostConstruct
    public void start() {
        try {
            this.api = new TelegramBotsApi(DefaultBotSession.class);
            this.bot = createBot();
            api.registerBot(bot);
            logger.info("Telegram bot started.");
        } catch (Exception e) {
            logger.error("Telegram bot failed to start: ", e);
        }
    }

    private ChatSyncTelegramBot createBot() {
        plugin.saveDefaultConfig();
        var config = plugin.getConfig();
        var token = config.getString("telegram.token");
        var username = config.getString("telegram.username");
        bot = new ChatSyncTelegramBot(token, username, plugin, userLinkRepository, groupLinkRepository);
        return bot;
    }

    @EventListener(priority = EventPriority.MONITOR)
    public void onChatEvent(AsyncChatEvent e) {
        String message = PLAIN_TEXT_COMPONENT_SERIALIZER.serialize(e.originalMessage());
        String playerName = e.getPlayer().getName();

        String telegramMessage = String.format("[%s] %s", playerName, message);

        bot.sendMessageToChannel(telegramMessage);
    }

    public void stop() {
        bot.stopBot();
        logger.info("Telegram bot stopped.");
    }

    public void sendToTelegram(String message) {
        bot.sendMessageToChannel(message);
    }

    public MessageTarget createPrivateMessageTarget(String nickname) throws IllegalArgumentException {
        return (sender, message) -> {
            String formatted = String.format("[PRIVATE] %s → %s: %s", sender.getName(), nickname, message);
            bot.sendPrivateMessage(nickname, formatted);
        };
    }

}
