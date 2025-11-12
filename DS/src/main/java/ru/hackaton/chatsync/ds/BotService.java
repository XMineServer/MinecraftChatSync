package ru.hackaton.chatsync.ds;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Component;
import com.hakan.basicdi.annotations.PostConstruct;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class BotService {

    private final JavaPlugin plugin;
    private final Logger logger;
    private DiscordClient client;
    private static GatewayDiscordClient gateway;
    // TODO replace guild usage with global one

    private Disposable webhookDisposable;
    private Disposable sayCommandDisposable;

    private long channelId;

    @Autowired
    public BotService(JavaPlugin plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @PostConstruct
    public void start() {
        plugin.saveDefaultConfig();
        var config = plugin.getConfig();
        String token = config.getString("discord.token");
        assert token != null;
        // TODO replace with global option
        long guildId = config.getLong("discord.guildId");
        channelId = config.getLong("discord.channelId");

        client = DiscordClient.create(token);

//        gateway = client.login().block(Duration.ofSeconds(10));
        gateway = client.login().block();

        // Register slash commands
        long applicationId = gateway.getRestClient().getApplicationId().block();

        // Create command say
        ApplicationCommandRequest sayCommand = ApplicationCommandRequest.builder()
                .name("say")
                .description("Say something in the minecraft server!")
                .build();

        // Register the command
        sayCommandDisposable = gateway.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, sayCommand)
                .subscribe();

        // Handle slash command interactions
        webhookDisposable = gateway.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("say")) {
                // TODO send the message to the minecraft server

            }
            return Mono.empty();
        }).subscribe();

        gateway.onDisconnect().block();
        logger.info("Discord bot started.");
    }

    public void stop() {
        if (webhookDisposable != null) {
            webhookDisposable.dispose();
        }
        if (sayCommandDisposable != null) {
            sayCommandDisposable.dispose();
        }
        logger.info("Discord bot stopped.");
    }

}
