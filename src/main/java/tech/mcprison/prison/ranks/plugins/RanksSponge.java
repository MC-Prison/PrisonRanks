package tech.mcprison.prison.ranks.plugins;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.ranks.PrisonRanks;

/**
 * @author Faizaan A. Datoo
 */
@Plugin( //
    id = "prison-ranks", //
    name = "PrisonRanks", //
    version = RanksSponge.Version, //
    dependencies = { //
        @Dependency(id = "prison-sponge") //
    }, //
    description = "A ranks module for Prison.", //
    url = "https://mc-prison.tech", //
    authors = {"The MC-Prison Team"} //
) public class RanksSponge {

    static final String Version = "3.0.0-SNAPSHOT";

    @Listener public void onEnable(GameStartedServerEvent event) {
        PrisonRanks ranks = new PrisonRanks(Version);
        Prison.get().getModuleManager().registerModule(ranks);
    }

}
