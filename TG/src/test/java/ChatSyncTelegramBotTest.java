import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import ru.hackaton.chatsync.event.ExternalGlobalChatMessageEvent;
import ru.hackaton.chatsync.event.ExternalPrivateChatMessageEvent;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.hackaton.chatsync.tg.ChatSyncTGPlugin;
import ru.hackaton.chatsync.tg.ChatSyncTelegramBot;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatSyncTelegramBotTest {

    private ServerMock server;
    private ChatSyncTGPlugin plugin;

    @BeforeEach
    void setUp() {
        System.setProperty("mockbukkit", "true");
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ChatSyncTGPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

//    @Test
//    void groupMessage_shouldFireExternalGlobalEvent() throws InterruptedException {
//        Update update = new Update();
//        Message msg = new Message();
//        msg.setText("hello from tg group");
//
//        Chat chat = new Chat();
//        chat.setId(-100L);
//        chat.setType("group");
//        msg.setChat(chat);
//
//        User user = new User();
//        user.setUserName("tgUser");
//        user.setId(123L);
//        msg.setFrom(user);
//
//        update.setMessage(msg);
//
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<ExternalGlobalChatMessageEvent> captured = new AtomicReference<>();
//
//        server.getPluginManager().registerEvents(new Listener() {
//            @EventHandler
//            public void onGlobal(ExternalGlobalChatMessageEvent e) {
//                captured.set(e);
//                latch.countDown();
//            }
//        }, plugin);
//
//        ChatSyncTelegramBot bot = new ChatSyncTelegramBot("token", "botName");
//
//        bot.handleUpdate(update, true);
//
//        boolean fired = latch.await(2, TimeUnit.SECONDS);
//        assertTrue(fired, "ExternalGlobalChatMessageEvent должен был быть вызван");
//        ExternalGlobalChatMessageEvent event = captured.get();
//        assertNotNull(event);
//        assertEquals("hello from tg group", event.getMessage());
//        assertEquals("telegram", event.getExternalUser().source());
//    }
//
//    @Test
//    void privateMessage_whenPlayerOnline_shouldFireExternalPrivateEvent() throws InterruptedException {
//        PlayerMock online = server.addPlayer("tgUser");
//
//        Update update = new Update();
//        Message msg = new Message();
//        msg.setText("private hello");
//
//        Chat chat = new Chat();
//        chat.setId(1L);
//        chat.setType("private");
//        msg.setChat(chat);
//
//        User user = new User();
//        user.setUserName("tgUser");
//        user.setId(321L);
//        msg.setFrom(user);
//
//        update.setMessage(msg);
//
//        CountDownLatch latch = new CountDownLatch(1);
//        AtomicReference<ExternalPrivateChatMessageEvent> captured = new AtomicReference<>();
//
//        server.getPluginManager().registerEvents(new Listener() {
//            @EventHandler
//            public void onPrivate(ExternalPrivateChatMessageEvent e) {
//                captured.set(e);
//                latch.countDown();
//            }
//        }, plugin);
//
//        ChatSyncTelegramBot bot = new ChatSyncTelegramBot("token", "botName");
//        bot.handleUpdate(update, true); // Синхронный вызов для теста
//
//        boolean fired = latch.await(2, TimeUnit.SECONDS);
//        assertTrue(fired, "ExternalPrivateChatMessageEvent должен был быть вызван для онлайн игрока");
//
//        ExternalPrivateChatMessageEvent ev = captured.get();
//        assertNotNull(ev);
//        assertEquals("private hello", ev.getMessage());
//        assertEquals("telegram", ev.getExternalUser().source());
//        assertTrue(ev.getPlayer().isOnline());
//        assertEquals("tgUser", ev.getPlayer().getName());
//    }
//
//    @Test
//    void privateMessage_whenPlayerOffline_shouldNotFirePrivateEvent() throws InterruptedException {
//        Update update = new Update();
//        Message msg = new Message();
//        msg.setText("private hello");
//
//        Chat chat = new Chat();
//        chat.setId(2L);
//        chat.setType("private");
//        msg.setChat(chat);
//
//        User user = new User();
//        user.setUserName("ghostUser");
//        user.setId(999L);
//        msg.setFrom(user);
//
//        update.setMessage(msg);
//
//        CountDownLatch latch = new CountDownLatch(1);
//
//        server.getPluginManager().registerEvents(new Listener() {
//            @EventHandler
//            public void onPrivate(ExternalPrivateChatMessageEvent e) {
//                latch.countDown();
//            }
//        }, plugin);
//
//        ChatSyncTelegramBot bot = new ChatSyncTelegramBot("token", "botName");
//        bot.handleUpdate(update, true); // Синхронный вызов
//
//        boolean fired = latch.await(500, TimeUnit.MILLISECONDS);
//        assertFalse(fired, "ExternalPrivateChatMessageEvent не должен вызываться для оффлайн-игрока");
//    }
}
