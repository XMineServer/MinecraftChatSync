package ru.hackaton.chatsync.tg;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.PostConstruct;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import ru.hackaton.chatsync.command.MessageTargetArgumentType;
import ru.hackaton.chatsync.target.ExternalMessageTargetProvider;
import ru.hackaton.chatsync.target.MessageTarget;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@com.hakan.basicdi.annotations.Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("UnstableApiUsage")
public class TelegramMessageTargetProvider implements ExternalMessageTargetProvider {

    private static final SimpleCommandExceptionType ERROR_WRONG_USER = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Wrong telegram user!"))
    );

    private static final Collection<String> nicknames = List.of("sidey383", "slampy");

    private final BotService botService;

    @PostConstruct
    public void init() {
        MessageTargetArgumentType.INSTANCE.addMessageTargetProvider(this);
    }

    @Pattern("[a-z0-9_]+")
    @Override
    public @NotNull String getNamespace() {
        return "tg";
    }

    @Override
    public @NotNull CompletableFuture<MessageTarget> parse(String argument, CommandSourceStack stack){
        return CompletableFuture.supplyAsync(() -> {
            String nickname = fromArgument(argument);
            if (nicknames.contains(nickname)) {
                return botService.createPrivateMessageTarget(nickname);
            } else {
                throw new CompletionException(ERROR_WRONG_USER.create());
            }
        });
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            nicknames.stream()
                    .map(this::toArgument)
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
}
