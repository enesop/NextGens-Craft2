package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.gui.PlayerSettingsInventory;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayerSettingsCommand {

    public static void register(UserManager userManager) {
        // check if the command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("commands.player_settings.enabled")) {
            return;
        }
        PlayerSettingsCommand command = new PlayerSettingsCommand(userManager);
        // register the command
        command.register();
    }

    private final UserManager userManager;
    private final CommandAPICommand command;

    public PlayerSettingsCommand(UserManager userManager) {
        this.userManager = userManager;

        // get variables we need
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        String mainCommand = config.getString("commands.player_settings.command");
        List<String> aliases = config.getStringList("commands.player_settings.aliases");

        // set the command
        this.command = new CommandAPICommand(mainCommand)
                .withPermission("nextgens.settings")
                .executesPlayer((player, args) -> {
                    PlayerSettingsInventory.openInventory(player, this.userManager);
                });

        // set aliases
        this.command.setAliases(aliases.toArray(String[]::new));
    }

    public void register() {
        this.command.register();
    }

}
