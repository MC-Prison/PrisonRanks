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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the creation, removal, and management of ranks.
 *
 * @author Faizaan A. Datoo
 */
public class RankManager {

    /*
     * Fields & Constants
     */

    public static final String RANK_EXTENSION = ".rank.json";

    private File rankFolder;
    private List<Rank> loadedRanks;

    /*
     * Constructor
     */

    /**
     * Instantiate this {@link RankManager}.
     *
     * @param rankFolder The directory to store rank files.
     */
    public RankManager(File rankFolder) {
        this.rankFolder = rankFolder;
        this.loadedRanks = new ArrayList<>();
    }

    /*
     * Methods & Getters & Setters
     */

    /**
     * Loads a rank from a file into the loaded ranks list.
     * After this method is called, the rank will be ready for use in the server.
     *
     * @param rankFile The {@link File} that this rank is stored in, usually with the extension ".rank.json".
     * @throws IOException If the file could not be read or does not exist.
     */
    public void loadRank(File rankFile) throws IOException {
        Rank dummy = new Rank();
        Rank rank = dummy.fromFile(rankFile);
        loadedRanks.add(rank);
    }

    /**
     * Loads every file within a directory with the extension ".rank.json".
     * If one file could not be loaded, it will simply be skipped.
     *
     * @throws IOException If the folder could not be found, or if a file could not be read or does not exist.
     */
    public void loadRanks() throws IOException {
        File[] rankFiles = rankFolder.listFiles((dir, name) -> name.endsWith(RANK_EXTENSION));

        if (rankFiles != null) {
            for (File file : rankFiles) {
                loadRank(file);
            }
        }
    }

    /**
     * Saves a rank to its save file.
     *
     * @param rank     The {@link Rank} to save.
     * @param saveFile The file to write the rank to. This does not yet have to exist. Convention-wise, it should be named the rank identifier plus the extension ".rank.json".
     * @throws IOException If the rank could not be serialized, or if the rank could not be saved to the file.
     */
    public void saveRank(Rank rank, File saveFile) throws IOException {
        rank.toFile(saveFile);
    }

    /**
     * Saves a rank to its save file.
     *
     * @param rank The {@link Rank} to save.
     * @throws IOException If the rank could not be serialized, or if the rank could not be saved to the file.
     */
    public void saveRank(Rank rank) throws IOException {
        this.saveRank(rank, new File(rankFolder, rank.id + RANK_EXTENSION));
    }

    /**
     * Saves all the loaded ranks to their own files within a directory.
     * Each rank file will be assigned a name in the format: rank identifier + {@link #RANK_EXTENSION}.
     *
     * @throws IOException If the rankFolder does not exist, or if one of the ranks could not be saved.
     */
    public void saveRanks() throws IOException {
        for (Rank rank : loadedRanks) {
            File rankFile = new File(rankFolder, rank.id + RANK_EXTENSION);
            rank.toFile(rankFile);
        }
    }

    /**
     * Creates a new rank with the specified parameters.
     * This new rank will be loaded, but will not be written to disk until {@link #saveRank(Rank, File)} is called.
     *
     * @param name The name of this rank, for use with the user (i.e. this will be shown to the user).
     * @param tag  The tag of this rank, which is used for prefixes/suffixes.
     * @param cost The cost of this rank, in whichever units the player chose (i.e. money or experience).
     * @return An optional containing either the {@link Rank} if it could be created, or empty
     * if the rank's creation failed.
     */
    public Optional<Rank> createRank(String name, String tag, double cost) {
        // Set the default values...
        Rank newRank = new Rank();
        newRank.id = getNextAvailableId();
        newRank.name = name;
        newRank.tag = tag;
        newRank.cost = cost;

        // ... add it to the list...
        loadedRanks.add(newRank);

        // ...and return it.
        return Optional.of(newRank);
    }

    /**
     * Returns the next available ID for a new rank.
     * This works by adding one to the highest current rank ID.
     *
     * @return The next available rank's ID.
     */
    private int getNextAvailableId() {
        // Set the highest to -1 for now, since we'll add one at the end
        int highest = -1;

        // If anything's higher, it's now the highest...
        for (Rank rank : loadedRanks) {
            if (highest < rank.id) {
                highest = rank.id;
            }
        }

        return highest + 1;
    }

    /**
     * Removes the provided rank. This will go through the process of removing the rank from the loaded
     * ranks list, removing the rank's save files, adjusting the ladder positions that this rank is a part of,
     * and finally, moving the players back to the previous rank of their ladder. This is a potentially destructive
     * operation; be sure that you are using it in the correct manner.
     *
     * @param rank The {@link Rank} to be removed.
     * @return true if the rank was removed successfully, false otherwise.
     */
    public boolean removeRank(Rank rank) {
        // Remove it from the list...
        loadedRanks.remove(rank);

        // ... TODO adjust the ladder positions...

        // ... TODO move players to the previous rank...

        // ... and remove the rank's save files.
        File saveFile = new File(rankFolder, rank.id + RANK_EXTENSION);
        if (!saveFile.exists()) {
            return true;
        } else {
            return saveFile.delete();
        }
    }

    /**
     * Returns the rank with the specified name.
     *
     * @param name The rank's name, case-sensitive.
     * @return An optional containing either the {@link Rank} if it could be found, or empty if it does not exist by the specified name.
     */
    public Optional<Rank> getRank(String name) {
        return loadedRanks.stream().filter(rank -> rank.name.equals(name)).findFirst();
    }

    /**
     * Returns the rank with the specified ID.
     *
     * @param id The rank's ID.
     * @return An optional containing either the {@link Rank} if it could be found, or empty if it does not exist by the specified id.
     */
    public Optional<Rank> getRank(int id) {
        return loadedRanks.stream().filter(rank -> rank.id == id).findFirst();
    }

    /**
     * Returns a list of all the loaded ranks on the server.
     *
     * @return A {@link List}. This will never return null, because if there are no loaded ranks, the list will just be empty.
     */
    public List<Rank> getRanks() {
        return loadedRanks;
    }

}
