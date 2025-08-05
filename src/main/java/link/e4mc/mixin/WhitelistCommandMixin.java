package link.e4mc.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import link.e4mc.E4mcClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.AbstractCommand;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractCommand.class)
public class WhitelistCommandMixin {
	@WrapMethod(method = "canUse")
	private boolean allowOwner(CommandSource source, Operation<Boolean> original) {
		var self = (AbstractCommand) (Object) this;
		if (self instanceof WhitelistCommand) {
			if (MinecraftServer.getInstance().asEntity() instanceof ServerPlayerEntity player && E4mcClient.isSingleplayerOwner(player.getGameProfile())) {
				return true;
			}
		}
		return original.call(source);
	}
}
