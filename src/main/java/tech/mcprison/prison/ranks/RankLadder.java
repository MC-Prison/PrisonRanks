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

import java.util.LinkedHashMap;
import java.util.Map;

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

    private String name;
    private Map<Integer, Rank> ranks;

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
        this.ranks = new LinkedHashMap<>();
    }

    /*
     * Methods
     */

    public void addRank(int position, Rank rank) {
        ranks.put(position, rank);
    }

    public void removeRank(int position) {
        if (ranks.size() < position) {
            throw new ArrayIndexOutOfBoundsException(
                position + " is greater than array size of " + ranks.size());
        }

        ranks.remove(position);

        int i = position + 1;

        while(i <= ranks.size()) {
            // TODO Finish this
        }

    }

    /*
     * Getters & Setters
     */

    public String getName() {
        return name;
    }

    public Map<Integer, Rank> getRanks() {
        return ranks;
    }

}
