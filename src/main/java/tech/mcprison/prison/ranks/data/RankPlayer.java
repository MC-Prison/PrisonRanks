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

package tech.mcprison.prison.ranks.data;

import tech.mcprison.prison.store.AbstractJsonable;

import java.util.HashMap;
import java.util.UUID;

/**
 * Represents a player with ranks.
 *
 * @author Faizaan A. Datoo
 */
public class RankPlayer extends AbstractJsonable<RankPlayer> {

    /*
     * Fields & Constants
     */

    public UUID uid;
    public HashMap<String, Integer> ranks; // <Ladder Name, Rank ID>

    /*
     * equals() and hashCode()
     */

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RankPlayer)) {
            return false;
        }

        RankPlayer that = (RankPlayer) o;

        return uid.equals(that.uid);
    }

    @Override public int hashCode() {
        return uid.hashCode();
    }

}
