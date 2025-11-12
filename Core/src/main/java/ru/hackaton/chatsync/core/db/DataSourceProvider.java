package ru.hackaton.chatsync.core.db;

import com.hakan.basicdi.annotations.Provide;
import com.hakan.spinjection.module.PluginModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ru.hackaton.chatsync.ChatSyncPlugin;

import javax.sql.DataSource;

public final class DataSourceProvider extends PluginModule {

    private HikariDataSource dataSource;

    private void createDatasource() {
        var plugin = ChatSyncPlugin.getInstance();
        var config = plugin.getConfig();
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(config.getString("database.url"));
        cfg.setUsername(config.getString("database.user"));
        cfg.setPassword(config.getString("database.password"));
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(10_000);
        cfg.setLeakDetectionThreshold(15_000);
        cfg.setPoolName("ChatSyncPool");
        this.dataSource = new HikariDataSource(cfg);
    }

    @Provide
    public DataSource dataSource() {
        if (dataSource == null) {
            createDatasource();
        }
        return dataSource;
    }

    public void close() {
        dataSource.close();
    }
}
