import org.junit.jupiter.api.*;
import ru.hackaton.chatsync.core.db.MinecraftUser;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;

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
    void upsertAndFindByUuidAndUsername_shouldWork() throws Exception {
        UUID uuid = UUID.randomUUID();

        // создаём пользователя
        repo.upsertMinecraftUser(uuid, "charlie");

        Optional<MinecraftUser> byUuid = repo.findMinecraftUser(uuid);
        Assertions.assertTrue(byUuid.isPresent(), "пользователь должен находиться по uuid");
        Assertions.assertEquals(uuid, byUuid.get().getUuid());
        Assertions.assertEquals("charlie", byUuid.get().getUsername());

        // апдейтим ник для того же uuid
        repo.upsertMinecraftUser(uuid, "charlie2");

        Optional<MinecraftUser> updatedByUuid = repo.findMinecraftUser(uuid);
        Assertions.assertTrue(updatedByUuid.isPresent(), "после апдейта пользователь всё ещё должен находиться");
        Assertions.assertEquals("charlie2", updatedByUuid.get().getUsername());

        // поиск по нику
        Optional<MinecraftUser> byName = repo.findMinecraftUser("charlie2");
        Assertions.assertTrue(byName.isPresent(), "пользователь должен находиться по обновлённому нику");
        Assertions.assertEquals(uuid, byName.get().getUuid());
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
