package link.e4mc.mixin;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import link.e4mc.Config;
import link.e4mc.E4mcClient;
import net.minecraft.server.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerManager.class)
public abstract class PlayerListMixin {

	@Shadow
	public abstract void setEnforceWhitelist(boolean enforce);

	@Shadow
	public abstract Whitelist getWhitelist();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void enableOptions(MinecraftServer server, CallbackInfo ci) {
		if (Config.INSTANCE.restoreDedicatedCommands.get()) {
			setEnforceWhitelist(Config.INSTANCE.useWhiteList.get());

			System.out.println(this.getWhitelist());
			System.out.println(Arrays.toString(this.getWhitelist().getClass().getMethods()));
			loadWhitelist(getWhitelist());

			if (!((StoredUserListAccessor<?, ?>)getWhitelist()).accessFile().exists()) {
				try {
					getWhitelist().save();
				} catch (IOException e) {
					E4mcClient.LOGGER.warn("Failed to save whitelist: ", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T, A extends StoredUserEntry<T>> void loadWhitelist(StoredUserList<T, A> whitelist) {
		Collection<A> collection;

		try (var bufferedReader = Files.newBufferedReader(((StoredUserListAccessor<T, A>)whitelist).accessFile().toPath(), Charsets.UTF_8)) {
			collection = ((StoredUserListAccessor<T, A>) whitelist).getGson().fromJson(bufferedReader, StoredUserListAccessor.getEntryType());
		} catch (IOException e) {
			E4mcClient.LOGGER.warn("Failed to load whitelist: ", e);
			return;
		}

		if (collection != null) {
			var entries = ((StoredUserListAccessor<T, A>) whitelist).invokeGetEntries();
			entries.clear();

			for (A storedUserEntry : collection) {
				var user = ((StoredUserEntryAccessor<T>) storedUserEntry).invokeGetUser();
				if (user != null) {
					entries.put(((StoredUserListAccessor<T, A>) whitelist).invokeGetKey(user), storedUserEntry);
				}
			}
		}
	}

	@Inject(method = "canLogin", at = @At("HEAD"), cancellable = true)
	private void allowOwnerLogin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<String> cir) {
		if (E4mcClient.isSingleplayerOwner(profile)) {
			cir.setReturnValue(null);
		}
	}
}
