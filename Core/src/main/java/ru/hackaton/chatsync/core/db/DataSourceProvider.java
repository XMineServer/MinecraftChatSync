package ru.hackaton.chatsync.core.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import javax.sql.DataSource;

public final class DataSourceProvider {

    private final HikariDataSource dataSource;

    public DataSourceProvider(@NotNull String url, @NotNull String user, @NotNull String password) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(password);
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(10_000);
        cfg.setLeakDetectionThreshold(15_000);
        cfg.setPoolName("ChatSyncPool");
        this.dataSource = new HikariDataSource(cfg);
    }

    public DataSource get() {
        return dataSource;
    }

    public void close() {
        dataSource.close();
    }
}
