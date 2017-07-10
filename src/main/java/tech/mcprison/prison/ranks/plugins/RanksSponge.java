/*
 * Copyright (C) 2017 The MC-Prison Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    static final String Version = "1.0.0-SNAPSHOT";

    @Listener public void onEnable(GameStartedServerEvent event) {
        PrisonRanks ranks = new PrisonRanks(Version);
        Prison.get().getModuleManager().registerModule(ranks);
    }

}
