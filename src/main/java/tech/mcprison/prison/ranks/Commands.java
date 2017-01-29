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

import tech.mcprison.prison.commands.Arg;
import tech.mcprison.prison.commands.Command;
import tech.mcprison.prison.internal.CommandSender;
import tech.mcprison.prison.output.Output;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The commands for this module.
 *
 * @author Faizaan A. Datoo
 */
public class Commands {

    /*
     * /ranks command
     */

    @Command(identifier = "rank create", description = "Creates a new rank", onlyPlayers = false, permissions = {
        "ranks.manage"}) public void createRank(CommandSender sender,
        @Arg(name = "name", description = "The name of this rank.") String name,
        //
        @Arg(name = "cost", description = "The cost of this rank.") double cost, //
        @Arg(name = "ladder", description = "The ladder to put this rank on.", def = "default")
            String ladder, //
        @Arg(name = "tag", description = "The tag to use for this rank.", def = "none") String tag
        //
    ) {

        // Ensure a rank with the name doesn't already exist
        if (PrisonRanks.getInstance().getRankManager().getRank(name).isPresent()) {
            Output.get()
                .sendWarn(sender, "A rank by this name already exists. Try a different name.");
            return;
        }

        // Fetch the ladder first, so we can see if it exists

        Optional<RankLadder> rankLadderOptional =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladder);
        if (!rankLadderOptional.isPresent()) {
            Output.get().sendWarn(sender, "A ladder by the name of '%s' does not exist.", ladder);
            return;
        }

        // Set a default tag if necessary
        if (tag.equals("none")) {
            tag = "[" + name + "]";
        }

        // Create the rank
        Optional<Rank> newRankOptional =
            PrisonRanks.getInstance().getRankManager().createRank(name, tag, cost);

        // Ensure it was created
        if (!newRankOptional.isPresent()) {
            Output.get().sendError(sender, "The rank could not be created.");
            return;
        }

        Rank newRank = newRankOptional.get();

        // Save the rank
        try {
            PrisonRanks.getInstance().getRankManager().saveRank(newRank);
        } catch (IOException e) {
            Output.get().sendError(sender,
                "The new rank could not be saved to disk. Check the console for details.");
            Output.get().logError("Rank could not be written to disk.", e);
        }

        // Add the ladder

        rankLadderOptional.get().addRank(newRank);
        try {
            PrisonRanks.getInstance().getLadderManager().saveLadder(rankLadderOptional.get());
        } catch (IOException e) {
            Output.get().sendError(sender,
                "The '%s' ladder could not be saved to disk. Check the console for details.",
                rankLadderOptional.get().name);
            Output.get().logError("Ladder could not be written to disk.", e);
        }

        // Tell the player the good news!
        Output.get()
            .sendInfo(sender, "Your new rank, '%s', was created in the ladder '%s'", name, ladder);

    }

    @Command(identifier = "rank list", description = "Lists all the ranks on the server.", onlyPlayers = false, permissions = {
        "ranks.manage"}) public void listRanks(CommandSender sender) {
        for (Rank rank : PrisonRanks.getInstance().getRankManager().getRanks()) {

            List<String> ladders = new ArrayList<>();
            for (RankLadder ladder : PrisonRanks.getInstance().getLadderManager()
                .getLaddersWithRank(rank.id)) {
                ladders.add(ladder.name);
            }

            Output.get().sendInfo(sender, String
                .join(" - ", String.valueOf(rank.id), rank.name, String.valueOf(rank.cost),
                    rank.tag, "(" + String.join(", ", ladders) + ")"));
        }
    }

}
