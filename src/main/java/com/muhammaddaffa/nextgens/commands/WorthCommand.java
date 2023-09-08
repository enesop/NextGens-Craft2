package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.mdlib.utils.Placeholder;
import com.muhammaddaffa.nextgens.NextGens;
import com.muhammaddaffa.nextgens.api.GeneratorAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WorthCommand {

    public static void registerThis() {
        // check if the command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("commands.worth.enabled")) {
            return;
        }
        // register the command
        WorthCommand command = new WorthCommand();
        command.register();
    }

    private final CommandAPICommand command;
    public WorthCommand() {
        // get the file configuration
        FileConfiguration config = Config.getFileConfiguration("config.yml");
        this.command = new CommandAPICommand(config.getString("commands.worth.command"))
                .withPermission("nextgens.worth")
                .executes((sender, args) -> {
                    if (!(sender instanceof Player player)) {
                        return;
                    }
                    // get variables we need
                    GeneratorAPI api = NextGens.getApi();
                    ItemStack stack = player.getInventory().getItemInMainHand();
                    // get the worth of the item
                    Double worth = api.getWorth(stack);
                    // if worth is null, let player know
                    if (worth == null) {
                        Common.configMessage("config.yml", player, "messages.item-worthless");
                        return;
                    }
                    // send message
                    Common.configMessage("config.yml", player, "messages.item-worth", new Placeholder()
                            .add("{worth}", Common.digits(worth)));
                });
        // set the aliases
        this.command.setAliases(config.getStringList("commands.worth.aliases").toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

}
