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

    @Command(identifier = "ranks create", description = "Creates a new rank", onlyPlayers = false, permissions = {
        "ranks.manage"}) public void createRank(CommandSender sender,
        @Arg(name = "name", description = "The name of this rank.") String name,
        //
        @Arg(name = "cost", description = "The cost of this rank.") double cost, //
        @Arg(name = "tag", description = "The tag to use for this rank.", def = "none") String tag
        //
    ) {

        // Ensure a rank with the name doesn't already exist
        if (PrisonRanks.getInstance().getRankManager().getRank(name).isPresent()) {
            Output.get()
                .sendWarn(sender, "A rank by this name already exists. Try a different name.");
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

    }

    @Command(identifier = "ranks list", description = "Lists all the ranks on the server.", onlyPlayers = false, permissions = {
        "ranks.manage"}) public void listRanks(CommandSender sender) {
        for (Rank rank : PrisonRanks.getInstance().getRankManager().getRanks()) {
            Output.get().sendInfo(sender, String
                .join(" - ", String.valueOf(rank.id), rank.name, String.valueOf(rank.cost),
                    rank.tag));
        }
    }

}
