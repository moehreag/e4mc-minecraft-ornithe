package link.e4mc.fabric;

import io.github.axolotlclient.AxolotlClient;
import io.github.axolotlclient.modules.AbstractModule;
import link.e4mc.Config;

public class E4mcAxolotlClientModule extends AbstractModule {

	@Override
	public void init() {
		AxolotlClient.CONFIG.general.add(Config.CATEGORY);
	}
}
