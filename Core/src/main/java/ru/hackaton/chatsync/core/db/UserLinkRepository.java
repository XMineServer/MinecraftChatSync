package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public final class UserLinkRepository {

    private final DataSource ds;

    public void link(long userId, String platform, String externalId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "INSERT IGNORE INTO user_links (user_id, platform, external_id, linked_at) VALUES (?, ?, ?, NOW())")) {
            st.setLong(1, userId);
            st.setString(2, platform);
            st.setString(3, externalId);
            st.executeUpdate();
        }
    }

    public void delete(long userId, String platform) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "DELETE FROM user_links WHERE user_id = ? AND platform = ?")) {
            st.setLong(1, userId);
            st.setString(2, platform);
            st.executeUpdate();
        }
    }

    /**
     * привязан ли такой-то пользователь к платформе?
     */
    public boolean exists(long userId, String platform) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT 1 FROM user_links WHERE user_id = ? AND platform = ?")) {
            st.setLong(1, userId);
            st.setString(2, platform);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<UserLink> findByUser(long userId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT id, user_id, platform, external_id, linked_at FROM user_links WHERE user_id = ?")) {
            st.setLong(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                List<UserLink> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    public Optional<UserLink> findByUser(long userId, String platform) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT id, user_id, platform, external_id, linked_at " +
                             "FROM user_links WHERE user_id = ? AND platform = ? LIMIT 1")) {
            st.setLong(1, userId);
            st.setString(2, platform);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                } else {
                    return Optional.empty();
                }
            }
        }
    }


    /**
     * Ищет игрока по платформе и externalId, например, tg id
     */
    public Optional<Long> findPlayerIdByExternal(String platform, String externalId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT user_id FROM user_links WHERE platform = ? AND external_id = ?")) {
            st.setString(1, platform);
            st.setString(2, externalId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("user_id"));
                } else {
                    return Optional.empty();
                }
            }
        }
    }

    public List<LinkedUser> findByUsernameStartWith(String user, String platform) throws SQLException {
        String like = user + "%";

        String sql = """
            SELECT u.username, u.uuid, ul.external_id
            FROM users u
            JOIN user_links ul ON u.id = ul.user_id
            WHERE u.username LIKE ? AND ul.platform = ?
            """;

        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(sql)) {
            st.setString(1, like);
            st.setString(2, platform);
            try (ResultSet rs = st.executeQuery()) {
                List<LinkedUser> result = new ArrayList<>();
                while (rs.next()) {
                    String username = rs.getString("username");
                    String uuidStr = rs.getString("uuid");
                    String externalId = rs.getString("external_id");
                    result.add(new LinkedUser(username, java.util.UUID.fromString(uuidStr), externalId));
                }
                return result;
            }
        }
    }


    private UserLink map(ResultSet rs) throws SQLException {
        return new UserLink(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("platform"),
                rs.getString("external_id"),
                rs.getTimestamp("linked_at").toInstant()
        );
    }
}
