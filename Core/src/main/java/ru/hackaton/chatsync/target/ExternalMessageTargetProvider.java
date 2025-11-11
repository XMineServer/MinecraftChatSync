package ru.hackaton.chatsync.target;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public interface ExternalMessageTargetProvider {

    Character NAMESPACE_SEPARATOR = '.';

    /**
     *  Некоторая константа, определяющая этот тип целей сообщения
     * */
    @Pattern("[a-z0-9_]+")
    @NotNull
    String getNamespace();

    @NotNull
    CompletableFuture<MessageTarget> parse(String argument, CommandSourceStack stack) throws CommandSyntaxException;

    @NotNull
    <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder);

    @NotNull
    default String toArgument(String value) {
        var argument = getNamespace() + NAMESPACE_SEPARATOR + value;
        if (new StringReader(argument).readUnquotedString().length() != argument.length()) {
            return '"' + argument + '"';
        }
        return argument;

    }

    @NotNull
    default String fromArgument(String value) {
        int i = value.indexOf(NAMESPACE_SEPARATOR);
        if (i < 0) return "";
        return value.substring(i + 1);
    }

}
