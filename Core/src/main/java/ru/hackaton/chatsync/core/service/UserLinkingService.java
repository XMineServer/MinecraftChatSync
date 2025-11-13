package ru.hackaton.chatsync.core.service;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import com.hakan.spinjection.listener.annotations.EventListener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.slf4j.Logger;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class UserLinkingService {

    private final Logger logger;
    private final UserLinkRepository userLinkRepository;
    private final MinecraftUserRepository minecraftUserRepository;
    private final SecureRandom random = new SecureRandom();

    // код -> инфо о пользователе
    private final Map<String, PendingLink> pendingLinks = new ConcurrentHashMap<>();

    private final long linkTtlMillis;

    /**
     * Таймаут по умолчанию 5 минут
     */
    @Autowired
    public UserLinkingService(Logger logger, UserLinkRepository userLinkRepository, MinecraftUserRepository minecraftUserRepository) {
        this.logger = logger;
        this.userLinkRepository = userLinkRepository;
        this.minecraftUserRepository = minecraftUserRepository;
        this.linkTtlMillis = Duration.ofMinutes(5).toMillis();
    }

    public UserLinkingService(Logger logger, UserLinkRepository userLinkRepository, MinecraftUserRepository minecraftUserRepository, Duration ttl) {
        this.logger = logger;
        this.userLinkRepository = userLinkRepository;
        this.minecraftUserRepository = minecraftUserRepository;
        this.linkTtlMillis = ttl.toMillis();
    }

    @EventListener
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        try {
            minecraftUserRepository.upsertMinecraftUser(
                    e.getUniqueId(),
                    e.getPlayerProfile().getName()
            );
        } catch (SQLException ex) {
            logger.error("Fail to insert minecraft user", ex);
        }
    }

    /**
     * Этап 1. Сгенерировать код и отправить пользователю
     */
    public String initiateLink(int userId, String platform) {
        cleanupExpired();

        String code = generateCode();
        long now = System.currentTimeMillis();
        pendingLinks.put(code, new PendingLink(userId, platform, now));
        return code;
    }

    /**
     * Этап 2. Проверить код и, если он верный, записать связь в БД.
     */
    public boolean confirmLink(String code, String externalId) throws SQLException {
        cleanupExpired();

        PendingLink pending = pendingLinks.remove(code);
        if (pending == null)
            return false;

        userLinkRepository.link(pending.userId(), pending.platform(), externalId);

        // TODO: создать ивент об успешной привязке

        return true;
    }

    /**
     * Удаляет просроченные записи (старше TTL).
     */
    private void cleanupExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, PendingLink>> it = pendingLinks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, PendingLink> entry = it.next();
            if (now - entry.getValue().createdAt() > linkTtlMillis) {
                it.remove();
            }
        }
    }

    private String generateCode() {
        int num = 100_000 + random.nextInt(900_000);
        return String.valueOf(num);
    }

    private record PendingLink(int userId, String platform, long createdAt) {}
}
