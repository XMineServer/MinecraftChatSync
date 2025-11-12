import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MySQLInitScriptTest {

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("chatsync")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("db.sql");

    @Test
    void testContainerIsReady() throws Exception {
        System.out.println("Database URL: " + mysql.getJdbcUrl());
        try (var conn = java.sql.DriverManager.getConnection(
                mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
             var rs = conn.createStatement().executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
        }
    }
}
