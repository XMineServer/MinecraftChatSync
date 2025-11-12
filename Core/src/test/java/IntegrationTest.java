import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

@Testcontainers
public abstract class IntegrationTest {

    @Container
    protected static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("chatsync")
            .withUsername("user")
            .withPassword("password");

    protected static DataSource dataSource;

    @BeforeAll
    static void initDataSource() throws Exception {
        var cfg = new HikariConfig();
        cfg.setJdbcUrl(MYSQL.getJdbcUrl());
        cfg.setUsername(MYSQL.getUsername());
        cfg.setPassword(MYSQL.getPassword());
        dataSource = new HikariDataSource(cfg);

        try (Connection conn = dataSource.getConnection()) {
            executeSql(conn, "/db.sql");
            executeSql(conn, "/data.sql");
        }
    }

    @BeforeEach
    void resetData() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            executeSql(conn, "/data.sql");
        }
    }

    protected static void executeSql(Connection conn, String path) throws Exception {
        try (var reader = new InputStreamReader(
                IntegrationTest.class.getResourceAsStream(path),
                StandardCharsets.UTF_8)) {

            var sqlParts = new Scanner(reader).useDelimiter(";").tokens().toList();
            for (var stmt : sqlParts) {
                if (!stmt.isBlank()) try (Statement s = conn.createStatement()) { s.execute(stmt); }
            }
        }
    }
}
