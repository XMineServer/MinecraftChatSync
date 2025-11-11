package ru.hackaton.chatsync.core.db;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public final class UserLinkRepository {

    private final DataSource ds;

    public UserLinkRepository(DataSource ds) {
        this.ds = ds;
    }

    public void link(String platform, String externalId, UUID playerUuid) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "INSERT INTO user_links (platform, external_id, user_id, linked_at) " +
                     "SELECT ?, ?, u.id, now() FROM users u WHERE u.uuid = ? " +
                     "ON CONFLICT (platform, external_id) DO UPDATE SET user_id = EXCLUDED.user_id")) {
            st.setString(1, platform);
            st.setString(2, externalId);
            st.setObject(3, playerUuid);
            st.executeUpdate();
        }
    }

    public void unlink(String platform, String externalId) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "DELETE FROM user_links WHERE platform=? AND external_id=?")) {
            st.setString(1, platform);
            st.setString(2, externalId);
            st.executeUpdate();
        }
    }

    public Optional<UUID> findPlayer(String platform, String externalId) {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT u.uuid FROM user_links ul JOIN users u ON u.id=ul.user_id " +
                             "WHERE ul.platform=? AND ul.external_id=?")) {
            st.setString(1, platform);
            st.setString(2, externalId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return Optional.of((UUID) rs.getObject("uuid"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

}
