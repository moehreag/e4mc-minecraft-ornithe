package link.e4mc.fabric;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandRegistry {

	public static CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
	private static final Logger LOGGER = LogManager.getLogger("ClientCommandHandler");

	public static LiteralArgumentBuilder<CommandSource> literal(String arg) {
		return LiteralArgumentBuilder.literal(arg);
	}

	public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String arg, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(arg, type);
	}

	private static boolean isIgnoredException(CommandExceptionType type) {
		return type == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand() ||
				type == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException();
	}

	private static Text getErrorMessage(CommandSyntaxException e) {
		Text message = new LiteralText(e.getMessage());
		String context = e.getContext();
		return context != null ?
				new TranslatableText("command.parse_error", message, e.getCursor(), context) : message;
	}

	public static boolean dispatch(String command, CommandSource source) {
		if (!command.startsWith("/")) {
			return false;
		}

		// cancel if present
		command = command.trim().substring(1);

		try {
			DISPATCHER.execute(command, source);
			return true;
		} catch (CommandSyntaxException e) {
			if (isIgnoredException(e.getType())) {
				return false;
			}

			LOGGER.warn("Syntax exception for command '{}'", command, e);
			source.sendMessage(getErrorMessage(e));
			return true;
		} catch (Exception e) {
			LOGGER.warn("Error while executing command '{}'", command, e);
			source.sendMessage(new LiteralText(e.getMessage() == null ? "" : e.getMessage()));
			return true;
		}
	}

	public static CompletableFuture<List<String>> getSuggestions(String command, CommandSource source) {
		String command0 = command.startsWith("/") ? command.substring(1) : command;
		return DISPATCHER.getCompletionSuggestions(DISPATCHER.parse(command0, source))
				.thenApply(suggestions -> suggestions.getList()
						.stream()
						.map(x -> command0.contains(" ") ? x.getText() : "/" + x.getText())
						.toList()
				);
	}
}
