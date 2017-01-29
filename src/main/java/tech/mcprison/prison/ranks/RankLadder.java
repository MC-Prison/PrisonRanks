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

import tech.mcprison.prison.store.AbstractJsonable;

import java.util.Map;
import java.util.Optional;

/**
 * A certain sequence that rank-ups will follow. There may be multiple
 * rank ladders on the server at a time, and one rank may be a part of
 * multiple ladders.
 *
 * @author Faizaan A. Datoo
 */
public class RankLadder extends AbstractJsonable<RankLadder> {

    /*
     * Fields & Constants
     */

    public int id;
    public String name;
    public Map<Integer, Integer> ranks; // <Position, RankID>

    /*
     * Constructor
     */

    /**
     * For serialization purposes only.
     */
    public RankLadder() {
    }

    /*
     * Methods
     */

    /**
     * Add a rank to this ladder.
     *
     * @param position The place in line to put this rank, beginning at 0. The player will
     *                 be taken through each rank by order of their positions in the ladder.
     * @param rank     The {@link Rank} to add.
     */
    public void addRank(int position, Rank rank) {
        ranks.put(position, rank.id);
    }

    /**
     * Add a rank to this ladder. The rank's position will be set to the next available position (i.e. at the end of the ladder).
     *
     * @param rank The {@link Rank} to add.
     */
    public void addRank(Rank rank) {
        ranks.put(getNextAvailablePosition(), rank.id);
    }

    /**
     * Removes a rank from this ladder.
     *
     * @param position The position of the rank to be removed. The positions of the rest of the
     *                 ranks will be downshifted to fill the gap.
     */
    public void removeRank(int position) {
        if (ranks.size() < position) {
            throw new ArrayIndexOutOfBoundsException(
                position + " is greater than array size of " + ranks.size());
        }

        ranks.remove(position);

        // Move everything down one.

        int i = position + 1;

        while (i <= ranks.size()) {
            int rank = ranks.get(i);
            ranks.remove(i);
            ranks.put(i - 1, rank);
            i++;
        }

    }

    /*
     * Getters & Setters
     */

    /**
     * Returns true if this ladder contains a rank with a specified ID.
     *
     * @param rankId The ID of the rank to search for.
     * @return True if the rank was found, false otherwise.
     */
    public boolean containsRank(int rankId) {
        return ranks.values().stream().anyMatch(rank -> rank == rankId);
    }

    /**
     * Returns the position of the specified {@link Rank} in this ladder.
     *
     * @param rank The {@link Rank} to retrieve the position of.
     * @return The position of the rank, or -1 if the rank was not found.
     */
    public int getPositionOfRank(Rank rank) {
        for (Map.Entry<Integer, Integer> rankEntry : ranks.entrySet()) {
            if (rankEntry.getValue() == rank.id) {
                return rankEntry.getKey();
            }
        }

        return -1;
    }

    /**
     * Returns the next highest rank in the ladder.
     *
     * @param oldPosition The position of the current rank.
     * @return An optional containing either the rank if there is a next rank in the ladder, or empty if there isn't or if the rank does not exist anymore.
     */
    public Optional<Rank> getNext(int oldPosition) {

        for (Map.Entry<Integer, Integer> rankEntry : ranks.entrySet()) {
            if (rankEntry.getKey() >= oldPosition) {
                return PrisonRanks.getInstance().getRankManager().getRank(rankEntry.getValue());
            }
        }

        return Optional.empty();
    }

    public Optional<Rank> getPrevious(int oldPosition) {

        for (int position = oldPosition - 1; position >= 0; position--) {
            if (ranks.containsKey(position)) {
                return PrisonRanks.getInstance().getRankManager().getRank(ranks.get(position));
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the next available position for a rank, by finding the highest one.
     *
     * @return The open position.
     */
    private int getNextAvailablePosition() {
        int highest = 0;

        for (int position : ranks.keySet()) {
            if (position >= highest) {
                highest = position;
            }
        }

        return highest;
    }

}
