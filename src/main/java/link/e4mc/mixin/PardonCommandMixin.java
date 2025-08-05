package link.e4mc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import link.e4mc.E4mcClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.dedicated.command.PardonCommand;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PardonCommand.class)
public class PardonCommandMixin {
	@WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/AbstractCommand;canUse(Lnet/minecraft/server/command/source/CommandSource;)Z"))
	private boolean allowOwner(PardonCommand instance, CommandSource source, Operation<Boolean> original) {
		if (MinecraftServer.getInstance().asEntity() instanceof ServerPlayerEntity player && E4mcClient.isSingleplayerOwner(player.getGameProfile())) {
			return true;
		}
		return original.call(instance, source);
	}
}
