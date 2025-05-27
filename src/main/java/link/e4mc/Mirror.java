package link.e4mc;

import java.util.function.UnaryOperator;

import net.minecraft.server.command.source.CommandSource;
import net.minecraft.text.*;

public class Mirror {

	public static ClickEvent runCommand(String command) {
		return new ClickEvent(net.minecraft.text.ClickEvent.Action.RUN_COMMAND, command);
	}

	public static ClickEvent copyToClipboard(String text) {
		return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, text);
	}

	public static HoverEvent showText(Text text) {
		return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
	}

	public static Text withStyle(Text component, UnaryOperator<Style> operator) {
		return component.setStyle(operator.apply(component.getStyle()));
	}

	public static Text append(Text component, Text other) {
		return component.append(other);
	}

	public static Text literal(String text) {
		return new LiteralText(text);
	}


	public static Text translatable(String text, Object... args) {
		return new TranslatableText(text, args);
	}

	public static void sendSuccessToSource(CommandSource source, Text message) {
		source.sendMessage(message);
	}

	public static void sendFailureToSource(CommandSource source, Text message) {
		source.sendMessage(message);
	}
}
