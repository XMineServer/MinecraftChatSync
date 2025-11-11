package ru.hackaton.chatsync.command;

import com.google.common.collect.Streams;
import com.hakan.basicdi.annotations.Autowired;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hackaton.chatsync.MinecraftMessagingService;
import ru.hackaton.chatsync.target.ExternalMessageTargetProvider;
import ru.hackaton.chatsync.target.MessageTarget;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("UnstableApiUsage")
public class MessageTargetArgumentType implements CustomArgumentType<CompletableFuture<MessageTarget>, String> {

    private static final SimpleCommandExceptionType ERROR_PLAYER_NOT_FOUND = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Player not found!"))
    );
    private static final SimpleCommandExceptionType ERROR_BAD_SOURCE = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("The source needs to be a CommandSourceStack!"))
    );
    private static final SimpleCommandExceptionType ERROR_WRONG_PROVIDER = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Wrong user provider!"))
    );
    private static final SimpleCommandExceptionType ERROR_NOT_INITIALIZED = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Plugin not initialized exception!"))
    );
    private static final Pattern EXTERNAL_USER_PATTERN = Pattern.compile("^([a-z0-9_]+)\\..*$");

    public static final MessageTargetArgumentType INSTANCE = new MessageTargetArgumentType();

    MinecraftMessagingService minecraftMessagingService;
    private final Map<String, ExternalMessageTargetProvider> messageTargetProviderMap = new ConcurrentHashMap<>();

    public void addMessageTargetProvider(ExternalMessageTargetProvider provider) {
        this.messageTargetProviderMap.put(provider.getNamespace(), provider);
    }

    @NotNull
    @Override
    public CompletableFuture<MessageTarget> parse(@NotNull StringReader reader) {
        throw new UnsupportedOperationException("This method will never be called.");
    }

    @NotNull
    @Override
    public <S> CompletableFuture<MessageTarget> parse(@NotNull StringReader reader, @NotNull S source) throws CommandSyntaxException {
        if (!(source instanceof CommandSourceStack stack)) {
            throw ERROR_BAD_SOURCE.create();
        }
        String value = reader.readUnquotedString();
        var matcher = EXTERNAL_USER_PATTERN.matcher(value);
        if (matcher.find()) {
            var type = matcher.group(1);
            var provider = messageTargetProviderMap.get(type);
            if (provider == null) {
                throw ERROR_WRONG_PROVIDER.create();
            }
            return provider.parse(value, stack);
        } else {
            Player player = Bukkit.getPlayer(value);
            if (player == null) {
                throw ERROR_PLAYER_NOT_FOUND.create();
            }
            if (minecraftMessagingService == null) {
                throw ERROR_NOT_INITIALIZED.create();
            }
            return CompletableFuture.completedFuture(new PlayerMessageTarget(minecraftMessagingService, player));
        }
    }

    @NotNull
    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }

    @NotNull
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, SuggestionsBuilder builder) {
        var input = builder.getRemaining();
        var matcher = EXTERNAL_USER_PATTERN.matcher(input);
        if (matcher.find()) {
            var type = matcher.group(1);
            var provider = messageTargetProviderMap.get(type);
            if (provider == null) {
                return builder.buildFuture();
            }
            return provider.listSuggestions(context, builder);
        } else {
            Streams.concat(
                    messageTargetProviderMap.keySet().stream()
                            .filter(namespace -> namespace.startsWith(input))
                            .map(namespace -> namespace + ExternalMessageTargetProvider.NAMESPACE_SEPARATOR),
                    Bukkit.getOnlinePlayers().stream().map(Player::getName)
            ).forEach(builder::suggest);
            return builder.buildFuture();
        }
    }
}
