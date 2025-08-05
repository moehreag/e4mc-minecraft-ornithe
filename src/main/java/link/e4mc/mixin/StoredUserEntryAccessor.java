package link.e4mc.mixin;

import net.minecraft.server.StoredUserEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StoredUserEntry.class)
public interface StoredUserEntryAccessor<T> {

	@Invoker
	T invokeGetUser();
}
