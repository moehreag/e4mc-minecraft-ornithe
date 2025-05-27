package link.e4mc.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import link.e4mc.fabric.CommandRegistry;
import net.minecraft.network.packet.c2s.play.CommandSuggestionsC2SPacket;
import net.minecraft.server.command.handler.CommandHandler;
import net.minecraft.server.command.source.CommandSource;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "handleCommandSuggestions", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCommandSuggestions(Lnet/minecraft/server/command/source/CommandSource;Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;)Ljava/util/List;"))
	private void addBrigadierSuggestions(CommandSuggestionsC2SPacket packet, CallbackInfo ci, @Local List<String> suggestions) {
		suggestions.addAll(CommandRegistry.getSuggestions(packet.getCommand(), this.player).join());
	}

	@WrapOperation(method = "runCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/handler/CommandHandler;run(Lnet/minecraft/server/command/source/CommandSource;Ljava/lang/String;)I"))
	private int runBrigadierCommand(CommandHandler instance, CommandSource source, String command, Operation<Integer> original) {
		if (!CommandRegistry.dispatch(command, source)) {
			original.call(instance, source, command);
		}
		return 1;
	}
}
