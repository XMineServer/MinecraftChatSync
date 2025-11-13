import org.junit.jupiter.api.*;
import ru.hackaton.chatsync.core.db.MinecraftUser;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.UUID;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinecraftUserRepositoryIT extends IntegrationTest {

    private MinecraftUserRepository repo;

    @BeforeAll
    void setupRepo() {
        repo = new MinecraftUserRepository(dataSource);
    }

    @Test
    void insertAndFindByUuidAndUsername_shouldWork() throws Exception {
        UUID uuid = UUID.randomUUID();

        // вставляем нового пользователя
        repo.insertMinecraftUser(uuid, "charlie");

        // ищем по uuid
        Optional<MinecraftUser> byUuid = repo.findMinecraftUser(uuid);
        Assertions.assertTrue(byUuid.isPresent(), "пользователь должен находиться по uuid");
        Assertions.assertEquals(uuid, byUuid.get().getUuid());
        Assertions.assertEquals("charlie", byUuid.get().getUsername());

        // ищем по нику
        Optional<MinecraftUser> byName = repo.findMinecraftUser("charlie");
        Assertions.assertTrue(byName.isPresent(), "пользователь должен находиться по нику");
        Assertions.assertEquals(uuid, byName.get().getUuid());
    }

    @Test
    void insertDuplicateUuid_shouldFailWithConstraintViolation() throws Exception {
        UUID uuid = UUID.randomUUID();

        repo.insertMinecraftUser(uuid, "charlie");
        // вторая вставка с тем же uuid должна упасть
        Assertions.assertThrows(SQLIntegrityConstraintViolationException.class, () -> {
            repo.insertMinecraftUser(uuid, "charlie2");
        });
    }

    @Test
    void findByUuid_shouldReturnEmptyForUnknownUser() throws Exception {
        Optional<MinecraftUser> notFound = repo.findMinecraftUser(UUID.randomUUID());
        Assertions.assertTrue(notFound.isEmpty(), "для несуществующего uuid должен возвращаться Optional.empty()");
    }

    @Test
    void findByUsername_shouldReturnExistingUserFromSeedData() throws Exception {
        Optional<MinecraftUser> alice = repo.findMinecraftUser("alice");
        Assertions.assertTrue(alice.isPresent(), "alice должна быть в тестовых данных");
        Assertions.assertEquals("alice", alice.get().getUsername());
    }
}
