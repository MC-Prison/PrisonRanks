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
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.output.BulletedListComponent;
import tech.mcprison.prison.output.ChatDisplay;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.ranks.data.Rank;
import tech.mcprison.prison.ranks.data.RankLadder;
import tech.mcprison.prison.ranks.data.RankPlayer;

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
     * /rankup command
     */

    @Command(identifier = "rankup", description = "Ranks up to the next rank.", permissions = {
        "ranks.user", "ranks.admin"}) public void rankUp(Player sender,
        @Arg(name = "ladder", description = "The ladder to rank up on.", def = "default")
            String ladderName) {

        // RETRIEVE THE LADDER

        // This player has to have permission to rank up on this ladder.
        if (!ladderName.equalsIgnoreCase("default") && !sender
            .hasPermission("ranks.rankup." + ladderName.toLowerCase())) {
            Output.get()
                .sendError(sender, "You need the permission '%s' to rank up on this ladder.",
                    "ranks.rankup." + ladderName.toLowerCase());
            return;
        }

        Optional<RankLadder> ladderOptional =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);

        // The ladder doesn't exist
        if (!ladderOptional.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' does not exist.", ladderName);
            return;
        }

        // RETRIEVE THE PLAYER

        Output.get().logInfo("UUID: %s", sender.getUUID().toString());

        Optional<RankPlayer> playerOptional =
            PrisonRanks.getInstance().getPlayerManager().getPlayer(sender.getUUID());

        // Well, this isn't supposed to happen...
        if (!playerOptional.isPresent()) {
            Output.get().sendError(sender,
                "You don't exist! The server has no records of you. Try rejoining, or contact a server administrator for help.");
            return;
        }

        // RANK-UP THE PLAYER

        RankPlayer player = playerOptional.get();

        RankUtil.RankUpResult result = RankUtil.rankUpPlayer(player, ladderName);

        switch (result.status) {
            case RankUtil.RANKUP_SUCCESS:
                Output.get().sendInfo(sender, "Congratulations! You have ranked up to rank '%s'.",
                    result.rank.name);
                break;
            case RankUtil.RANKUP_CANT_AFFORD:
                Output.get().sendError(sender,
                    "You don't have enough money to rank up! The next rank costs %s.",
                    RankUtil.doubleToDollarString(result.rank.cost));
                break;
            case RankUtil.RANKUP_HIGHEST:
                Output.get().sendInfo(sender, "You are already at the highest rank!");
                break;
            case RankUtil.RANKUP_FAILURE:
                Output.get().sendError(sender,
                    "Failed to retrieve or write data. Your files may be corrupted. Alert a server administrator.");
                break;
            case RankUtil.RANKUP_NO_RANKS:
                Output.get().sendError(sender, "There are no ranks in this ladder.");
                break;
        }

    }

    @Command(identifier = "ranks", onlyPlayers = false, permissions = {"ranks.user", "ranks.admin"})
    public void baseCommand(CommandSender sender) {
        if (!sender.hasPermission("ranks.admin")) {
            sender.dispatchCommand("ranks list");
        } else {
            sender.dispatchCommand("ranks help");
        }
    }

    /*
     * /rank command
     */

    @Command(identifier = "ranks create", description = "Creates a new rank", onlyPlayers = false, permissions = {
        "ranks.admin"}) public void createRank(CommandSender sender,
        @Arg(name = "name", description = "The name of this rank.") String name,
        @Arg(name = "cost", description = "The cost of this rank.") double cost,
        @Arg(name = "ladder", description = "The ladder to put this rank on.", def = "default")
            String ladder,
        @Arg(name = "tag", description = "The tag to use for this rank.", def = "none")
            String tag) {

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

    @Command(identifier = "ranks remove", description = "Removes a rank, and deletes its files.", onlyPlayers = false, permissions = {
        "ranks.admin"})
    public void removeRank(CommandSender sender, @Arg(name = "name") String rankName) {
        // Check to ensure the rank exists
        Optional<Rank> rankOptional = PrisonRanks.getInstance().getRankManager().getRank(rankName);
        if (!rankOptional.isPresent()) {
            Output.get().sendError(sender, "The rank '%s' does not exist.", rankName);
            return;
        }

        Rank rank = rankOptional.get();

        boolean success = PrisonRanks.getInstance().getRankManager().removeRank(rank);

        if (success) {
            Output.get().sendInfo(sender, "The rank '%s' has been removed successfully.", rankName);
        } else {
            Output.get()
                .sendError(sender, "The rank '%s' could not be deleted due to an error.", rankName);
        }
    }

    @Command(identifier = "ranks list", description = "Lists all the ranks on the server.", onlyPlayers = false, permissions = {
        "ranks.user", "ranks.admin"}) public void listRanks(CommandSender sender) {
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

    @Command(identifier = "ranks command add", description = "Adds a command to a rank.", onlyPlayers = false, permissions = {
        "ranks.admin"})
    public void commandAdd(CommandSender sender, @Arg(name = "rank") String rankName,
        @Arg(name = "command") String command) {
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }

        Optional<Rank> rankOptional = PrisonRanks.getInstance().getRankManager().getRank(rankName);
        if (!rankOptional.isPresent()) {
            Output.get().sendError(sender, "The rank '%s' does not exist.", rankName);
            return;
        }
        Rank rank = rankOptional.get();

        if (rank.rankUpCommands == null) {
            rank.rankUpCommands = new ArrayList<>();
        }
        rank.rankUpCommands.add(command);

        Output.get().sendInfo(sender, "Added command '%s' to the rank '%s'.", command, rank.name);

    }

    @Command(identifier = "ranks command remove", description = "Removes a command from a rank.", onlyPlayers = false, permissions = {
        "ranks.admin"})
    public void commandRemove(CommandSender sender, @Arg(name = "rank") String rankName,
        @Arg(name = "command") String command) {
        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }

        Optional<Rank> rankOptional = PrisonRanks.getInstance().getRankManager().getRank(rankName);
        if (!rankOptional.isPresent()) {
            Output.get().sendError(sender, "The rank '%s' does not exist.", rankName);
            return;
        }
        Rank rank = rankOptional.get();

        if (rank.rankUpCommands == null) {
            rank.rankUpCommands = new ArrayList<>();
        }
        boolean did = rank.rankUpCommands.remove(command);

        if (!did) {
            Output.get()
                .sendWarn(sender, "The rank doesn't contain that command. Nothing was changed.");
        } else {
            Output.get()
                .sendInfo(sender, "Removed command '%s' from the rank '%s'.", command, rank.name);
        }

    }

    @Command(identifier = "ranks ladder add", description = "Creates a new rank ladder.", onlyPlayers = false, permissions = {
        "ranks.admin"})
    public void ladderAdd(CommandSender sender, @Arg(name = "ladderName") String ladderName) {
        Optional<RankLadder> ladderOptional =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);
        if (ladderOptional.isPresent()) {
            Output.get()
                .sendError(sender, "A ladder with the name '%s' already exists.", ladderName);
            return;
        }

        ladderOptional = PrisonRanks.getInstance().getLadderManager().createLadder(ladderName);

        if (!ladderOptional.isPresent()) {
            Output.get().sendError(sender,
                "An error occurred while creating your ladder. &8Check the console for details.");
            return;
        }

        try {
            PrisonRanks.getInstance().getLadderManager().saveLadder(ladderOptional.get());
        } catch (IOException e) {
            Output.get().sendError(sender,
                "An error occurred while creating your ladder. &8Check the console for details.");
            Output.get().logError("Could not save ladder.", e);
            return;
        }

        Output.get().sendInfo(sender, "The ladder '%s' has been created.", ladderName);
    }

    @Command(identifier = "ranks ladder remove", description = "Deletes a rank ladder.", onlyPlayers = false, permissions = "ranks.admin")
    public void ladderRemove(CommandSender sender, @Arg(name = "ladderName") String ladderName) {
        Optional<RankLadder> ladder =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);

        if (!ladder.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' doesn't exist.", ladderName);
            return;
        }

        boolean success = PrisonRanks.getInstance().getLadderManager().removeLadder(ladder.get());
        if (success) {
            Output.get().sendInfo(sender, "The ladder '%s' has been deleted.", ladderName);
        } else {
            Output.get().sendError(sender,
                "An error occurred while removing your ladder. &8Check the console for details.");
        }
    }

    @Command(identifier = "ranks ladder list", description = "Lists all rank ladders.", onlyPlayers = false, permissions = "ranks.admin")
    public void ladderList(CommandSender sender) {
        ChatDisplay display = new ChatDisplay("Ladders");
        BulletedListComponent.BulletedListBuilder list =
            new BulletedListComponent.BulletedListBuilder();
        for (RankLadder ladder : PrisonRanks.getInstance().getLadderManager().getLadders()) {
            list.add(ladder.name);
        }
        display.addComponent(list.build());

        display.send(sender);
    }

    @Command(identifier = "ranks ladder info", description = "Lists the ranks within a ladder.", onlyPlayers = false, permissions = "ranks.admin")
    public void ladderInfo(CommandSender sender, @Arg(name = "ladderName") String ladderName) {
        Optional<RankLadder> ladder =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);

        if (!ladder.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' doesn't exist.", ladderName);
            return;
        }

        ChatDisplay display = new ChatDisplay(ladder.get().name);
        display.text("&7This ladder contains the following ranks:");

        BulletedListComponent.BulletedListBuilder builder =
            new BulletedListComponent.BulletedListBuilder();
        for (RankLadder.PositionRank rank : ladder.get().ranks) {
            builder.add("&3#%d &8- &3%s", rank.getPosition(),
                PrisonRanks.getInstance().getRankManager().getRank(rank.getRankId()).get().name);
        }

        display.addComponent(builder.build());

        display.send(sender);
    }

    @Command(identifier = "ranks ladder addRank", description = "Adds a rank to a ladder.", onlyPlayers = false, permissions = "ranks.admin")
    public void ladderAddRank(CommandSender sender, @Arg(name = "ladderName") String ladderName,
        @Arg(name = "rankName") String rankName, @Arg(name = "position", def = "0", verifiers = "min[0]") int position) {
        Optional<RankLadder> ladder =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);
        if (!ladder.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' doesn't exist.", ladderName);
            return;
        }

        Optional<Rank> rank = PrisonRanks.getInstance().getRankManager().getRank(rankName);
        if (!rank.isPresent()) {
            Output.get().sendError(sender, "The rank '%s' doesn't exist.", rankName);
            return;
        }

        // TODO DUPLICATE DETECTION (DON'T LET THE SAME RANK BE ADDED TWICE)

        if(position > 0) {
            ladder.get().addRank(position, rank.get());
        } else {
            ladder.get().addRank(rank.get());
        }

        try {
            PrisonRanks.getInstance().getLadderManager().saveLadder(ladder.get());
        } catch (IOException e) {
            Output.get().sendError(sender, "An error occurred while adding a rank to your ladder. &8Check the console for details.");
            Output.get().logError("Error while saving ladder.", e);
            return;
        }

        Output.get().sendInfo(sender, "Added rank '%s' to ladder '%s'.", rank.get().name, ladder.get().name);
    }

    @Command(identifier = "ranks ladder removeRank", description = "Removes a rank from a ladder.", onlyPlayers = false, permissions = "ranks.admin")
    public void ladderRemoveRank(CommandSender sender, @Arg(name = "ladderName") String ladderName, @Arg(name = "rankName") String rankName) {
        Optional<RankLadder> ladder =
            PrisonRanks.getInstance().getLadderManager().getLadder(ladderName);
        if (!ladder.isPresent()) {
            Output.get().sendError(sender, "The ladder '%s' doesn't exist.", ladderName);
            return;
        }

        Optional<Rank> rank = PrisonRanks.getInstance().getRankManager().getRank(rankName);
        if (!rank.isPresent()) {
            Output.get().sendError(sender, "The rank '%s' doesn't exist.", rankName);
            return;
        }

        ladder.get().removeRank(ladder.get().getPositionOfRank(rank.get()));

        try {
            PrisonRanks.getInstance().getLadderManager().saveLadder(ladder.get());
        } catch (IOException e) {
            Output.get().sendError(sender, "An error occurred while removing a rank from your ladder. &8Check the console for details.");
            Output.get().logError("Error while saving ladder.", e);
            return;
        }

        Output.get().sendInfo(sender, "Removed rank '%s' from ladder '%s'.", rank.get().name, ladder.get().name);
    }

}
