package link.e4mc.fabric;

import io.github.axolotlclient.commands.ClientCommands;
import link.e4mc.E4mcClient;
import net.fabricmc.api.ModInitializer;

public class E4mcClientFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        E4mcClient.init();
        E4mcClient.registerCommands(ClientCommands.getDISPATCHER());
        //CommandRegistrationCallback.EVENT.register((dispatcher, ignored) -> E4mcClient.registerCommands(dispatcher));
    }
}
