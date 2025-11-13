import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import ru.hackaton.chatsync.core.db.MinecraftUserRepository;
import ru.hackaton.chatsync.core.db.UserLinkRepository;
import ru.hackaton.chatsync.core.service.UserLinkingService;

import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserLinkingServiceIT extends IntegrationTest {

    private Logger logger;
    private UserLinkRepository userLinkRepository;
    private UserLinkingService userLinkingService;
    private MinecraftUserRepository minecraftUserRepository;

    @BeforeAll
    void setupRepo() {
        userLinkRepository = new UserLinkRepository(dataSource);
        userLinkingService = new UserLinkingService(logger, userLinkRepository, minecraftUserRepository, Duration.ofSeconds(5));
    }

    @Test
    void initiateAndConfirmLink_shouldCreateRecord() throws Exception {
        String code = userLinkingService.initiateLink(2, "telegram");
        Assertions.assertNotNull(code);

        boolean result = userLinkingService.confirmLink(code, "55555");
        Assertions.assertTrue(result);
        Assertions.assertTrue(userLinkRepository.exists(2, "telegram"));
    }

    @Test
    void confirmLink_shouldFailIfCodeInvalid() throws Exception {
        boolean result = userLinkingService.confirmLink("999999", "77777");
        Assertions.assertFalse(result);
    }

    @Test
    void confirmLink_shouldFailIfCodeExpired() throws Exception {
        var shortTtlService = new UserLinkingService(logger, userLinkRepository, minecraftUserRepository, Duration.ofSeconds(1));
        String code = shortTtlService.initiateLink(1, "telegram");

        Thread.sleep(1200);
        boolean result = shortTtlService.confirmLink(code, "99999");
        Assertions.assertFalse(result, "код должен быть просрочен");
    }

    @Test
    void duplicateLink_shouldBeIgnored() throws Exception {
        String code = userLinkingService.initiateLink(1, "telegram");
        boolean result = userLinkingService.confirmLink(code, "12345");
        Assertions.assertTrue(result);
    }

    @Test
    void multipleSimultaneousCodes_shouldWorkIndependently() throws Exception {
        String codeA = userLinkingService.initiateLink(1, "discord");
        String codeB = userLinkingService.initiateLink(2, "discord");

        Assertions.assertNotEquals(codeA, codeB);
        Assertions.assertTrue(userLinkingService.confirmLink(codeA, "A123"));
        Assertions.assertTrue(userLinkingService.confirmLink(codeB, "B123"));
    }

    @Test
    void confirmLink_shouldBeSingleUse() throws Exception {
        String code = userLinkingService.initiateLink(3, "telegram");

        Assertions.assertTrue(
                userLinkingService.confirmLink(code, "99999"),
                "первое подтверждение кода должно быть успешным"
        );

        Assertions.assertFalse(
                userLinkingService.confirmLink(code, "another"),
                "повторное использование того же кода должно быть невозможным"
        );
    }

}
