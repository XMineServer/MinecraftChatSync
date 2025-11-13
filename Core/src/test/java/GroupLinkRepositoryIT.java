import org.junit.jupiter.api.*;
import ru.hackaton.chatsync.core.db.GroupLinkRepository;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupLinkRepositoryIT extends IntegrationTest {

    private GroupLinkRepository repo;

    @BeforeAll
    void setupRepo() {
        repo = new GroupLinkRepository(dataSource);
    }

    @Test
    void linkAndUnlinkWorks() throws Exception {
        repo.link("telegram", List.of("group", "dev"));
        Assertions.assertTrue(repo.exists("telegram", List.of("group", "dev")));

        repo.unlink("telegram", List.of("group", "dev"));
        Assertions.assertFalse(repo.exists("telegram", List.of("group", "dev")));
    }

    @Test
    void duplicateInsertIgnored() throws Exception {
        repo.link("discord", List.of("team"));
        repo.link("discord", List.of("team"));
        Assertions.assertTrue(repo.exists("discord", List.of("team")));
    }

    @Test
    void findByPlatformFiltersCorrectly() throws Exception {
        var tg = repo.findByPlatform("telegram");
        Assertions.assertTrue(tg.stream().allMatch(l -> l.getPlatform().equals("telegram")));
    }

    @Test
    void exists_shouldReturnFalseForMissingLink() throws Exception {
        Assertions.assertFalse(
                repo.exists("telegram", List.of("nonexistent", "path")),
                "для несуществующего пути exists должен вернуть false"
        );
    }

    @Test
    void unlink_shouldRemoveLink() throws Exception {
        repo.link("discord", List.of("temp"));
        Assertions.assertTrue(repo.exists("discord", List.of("temp")));

        repo.unlink("discord", List.of("temp"));
        Assertions.assertFalse(repo.exists("discord", List.of("temp")));
    }
}
