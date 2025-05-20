package link.e4mc;

import java.nio.file.Files;
import java.nio.file.Path;

import io.github.axolotlclient.AxolotlClientConfig.api.AxolotlClientConfig;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.AxolotlClientConfig.impl.managers.JsonConfigManager;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.StringOption;

public class Config {
	public static final OptionCategory CATEGORY = OptionCategory.create("e4mc_minecraft").includeInParentTree(false);
	private static final Path FILE = Agnos.configDir().resolve("e4mc/e4mc.json");
	private static final JsonConfigManager MANAGER = new JsonConfigManager(FILE, CATEGORY);
	public static final Config INSTANCE = new Config();

	public static void register() {
		AxolotlClientConfig.getInstance().register(MANAGER);
	}

	private Config() {
		CATEGORY.add(useBroker, brokerUrl, relayHost, relayPort);
		if (!Files.exists(FILE)) {
			MANAGER.save();
		}
		MANAGER.load();
	}

	public final BooleanOption useBroker = new BooleanOption("e4mc.use_broker", true);
	public final StringOption brokerUrl = new StringOption("e4mc.broker_url", "https://broker.e4mc.link/getBestRelay");

	public final StringOption relayHost = new StringOption("e4mc.relay_host", "test.e4mc.link");
	public final StringOption relayPort = new StringOption("e4mc.relay_port", "25575");
}