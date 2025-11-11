package ru.hackaton.chatsync.command;

import com.hakan.basicdi.annotations.Autowired;
import com.hakan.basicdi.annotations.Component;
import com.hakan.basicdi.annotations.PostConstruct;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.hackaton.chatsync.MinecraftMessagingService;
import ru.hackaton.chatsync.target.MessageTarget;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Для понимания этого класса прочитать вот эту <a href="https://docs.papermc.io/paper/dev/command-api/basics/introduction/">документацию</a>
 * */
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("UnstableApiUsage")
public class MsgCommand {

    private static final SimpleCommandExceptionType ERROR_NOT_INITIALIZED = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(net.kyori.adventure.text.Component.text("Plugin not initialized exception!"))
    );
    private static final String COMMAND = "msg";
    private static final Collection<String> ALIASES = List.of("w", "tell", "m");
    private static final String PERMISSION = "hackaton.chatsync.msg";
    private static final String USER_NAME_ARGUMENT = "user";
    private static final String TEXT_ARGUMENT = "text";

    private static MsgCommand INSTANCE;

    private final JavaPlugin plugin;
    private final MinecraftMessagingService minecraftMessagingService;

    @PostConstruct
    public void init() {
        INSTANCE = this;
        MessageTargetArgumentType.INSTANCE.minecraftMessagingService = minecraftMessagingService;
        plugin.getLifecycleManager().registerEventHandler(
                LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(createCommand(), ALIASES)
        );
    }

    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal(COMMAND)
                .requires(ctx -> ctx.getSender().hasPermission(PERMISSION))
                .then(targetPlayerNode())
                .build();
    }

    private static ArgumentBuilder<CommandSourceStack, ?> targetPlayerNode() {
        return Commands.argument(USER_NAME_ARGUMENT, MessageTargetArgumentType.INSTANCE)
                .then(messageTextNode());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> messageTextNode() {
        return Commands.argument(TEXT_ARGUMENT, StringArgumentType.greedyString())
                .executes(MsgCommand::executeMsg);
    }

    private static int executeMsg(CommandContext<CommandSourceStack> command) throws CommandSyntaxException {
        if (INSTANCE == null)
            throw ERROR_NOT_INITIALIZED.create();
        var sender = command.getSource().getSender();
        @SuppressWarnings("unchecked")
        var targetFuture = (CompletableFuture<MessageTarget>) command.getArgument(USER_NAME_ARGUMENT, CompletableFuture.class);
        var message = command.getArgument(TEXT_ARGUMENT, String.class);
        targetFuture.thenAccept((target) -> target.sendMessage(sender, message));
        return Command.SINGLE_SUCCESS;
    }


}
