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

import java.util.HashMap;
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
    public Map<Integer, Rank> ranks;

    /*
     * Constructor
     */

    /**
     * For serialization purposes only.
     */
    public RankLadder() {
    }

    /**
     * Initialize a new {@link RankLadder}.
     *
     * @param name The name of this rank ladder; this is how it will be identified. This will be made lowercase.
     */
    public RankLadder(String name) {
        this.name = name.toLowerCase();
        this.ranks = new HashMap<>();
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
        ranks.put(position, rank);
    }

    /**
     * Add a rank to this ladder. The rank's position will be set to the next available position (i.e. at the end of the ladder).
     *
     * @param rank The {@link Rank} to add.
     */
    public void addRank(Rank rank) {
        ranks.put(getNextAvailablePosition(), rank);
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
            Rank rank = ranks.get(i);
            ranks.remove(i);
            ranks.put(i - 1, rank);
            i++;
        }

    }

    /*
     * Getters & Setters
     */

    /**
     * Returns the next highest rank in the ladder.
     *
     * @param oldPosition The position of the current rank.
     * @return An optional containing either the rank if there is a next rank in the ladder, or empty if there isn't.
     */
    public Optional<Rank> getNext(int oldPosition) {

        for (Map.Entry<Integer, Rank> rankEntry : ranks.entrySet()) {
            if (rankEntry.getKey() >= oldPosition) {
                return Optional.of(rankEntry.getValue());
            }
        }

        return Optional.empty();
    }

    public Optional<Rank> getPrevious(int oldPosition) {

        for (int position = oldPosition - 1; position >= 0; position--) {
            if (ranks.containsKey(position)) {
                return Optional.of(ranks.get(position));
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
