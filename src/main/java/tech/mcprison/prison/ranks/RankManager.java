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
import java.util.List;
import java.util.Optional;

/**
 * Manages the creation, removal, and management of ranks.
 *
 * @author Faizaan A. Datoo
 */
public class RankManager {

    /*
     * Methods
     */

    /**
     * Loads a rank from a file into the loaded ranks list.
     * After this method is called, the rank will be ready for use in the server.
     *
     * @param rankFile The {@link File} that this rank is stored in, usually with the extension ".rank.json".
     * @throws IOException If the file could not be read or does not exist.
     */
    public void loadRank(File rankFile) throws IOException {

    }

    /**
     * Loads every file within a directory with the extension ".rank.json".
     * If one file could not be loaded, it will simply be skipped.
     *
     * @param rankFolder The directory to search for rank files.
     * @throws IOException If the folder could not be found, or if a file could not be read or does not exist.
     */
    public void loadRanks(File rankFolder) throws IOException {

    }

    /**
     * Saves a rank to its save file.
     *
     * @param rank     The {@link Rank} to save.
     * @param saveFile The file to write the rank to. This does not yet have to exist. Convention-wise, it should be named the rank identifier plus the extension ".rank.json".
     * @throws IOException If the rank could not be serialized, or if the rank could not be saved to the file.
     */
    public void saveRank(Rank rank, File saveFile) throws IOException {

    }

    /**
     * Saves all the loaded ranks to their own files within a directory.
     * Each rank file will be assigned a name in the format: rank identifier + ".rank.json".
     *
     * @param rankFolder The directory to store rank files.
     * @throws IOException If the rankFolder does not exist, or if one of the ranks could not be saved.
     */
    public void saveRanks(File rankFolder) throws IOException {

    }

    /**
     * Creates a new rank with the specified parameters.
     * The rank will be loaded and stored automatically.
     *
     * @return An optional containing either the {@link Rank} if it could be created, or empty
     * if the rank's creation failed.
     */
    public Optional<Rank> createRank() {
        return null;
    }

    /**
     * Removes the provided rank. This will go through the process of removing the rank from the loaded
     * ranks list, removing the rank's save files, adjusting the ladder positions that this rank is a part of,
     * and finally, moving the players back to the bottom rank of their ladder. This is a potentially destructive
     * operation; be sure that you are using it in the correct manner.
     *
     * @param rank The {@link Rank} to be removed.
     * @return true if the rank was removed successfully, false otherwise.
     */
    public boolean removeRank(Rank rank) {
        return false;
    }

    /**
     * Returns the rank with the specific identifier.
     *
     * @param identifier The rank's identifier. This was set when the rank was created. It is unique and, as a result, case-sensitive.
     * @return An optional containing either the {@link Rank} if it could be found, or empty if it does not exist by the specified identifier.
     */
    public Optional<Rank> getRank(String identifier) {
        return null;
    }

    /**
     * Returns a list of all the loaded ranks on the server.
     *
     * @return A {@link List}. This will never return null, because if there are no loaded ranks, the list will just be empty.
     */
    public List<Rank> getRanks() {
        return null;
    }

}
