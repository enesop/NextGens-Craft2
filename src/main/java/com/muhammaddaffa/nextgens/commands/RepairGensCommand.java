package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.generators.listeners.helpers.GeneratorFixHelper;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.gui.PlayerSettingsInventory;
import com.muhammaddaffa.nextgens.users.UserManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class RepairGensCommand {

    public static void register(GeneratorManager generatorManager) {
        // check if the command is enabled
        if (!NextGens.DEFAULT_CONFIG.getConfig().getBoolean("commands.repair_gens.enabled")) {
            return;
        }
        RepairGensCommand command = new RepairGensCommand(generatorManager);
        // register the command
        command.register();
    }

    private final CommandAPICommand command;

    public RepairGensCommand(GeneratorManager generatorManager) {
        // get variables we need
        FileConfiguration config = NextGens.DEFAULT_CONFIG.getConfig();
        String mainCommand = config.getString("commands.repair_gens.command");
        List<String> aliases = config.getStringList("commands.repair_gens.aliases");

        // set the command
        this.command = new CommandAPICommand(mainCommand)
                .withPermission("nextgens.repairgens")
                .executesPlayer((player, args) -> {
                    GeneratorFixHelper.fixGenerators(player, generatorManager);
                });

        // set aliases
        this.command.setAliases(aliases.toArray(String[]::new));
    }

    public void register() {
        this.command.register();
    }

}
