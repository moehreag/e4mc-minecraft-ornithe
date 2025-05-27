package link.e4mc;

import com.mojang.brigadier.CommandDispatcher;
import link.e4mc.fabric.CommandRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.source.CommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class E4mcClient {
	public static final String MOD_ID = "e4mc_minecraft";
	public static QuiclimeSession session;
	private static final Logger LOGGER = LoggerFactory.getLogger(E4mcClient.MOD_ID);

	public static void init() {
		Config.register();
//        if (System.getProperty("os.name").startsWith("Windows")) {
//            var path = Agnos.jarPath();
//            var motwPath = path + ":Zone.Identifier";
//            try(FileInputStream inputStream = new FileInputStream(motwPath)) {
//                String hidden = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
//                LOGGER.warn(hidden);
//            } catch (IOException ignored) {}
//        }
	}

	public static void registerCommands(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				CommandRegistry.literal("e4mc")
						.requires(src -> {
							if (MinecraftServer.getInstance().isDedicated()) {
								return src.canUseCommand(4, "e4mc");
							} else {
								return src.getName().equals(Minecraft.getInstance().getSession().getUsername());
							}
						})
						.then(CommandRegistry.literal("stop").executes(ctx -> {
							if ((session != null) && (session.state != QuiclimeSession.State.STOPPED)) {
								session.stop();
								Mirror.sendSuccessToSource(ctx.getSource(), Mirror.translatable("text.e4mc_minecraft.closeServer"));
							} else {
								Mirror.sendFailureToSource(ctx.getSource(), Mirror.translatable("text.e4mc_minecraft.serverAlreadyClosed"));
							}
							return 1;
						}))
						.then(CommandRegistry.literal("restart").executes(ctx -> {
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
