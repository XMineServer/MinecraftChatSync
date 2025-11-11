package ru.hackaton.chatsync.ds;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.Getter;
import reactor.core.publisher.Mono;

public class BotService {

    @Getter
    private static BotService instance;

    private DiscordClient client;
//    private ChatSyncDiscordBot bot;
    private static GatewayDiscordClient gateway;
    // TODO replace guild usage with global one
    private static long guildId;

    public BotService(String token, String username, long gid) {
        instance = this;
        client = DiscordClient.create(token);
        guildId = gid;
    }

    public void start() throws Exception {
        gateway = client.login().block();

        // Register slash commands
        long applicationId = gateway.getRestClient().getApplicationId().block();

        // Create command say
        ApplicationCommandRequest sayCommand = ApplicationCommandRequest.builder()
                .name("say")
                .description("Say something in the minecraft server!")
                .build();

        // Register the command
        gateway.getRestClient().getApplicationService()
                .createGuildApplicationCommand(applicationId, guildId, sayCommand)
                .subscribe();

        // Handle slash command interactions
        gateway.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("say")) {
                // TODO send the message to the minecraft server

            }
            return Mono.empty();
        }).subscribe();

        System.out.println("[ChatSyncDS] Discord bot started.");
        gateway.onDisconnect().block();
    }

    public void stop() {
        System.out.println("[ChatSyncDS] Discord bot stopped.");
    }

    public void sendToDiscord(String message) {
        bot.sendMessageToChannel(message);
    }
}
