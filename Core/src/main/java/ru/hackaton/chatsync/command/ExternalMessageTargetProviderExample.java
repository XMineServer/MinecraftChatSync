package ru.hackaton.chatsync.command;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.PostConstruct;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import ru.hackaton.chatsync.MinecraftMessagingService;
import ru.hackaton.chatsync.target.ExternalMessageTargetProvider;
import ru.hackaton.chatsync.target.MessageTarget;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Пример реализации {@link ExternalMessageTargetProvider} для внешнего сервиса
 * */
//@com.hakan.basicdi.annotations.Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("UnstableApiUsage")
public class ExternalMessageTargetProviderExample implements ExternalMessageTargetProvider {
    private static final SimpleCommandExceptionType ERROR_WRONG_USER = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("Wrong special user!"))
    );

    private final MinecraftMessagingService minecraftMessagingService;

    @PostConstruct
    public void register() {
        // Нужно зарегистрировать себя в обработчике аргумента
        MessageTargetArgumentType.INSTANCE.addMessageTargetProvider(this);
    }

    @Override
    @Pattern("[a-z0-9_]+")
    public @NotNull String getNamespace() {
        return "special";
    }

    @Override
    public @NotNull CompletableFuture<MessageTarget> parse(String argument, CommandSourceStack stack) throws CommandSyntaxException {
        // Необходимо вызвать fromArgument, чтобы получить корректный идентификатор
        if (Objects.equals(fromArgument(argument), "Console")) {
            return CompletableFuture.supplyAsync(() -> {
                //Какая-то затратная операция (запрос в бд или API)
                // Синхронно такие операции тут делать нельзя!
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // Отправка сообщения. Тут скорее всего будет какой-то ваш класс, а не лямбда
                return (sender, message) -> minecraftMessagingService.sendMsg(sender, Bukkit.getConsoleSender(), message);
            });
        }
        // Если аргумент некорректный - кидаем CommandSyntaxException
        throw ERROR_WRONG_USER.create();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        return CompletableFuture.supplyAsync(() -> {
            // Какая-то затратная операция (запрос в бд или API)
            // Синхронно такие операции тут делать нельзя!
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // Необходимо обернуть в toArgument, чтобы вернуть корректный результат
            builder.suggest(toArgument("Console"));
            builder.suggest(toArgument("With@Special Symbols"));
            // Магия из API, SuggestionsBuilder оборачивает наши значения
            return builder.build();
        });
    }
}
