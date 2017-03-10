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

package tech.mcprison.prison.ranks.managers;

import com.google.common.eventbus.Subscribe;
import tech.mcprison.prison.Prison;
import tech.mcprison.prison.internal.events.player.PlayerJoinEvent;
import tech.mcprison.prison.output.Output;
import tech.mcprison.prison.ranks.data.RankPlayer;
import tech.mcprison.prison.ranks.events.FirstJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all the players in the records.
 *
 * @author Faizaan A. Datoo
 */
public class PlayerManager {

    /*
     * Fields & Constants
     */

    public static final String PLAYER_EXTENSION = ".player.json";

    private File playerFolder;
    private List<RankPlayer> players;

    /*
     * Constructor
     */

    public PlayerManager(File playerFolder) {
        this.playerFolder = playerFolder;
        this.players = new ArrayList<>();

        Prison.get().getEventBus().register(this);
    }

    /*
     * Methods
     */

    /**
     * Loads a player from a file and stores it in the registry for use on the server.
     *
     * @param playerFile The key that the player data is stored as. Case-sensitive.
     * @throws IOException If the file could not be read, or if the file does not exist.
     */
    public void loadPlayer(String playerFile) throws IOException {
        RankPlayer player =
            Prison.get().getPlatform().getStorage().read(playerFile, RankPlayer.class);
        players.add(player);
    }

    /**
     * Loads every player in the specified playerFolder.
     *
     * @throws IOException If one of the files could not be read, or if the playerFolder does not exist.
     */
    public void loadPlayers() throws IOException {
        List<RankPlayer> players =
            Prison.get().getPlatform().getStorage().readAll(RankPlayer.class);
        this.players.addAll(players);
    }

    /**
     * Saves a {@link RankPlayer} to disk.
     *
     * @param player     The {@link RankPlayer} to save.
     * @param playerFile The key to save as.
     * @throws IOException If the file could not be created or written to.
     * @see #savePlayer(RankPlayer) To save with the default conventional filename.
     */
    public void savePlayer(RankPlayer player, String playerFile) throws IOException {
        Prison.get().getPlatform().getStorage().write(playerFile, player);
    }

    public void savePlayer(RankPlayer player) throws IOException {
        this.savePlayer(player, "player_" + player.uid.getLeastSignificantBits());
    }

    /**
     * Saves every player in the registry.
     *
     * @throws IOException If one of the players could not be saved.
     * @see #savePlayer(RankPlayer, String)
     */
    public void savePlayers() throws IOException {
        for (RankPlayer player : players) {
            savePlayer(player);
        }
    }

    /*
     * Getters & Setters
     */

    public File getPlayerFolder() {
        return playerFolder;
    }

    public List<RankPlayer> getPlayers() {
        return players;
    }

    public Optional<RankPlayer> getPlayer(UUID uid) {
        return players.stream().filter(player -> player.uid.equals(uid)).findFirst();
    }

    /*
     * Listeners
     */

    @Subscribe public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getPlayer(event.getPlayer().getUUID()).isPresent()) {
            // We need to create a new player data file.
            RankPlayer newPlayer = new RankPlayer();
            newPlayer.uid = event.getPlayer().getUUID();
            newPlayer.ranks = new HashMap<>();

            players.add(newPlayer);

            try {
                savePlayer(newPlayer);
            } catch (IOException e) {
                Output.get().logError(
                    "Failed to create new player data file for player " + event.getPlayer()
                        .getName(), e);
                return;
            }

            Prison.get().getEventBus().post(new FirstJoinEvent(newPlayer));
        }
    }

}
