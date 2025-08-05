package link.e4mc.mixin;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import com.google.gson.Gson;
import net.minecraft.server.StoredUserEntry;
import net.minecraft.server.StoredUserList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StoredUserList.class)
public interface StoredUserListAccessor<K, V extends StoredUserEntry<K>> {
	@Accessor
	Gson getGson();

	@Accessor("ENTRY_TYPE")
	static ParameterizedType getEntryType() {
		throw new UnsupportedOperationException();
	}

	@Invoker
	Map<String, V> invokeGetEntries();

	@Invoker
	String invokeGetKey(K key);

	@Accessor("file")
	File accessFile();
}
