package link.e4mc;

import net.ornithemc.osl.config.api.ConfigManager;
import net.ornithemc.osl.config.api.ConfigScope;
import net.ornithemc.osl.config.api.LoadingPhase;
import net.ornithemc.osl.config.api.config.BaseConfig;
import net.ornithemc.osl.config.api.config.option.BooleanOption;
import net.ornithemc.osl.config.api.config.option.StringOption;
import net.ornithemc.osl.config.api.serdes.FileSerializerType;
import net.ornithemc.osl.config.api.serdes.SerializerTypes;

public class Config extends BaseConfig {
	public static Config INSTANCE;

	private Config() {
	}

	public BooleanOption useBroker = new BooleanOption("e4mc.use_broker", "Whether to use the broker to get the best relay <br>based on location or use a hard-coded relay.", true);
	public StringOption brokerUrl = new StringOption("e4mc.broker_url", "", "https://broker.e4mc.link/getBestRelay");

	public StringOption relayHost = new StringOption("e4mc.relay_host", "", "test.e4mc.link");
	public StringOption relayPort = new StringOption("e4mc.relay_port", "", "25575");

	public static void register() {
		if (INSTANCE == null) {
			ConfigManager.register(INSTANCE = new Config());
		} else {
			throw new IllegalStateException("Config may only be loaded once!");
		}
	}

	@Override
	public String getNamespace() {
		return "e4mc";
	}

	@Override
	public String getName() {
		return "e4mc";
	}

	@Override
	public String getSaveName() {
		return "e4mc.json";
	}

	@Override
	public ConfigScope getScope() {
		return ConfigScope.GLOBAL;
	}

	@Override
	public LoadingPhase getLoadingPhase() {
		return LoadingPhase.READY;
	}

	@Override
	public FileSerializerType<?> getType() {
		return SerializerTypes.JSON;
	}

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public void init() {
		registerOptions("root", useBroker, brokerUrl, relayHost, relayPort);
	}
}