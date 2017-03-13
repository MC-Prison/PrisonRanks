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

import com.google.gson.internal.LinkedTreeMap;
import tech.mcprison.prison.ranks.PrisonRanks;
import tech.mcprison.prison.ranks.RankUtil;
import tech.mcprison.prison.store.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A certain sequence that rank-ups will follow. There may be multiple
 * rank ladders on the server at a time, and one rank may be a part of
 * multiple ladders.
 *
 * @author Faizaan A. Datoo
 */
public class RankLadder {

    /*
     * Fields & Constants
     */

    public int id;
    public String name;
    public List<PositionRank> ranks;

    /*
     * Document-related
     */

    public RankLadder() {
    }

    public RankLadder(Document document) {
        this.id = RankUtil.doubleToInt(document.get("id"));
        this.name = (String) document.get("name");
        List<LinkedTreeMap<String, Object>> ranksLocal =
            (List<LinkedTreeMap<String, Object>>) document.get("ranks");

        this.ranks = new ArrayList<>();
        for(LinkedTreeMap<String, Object> rank : ranksLocal) {
            ranks.add(new PositionRank(RankUtil.doubleToInt(rank.get("position")), RankUtil.doubleToInt((rank.get("rankId")))));
        }
    }

    public Document toDocument() {
        Document ret = new Document();
        ret.put("id", this.id);
        ret.put("name", this.name);
        ret.put("ranks", this.ranks);
        return ret;
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
        ranks.add(new PositionRank(position, rank.id));
    }

    /**
     * Add a rank to this ladder. The rank's position will be set to the next available position (i.e. at the end of the ladder).
     *
     * @param rank The {@link Rank} to add.
     */
    public void addRank(Rank rank) {
        ranks.add(new PositionRank(getNextAvailablePosition(), rank.id));
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
            int rank = ranks.get(i).getRankId();
            ranks.remove(i);
            ranks.add(new PositionRank(i - 1, rank));
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
        return ranks.stream().anyMatch(rank -> rank.getRankId() == rankId);
    }

    /**
     * Returns the position of the specified {@link Rank} in this ladder.
     *
     * @param rank The {@link Rank} to retrieve the position of.
     * @return The position of the rank, or -1 if the rank was not found.
     */
    public int getPositionOfRank(Rank rank) {
        for (PositionRank rankEntry : ranks) {
            if (rankEntry.getRankId() == rank.id) {
                return rankEntry.getPosition();
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

        for (PositionRank rankEntry : ranks) {
            if (rankEntry.getPosition() > oldPosition) {
                System.out.println(rankEntry.getPosition() + " - " + rankEntry.getRankId());
                return PrisonRanks.getInstance().getRankManager().getRank(rankEntry.getRankId());
            }
        }

        return Optional.empty();
    }

    public Optional<Rank> getPrevious(int oldPosition) {

        for (int position = oldPosition - 1; position >= 0; position--) {
            int finalPosition = position;
            if (ranks.stream()
                .anyMatch(positionRank -> positionRank.getPosition() == finalPosition)) {
                return PrisonRanks.getInstance().getRankManager().getRank(ranks.stream()
                    .filter(positionRank -> positionRank.getPosition() == finalPosition).findFirst()
                    .get().getPosition());
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

        for (PositionRank rank : ranks) {
            if (rank.getPosition() >= highest) {
                highest = rank.getPosition();
            }
        }

        return highest;
    }

    /*
     * equals() and hashCode()
     */

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RankLadder)) {
            return false;
        }

        RankLadder that = (RankLadder) o;

        if (id != that.id) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    class PositionRank {
        private int position;
        private int rankId;

        public PositionRank(int position, int rankId) {
            this.position = position;
            this.rankId = rankId;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getRankId() {
            return rankId;
        }

        public void setRankId(int rankId) {
            this.rankId = rankId;
        }
    }

}
