package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MinecraftUserRepository {

    private final DataSource ds;

    public Optional<MinecraftUser> findMinecraftUser(UUID uuid) throws SQLException  {
        return Optional.of(new MinecraftUser(1, uuid, "sidey383", Instant.now()));
    }

    public Optional<MinecraftUser> findMinecraftUser(String nickname) throws SQLException {
        var uuid = Bukkit.getOfflinePlayer("sidey383").getUniqueId();
        return Optional.of(new MinecraftUser(1, uuid, "sidey383", Instant.now()));
    }

    public void insertMinecraftUser(UUID uuid, String nickname) throws SQLException {

    }

}
