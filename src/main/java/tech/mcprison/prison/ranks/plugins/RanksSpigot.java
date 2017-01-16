package tech.mcprison.prison.ranks.plugins;

import org.bukkit.plugin.java.JavaPlugin;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.ranks.PrisonRanks;

/**
 * @author Faizaan A. Datoo
 */
public class RanksSpigot extends JavaPlugin {

    @Override public void onEnable() {
        PrisonRanks ranks = new PrisonRanks(getDescription().getVersion());
        Prison.get().getModuleManager().registerModule(ranks);
    }
}
