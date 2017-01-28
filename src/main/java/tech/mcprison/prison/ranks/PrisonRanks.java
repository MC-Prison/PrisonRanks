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

import tech.mcprison.prison.modules.Module;
import tech.mcprison.prison.output.Output;

import java.io.File;
import java.io.IOException;

/**
 * @author Faizaan A. Datoo
 */
public class PrisonRanks extends Module {

    /*
     * Fields & Constants
     */

    private static PrisonRanks instance;
    private File ranksFolder, laddersFolder;
    private RankManager rankManager;
    private LadderManager ladderManager;

    /*
     * Constructor
     */

    public PrisonRanks(String version) {
        super("Ranks", version, 1);
    }

    /*
     * Methods
     */

    @Override public void enable() {
        instance = this;

        // Load up the ranks

        ranksFolder = new File(getDataFolder(), "data");
        rankManager = new RankManager(ranksFolder);
        try {
            rankManager.loadRanks();
        } catch (IOException e) {
            Output.get().logError("A rank file failed to load.", e);
        }

        // Load up the ladders

        laddersFolder = new File(ranksFolder, "ladders");
        ladderManager = new LadderManager(laddersFolder);
        try {
            ladderManager.loadLadders();
        } catch (IOException e) {
            Output.get().logError("A ladder file failed to load.", e);
        }


    }

    @Override public void disable() {
        try {
            rankManager.saveRanks();
        } catch (IOException e) {
            Output.get().logError("A ranks file failed to save.", e);
        }
    }

    /*
     * Getters & Setters
     */

    public static PrisonRanks getInstance() {
        return instance;
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

}
