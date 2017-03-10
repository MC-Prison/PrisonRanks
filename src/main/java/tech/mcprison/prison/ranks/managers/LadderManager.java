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

package tech.mcprison.prison.ranks.managers;

import tech.mcprison.prison.Prison;
import tech.mcprison.prison.ranks.data.RankLadder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the creation, removal, and management of ladders.
 *
 * @author Faizaan A. Datoo
 */
public class LadderManager {

    /*
     * Fields & Constants
     */

    public static final String LADDER_EXTENSION = ".ladder.json";

    private File ladderFolder;
    private List<RankLadder> loadedLadders;

    /*
     * Constructor
     */

    /**
     * Instantiate this {@link LadderManager}.
     *
     * @param ladderFolder The directory to store ladder files.
     */
    public LadderManager(File ladderFolder) {
        this.ladderFolder = ladderFolder;
        this.loadedLadders = new ArrayList<>();
    }

    /*
     * Methods & Getters & Setters
     */

    /**
     * Loads a ladder from a file into the loaded ladders list.
     * After this method is called, the ladder will be ready for use in the server.
     *
     * @param fileKey The keye that this ladder is stored as. This is case-sensitive.
     * @throws IOException If the file could not be read or does not exist.
     */
    public void loadLadder(String fileKey) throws IOException {
        RankLadder ladder = Prison.get().getPlatform().getStorage().read(fileKey, RankLadder.class);
        loadedLadders.add(ladder);
    }

    /**
     * Loads every {@link RankLadder} stored to disk.
     *
     * @throws IOException If the folder could not be found, or if a file could not be read or does not exist.
     */
    public void loadLadders() throws IOException {
        List<RankLadder> ladders =
            Prison.get().getPlatform().getStorage().readAll(RankLadder.class);
        loadedLadders.addAll(ladders);
    }

    /**
     * Saves a ladder to its save file.
     *
     * @param ladder  The {@link RankLadder} to save.
     * @param fileKey The key to write the ladder as.
     * @throws IOException If the ladder could not be serialized, or if the ladder could not be saved to the file.
     */
    public void saveLadder(RankLadder ladder, String fileKey) throws IOException {
        Prison.get().getPlatform().getStorage().write(fileKey, ladder);
    }

    /**
     * Saves a ladder to its save file.
     *
     * @param ladder The {@link RankLadder} to save.
     * @throws IOException If the ladder could not be serialized, or if the ladder could not be saved to the file.
     */
    public void saveLadder(RankLadder ladder) throws IOException {
        this.saveLadder(ladder, "ladder_" + ladder.id);
    }

    /**
     * Saves all the loaded ladders to their own files within a directory.
     * Each ladder file will be assigned a name in the format: ladder_&lt;ladder id&gt;.
     *
     * @throws IOException If the ladderFolder does not exist, or if one of the ladders could not be saved.
     */
    public void saveLadders() throws IOException {
        for (RankLadder ladder : loadedLadders) {
            Prison.get().getPlatform().getStorage().write("ladder_" + ladder.id, ladder);
        }
    }

    /**
     * Creates a new ladder with the specified parameters.
     * This new ladder will be loaded, but will not be written to disk until {@link #saveLadder(RankLadder, String)} is called.
     *
     * @param name The name of this ladder, for use with the user (i.e. this will be shown to the user).
     * @return An optional containing either the {@link RankLadder} if it could be created, or empty
     * if the ladder's creation failed.
     */
    public Optional<RankLadder> createLadder(String name) {
        // Set the default values...
        RankLadder newLadder = new RankLadder();
        newLadder.id = getNextAvailableId();
        newLadder.name = name;
        newLadder.ranks = new HashMap<>();

        // ... add it to the list...
        loadedLadders.add(newLadder);

        // ...and return it.
        return Optional.of(newLadder);
    }

    /**
     * Returns the next available ID for a new ladder.
     * This works by adding one to the highest current ladder ID.
     *
     * @return The next available ladder's ID.
     */
    private int getNextAvailableId() {
        // Set the highest to -1 for now, since we'll add one at the end
        int highest = -1;

        // If anything's higher, it's now the highest...
        for (RankLadder ladder : loadedLadders) {
            if (highest < ladder.id) {
                highest = ladder.id;
            }
        }

        return highest + 1;
    }

    /**
     * Removes the provided ladder. This will go through the process of removing the ladder from the loaded
     * ladders list, removing the ladder's save files, removing the ranks from the ladder, and handling the affected players.
     * This is a destructive operation; be sure that you are using it in the correct manner.
     *
     * @param ladder The {@link RankLadder} to be removed.
     * @return true if the ladder was removed successfully, false otherwise.
     */
    public boolean removeLadder(RankLadder ladder) {
        // Remove it from the list...
        loadedLadders.remove(ladder);

        // ... TODO Remove the ranks from the ladder ...

        // ... TODO Handle affected players ...

        // ... and remove the ladder's save files.
        Prison.get().getPlatform().getStorage().delete("ladder_" + ladder.id, RankLadder.class);
        return true;
    }

    /**
     * Returns the ladder with the specified name.
     *
     * @param name The ladder's name, case-sensitive.
     * @return An optional containing either the {@link RankLadder} if it could be found, or empty if it does not exist by the specified name.
     */
    public Optional<RankLadder> getLadder(String name) {
        return loadedLadders.stream().filter(ladder -> ladder.name.equals(name)).findFirst();
    }

    /**
     * Returns the ladder with the specified ID.
     *
     * @param id The ladder's ID.
     * @return An optional containing either the {@link RankLadder} if it could be found, or empty if it does not exist by the specified id.
     */
    public Optional<RankLadder> getLadder(int id) {
        return loadedLadders.stream().filter(ladder -> ladder.id == id).findFirst();
    }

    /**
     * Returns a list of all the loaded ladders on the server.
     *
     * @return A {@link List}. This will never return null, because if there are no loaded ladders, the list will just be empty.
     */
    public List<RankLadder> getLadders() {
        return loadedLadders;
    }

    /**
     * Returns a list of the ladders which contain a rank.
     * If the server is set up correctly, the list should never be empty (the default ladder will at least be present). However,
     * it is safer to check for this condition for a fail-safe.
     *
     * @param rankId The ID of the rank to check each ladder against.
     * @return A list of {@link RankLadder}s with the matched criteria.
     */
    public List<RankLadder> getLaddersWithRank(int rankId) {
        return loadedLadders.stream().filter(rankLadder -> rankLadder.containsRank(rankId))
            .collect(Collectors.toList());
    }

}
