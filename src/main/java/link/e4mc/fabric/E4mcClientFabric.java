package link.e4mc.fabric;

import link.e4mc.E4mcClient;
import net.ornithemc.osl.entrypoints.api.ModInitializer;

public class E4mcClientFabric implements ModInitializer {
	@Override
	public void init() {
		E4mcClient.init();
		E4mcClient.registerCommands(CommandRegistry.DISPATCHER);
	}
}
