package link.e4mc;

import java.nio.file.Path;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class Agnos {
	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType().equals(EnvType.CLIENT);
	}

	public static Path configDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	public static Path jarPath() {
		return FabricLoader.getInstance().getModContainer("e4mc_minecraft").get().getOrigin().getPaths().get(0);
	}
}
