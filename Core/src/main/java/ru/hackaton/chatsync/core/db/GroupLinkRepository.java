package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Service;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public final class GroupLinkRepository {

    private final DataSource ds;

    public void link(String platform, List<String> path) throws SQLException {
        String pathStr = String.join("/", path);
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "INSERT IGNORE INTO group_links (platform, context_path, linked_at) " +
                             "VALUES (?, ?, NOW())")) {
            st.setString(1, platform);
            st.setString(2, pathStr);
            st.executeUpdate();
        }
    }

    public void unlink(String platform, List<String> path) throws SQLException {
        String pathStr = String.join("/", path);
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "DELETE FROM group_links WHERE platform = ? AND context_path = ?")) {
            st.setString(1, platform);
            st.setString(2, pathStr);
            st.executeUpdate();
        }
    }

    public boolean exists(String platform, List<String> path) throws SQLException {
        String pathStr = String.join("/", path);
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT 1 FROM group_links WHERE platform = ? AND context_path = ?")) {
            st.setString(1, platform);
            st.setString(2, pathStr);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<GroupLink> findAll() throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT id, platform, context_path, linked_at FROM group_links")) {
            try (ResultSet rs = st.executeQuery()) {
                List<GroupLink> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    public List<GroupLink> findByPlatform(String platform) throws SQLException {
        try (Connection c = ds.getConnection();
             PreparedStatement st = c.prepareStatement(
                     "SELECT id, platform, context_path, linked_at FROM group_links WHERE platform = ?")) {
            st.setString(1, platform);
            try (ResultSet rs = st.executeQuery()) {
                List<GroupLink> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        }
    }

    private GroupLink map(ResultSet rs) throws SQLException {
        return new GroupLink(
                rs.getLong("id"),
                rs.getString("platform"),
                rs.getString("context_path"),
                rs.getTimestamp("linked_at").toInstant()
        );
    }
}
