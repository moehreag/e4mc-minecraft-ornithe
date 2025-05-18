package link.e4mc;

import com.mojang.brigadier.CommandDispatcher;
import io.github.axolotlclient.commands.ClientCommandInfo;
import io.github.axolotlclient.commands.ClientCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E4mcClient {
	public static final String MOD_ID = "e4mc_minecraft";
	public static QuiclimeSession session;
	private static final Logger LOGGER = LoggerFactory.getLogger(E4mcClient.MOD_ID);

	public static void init() {
//        if (System.getProperty("os.name").startsWith("Windows")) {
//            var path = Agnos.jarPath();
//            var motwPath = path + ":Zone.Identifier";
//            try(FileInputStream inputStream = new FileInputStream(motwPath)) {
//                String hidden = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
//                LOGGER.warn(hidden);
//            } catch (IOException ignored) {}
//        }
	}

	public static void registerCommands(CommandDispatcher<ClientCommandInfo> dispatcher) {
		dispatcher.register(
				ClientCommands.literal("e4mc")
						.requires(src -> {
							if (src.getMinecraft().getServer().isDedicated()) {
								return src.getMinecraft().player.canUseCommand(4, "e4mc");
							} else {
								return src.getMinecraft().getServer().getUsername().equals(src.getMinecraft().player.getGameProfile().getName());
							}
						})
						.then(ClientCommands.literal("stop").executes(ctx -> {
							if ((session != null) && (session.state != QuiclimeSession.State.STOPPED)) {
								session.stop();
								Mirror.sendSuccessToSource(ctx.getSource(), Mirror.translatable("text.e4mc_minecraft.closeServer"));
							} else {
								Mirror.sendFailureToSource(ctx.getSource(), Mirror.translatable("text.e4mc_minecraft.serverAlreadyClosed"));
							}
							return 1;
						}))
						.then(ClientCommands.literal("restart").executes(ctx -> {
							if ((session != null) && (session.state != QuiclimeSession.State.STARTED)) {
								session.stop();
								session = new QuiclimeSession();
								session.startAsync();
							}
							return 1;
						}))
		);
	}
}
