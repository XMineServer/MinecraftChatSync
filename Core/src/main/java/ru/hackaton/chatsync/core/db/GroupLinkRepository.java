package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Component;
import lombok.RequiredArgsConstructor;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public final class GroupLinkRepository {

    private final DataSource ds;

    public void link(String platform, List<String> path) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "INSERT INTO group_links (platform, context_path, linked_at) VALUES (?, ?, now()) " +
                     "ON CONFLICT (platform, context_path) DO NOTHING")) {
            st.setString(1, platform);
            st.setArray(2, c.createArrayOf("text", path.toArray()));
            st.executeUpdate();
        }
    }

    public void unlink(String platform, List<String> path) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "DELETE FROM group_links WHERE platform=? AND context_path=?")) {
            st.setString(1, platform);
            st.setArray(2, c.createArrayOf("text", path.toArray()));
            st.executeUpdate();
        }
    }

    public boolean exists(String platform, List<String> path) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT 1 FROM group_links WHERE platform=? AND context_path=?")) {
            st.setString(1, platform);
            st.setArray(2, c.createArrayOf("text", path.toArray()));
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }
}
