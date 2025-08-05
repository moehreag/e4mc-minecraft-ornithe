package link.e4mc.mixin;

import java.io.File;
import java.net.Proxy;

import link.e4mc.Agnos;
import link.e4mc.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.handler.CommandManager;
import net.minecraft.server.dedicated.command.BanCommand;
import net.minecraft.server.dedicated.command.BanListCommand;
import net.minecraft.server.dedicated.command.PardonCommand;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

	@Inject(method = "<init>(Ljava/io/File;Ljava/net/Proxy;Ljava/io/File;)V", at = @At("TAIL"))
	private void initCommands(File gameDir, Proxy proxy, File userCacheFile, CallbackInfo ci) {
		if (Config.INSTANCE.restoreDedicatedCommands.get() && Agnos.isClient()) {
			var manager = (CommandManager) MinecraftServer.getInstance().getCommandHandler();
			manager.register(new BanListCommand());
			manager.register(new BanCommand());
			manager.register(new PardonCommand());
			manager.register(new WhitelistCommand());
		}
	}

	@Inject(method = "setPlayerManager", at = @At("TAIL"))
	private void initCommands(PlayerManager playerManager, CallbackInfo ci) {
		if (Config.INSTANCE.restoreDedicatedCommands.get() && Agnos.isClient()) {
			playerManager.getBans().setEnabled(true);
		}
	}

}
