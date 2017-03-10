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

package tech.mcprison.prison.ranks;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.Output;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.managers.LadderManager;
import tech.mcprison.prison.ranks.managers.PlayerManager;
import tech.mcprison.prison.ranks.managers.RankManager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * @author Faizaan A. Datoo
 */
public class PrisonRanks extends Module {

    /*
     * Fields & Constants
     */

    private static PrisonRanks instance;
    private File ranksFolder, laddersFolder, playersFolder;
    private RankManager rankManager;
    private LadderManager ladderManager;
    private PlayerManager playerManager;

    /*
     * Constructor
     */

    public PrisonRanks(String version) {
        super("Ranks", version, 1);
    }

    /*
     * Methods
     */

    public static PrisonRanks getInstance() {
        return instance;
    }

    @Override public void enable() {
        instance = this;

        // Load up the ranks

        ranksFolder = new File(getDataFolder(), "data");
        ranksFolder.mkdir();
        rankManager = new RankManager(ranksFolder);
        try {
            rankManager.loadRanks();
        } catch (IOException e) {
            Output.get().logError("A rank file failed to load.", e);
        }

        // Load up the ladders

        laddersFolder = new File(ranksFolder, "ladders");
        laddersFolder.mkdir();
        ladderManager = new LadderManager(laddersFolder);
        try {
            ladderManager.loadLadders();
        } catch (IOException e) {
            Output.get().logError("A ladder file failed to load.", e);
        }
        createDefaultLadder();

        // Load up the players

        playersFolder = new File(ranksFolder, "players");
        playersFolder.mkdir();
        playerManager = new PlayerManager(playersFolder);
        try {
            playerManager.loadPlayers();
        } catch (IOException e) {
            Output.get().logError("A player file failed to load.", e);
        }

        // Load up the commands

        Prison.get().getCommandHandler().registerCommands(new Commands());

        // Load up all else

        new FirstJoinHandler();

    }

    /**
     * A default ladder is absolutely necessary on the server, so let's create it if it doesn't exist.
     */
    private void createDefaultLadder() {
        if (!ladderManager.getLadder("default").isPresent()) {
            Optional<RankLadder> rankLadderOptional = ladderManager.createLadder("default");

            if (!rankLadderOptional.isPresent()) {
                Output.get().logError("Could not create the default ladder.");
                Prison.get().getModuleManager()
                    .setStatus(this.getName(), "&cNo default ladder found.");
                return;
            }

            try {
                ladderManager.saveLadder(rankLadderOptional.get());
            } catch (IOException e) {
                Output.get().logError("Could not save the default ladder.", e);
                Prison.get().getModuleManager()
                    .setStatus(this.getName(), "&cNo default ladder found.");
            }
        }
    }

    /*
     * Getters & Setters
     */

    @Override public void disable() {
        try {
            rankManager.saveRanks();
        } catch (IOException e) {
            Output.get().logError("A ranks file failed to save.", e);
        }
    }

    public File getRanksFolder() {
        return ranksFolder;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public File getLaddersFolder() {
        return laddersFolder;
    }

    public LadderManager getLadderManager() {
        return ladderManager;
    }

    public File getPlayersFolder() {
        return playersFolder;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public RankLadder getDefaultLadder() {
        return getLadderManager().getLadder("default").orElseThrow(IllegalStateException::new);
    }

}
