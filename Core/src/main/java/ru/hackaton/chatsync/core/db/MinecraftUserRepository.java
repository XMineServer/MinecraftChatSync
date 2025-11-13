package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MinecraftUserRepository {

    private final DataSource ds;

    public Optional<MinecraftUser> findMinecraftUser(UUID uuid) throws SQLException {
        String sql = """
            SELECT id, uuid, username, created_at
            FROM users
            WHERE uuid = ?
        """;

        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setString(1, uuid.toString());

            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public Optional<MinecraftUser> findMinecraftUser(String nickname) throws SQLException {
        String sql = """
            SELECT id, uuid, username, created_at
            FROM users
            WHERE username = ?
            LIMIT 1
        """;

        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setString(1, nickname);

            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        }
    }

    public void insertMinecraftUser(UUID uuid, String username) throws SQLException {
        String sql = "INSERT INTO users (uuid, username) VALUES (?, ?)";

        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {

            st.setString(1, uuid.toString());
            st.setString(2, username);
            st.executeUpdate();
        }
    }

    private MinecraftUser map(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String username = rs.getString("username");
        Instant createdAt = rs.getTimestamp("created_at").toInstant();

        return new MinecraftUser(id, uuid, username, createdAt);
    }
}
