import org.junit.jupiter.api.*;
import ru.hackaton.chatsync.core.db.LinkedUser;
import ru.hackaton.chatsync.core.db.UserLink;
import ru.hackaton.chatsync.core.db.UserLinkRepository;

import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserLinkRepositoryIT extends IntegrationTest {

    private UserLinkRepository repo;

    @BeforeAll
    void setupRepo() {
        repo = new UserLinkRepository(dataSource);
    }

    @Test
    void findByUserAndPlatform_shouldReturnExistingLinkFromSeedData() throws Exception {
        Optional<UserLink> opt = repo.findByUser(1, "telegram");

        Assertions.assertTrue(opt.isPresent(), "линк для user_id=1 и telegram должен существовать (data.sql)");
        UserLink link = opt.get();
        Assertions.assertEquals(1L, link.getUserId());
        Assertions.assertEquals("telegram", link.getPlatform());
        Assertions.assertEquals("12345", link.getExternalId());
    }

    @Test
    void findByUserAndPlatform_shouldReturnEmptyForUnknownPlatform() throws Exception {
        Optional<UserLink> opt = repo.findByUser(1, "discord");
        Assertions.assertTrue(opt.isEmpty());
    }

    @Test
    void findByUsernameStartWith_shouldReturnLinkedUsersForPrefixAndPlatform() throws Exception {
        List<LinkedUser> users = repo.findByUsernameStartWith("a", "telegram");

        Assertions.assertFalse(users.isEmpty(), "по префиксу 'a' и платформе 'telegram' должна вернуться alice");
        LinkedUser u = users.get(0);
        Assertions.assertEquals("alice", u.getUsername());
        Assertions.assertEquals("12345", u.getExternalId());
    }

    @Test
    void findByUsernameStartWith_shouldRespectPlatformFilter() throws Exception {
        List<LinkedUser> users = repo.findByUsernameStartWith("a", "discord");
        Assertions.assertTrue(users.isEmpty(), "у alice нет линка для discord, список должен быть пустым");
    }

    @Test
    void findByUsernameStartWith_shouldRespectPrefixFilter() throws Exception {
        List<LinkedUser> users = repo.findByUsernameStartWith("b", "telegram");
        Assertions.assertTrue(users.isEmpty(), "у bob нет линка ни к одной платформе, список должен быть пустым");
    }

    @Test
    void exists_shouldReflectLinkPresence() throws Exception {
        Assertions.assertTrue(repo.exists(1, "telegram"));
        Assertions.assertFalse(repo.exists(2, "telegram"));
        Assertions.assertFalse(repo.exists(1, "discord"));
    }

    @Test
    void findPlayerIdByExternal_shouldReturnUserId() throws Exception {
        Optional<Long> userId = repo.findPlayerIdByExternal("telegram", "12345");
        Assertions.assertTrue(userId.isPresent());
        Assertions.assertEquals(1, userId.get());
    }

    @Test
    void findPlayerIdByExternal_shouldReturnEmptyForUnknown() throws Exception {
        Optional<Long> userId = repo.findPlayerIdByExternal("telegram", "99999");
        Assertions.assertTrue(userId.isEmpty());
    }

    @Test
    void link_shouldBeIdempotentForSameTriple() throws Exception {
        repo.link(1L, "telegram", "12345");
        repo.link(1L, "telegram", "12345");

        List<UserLink> links = repo.findByUser(1);
        long count = links.stream()
                .filter(l -> l.getPlatform().equals("telegram") && l.getExternalId().equals("12345"))
                .count();

        Assertions.assertEquals(1, count, "INSERT IGNORE должен гарантировать отсутствие дублей");
    }

    @Test
    void findByUser_shouldReturnAllLinksForUser() throws Exception {
        repo.link(1L, "discord", "A1");

        List<UserLink> links = repo.findByUser(1);
        Assertions.assertTrue(links.size() >= 2);

        boolean hasTelegram = links.stream().anyMatch(l -> l.getPlatform().equals("telegram"));
        boolean hasDiscord  = links.stream().anyMatch(l -> l.getPlatform().equals("discord"));

        Assertions.assertTrue(hasTelegram);
        Assertions.assertTrue(hasDiscord);
    }
}
