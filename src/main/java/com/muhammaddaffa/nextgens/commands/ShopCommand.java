package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.commandapi.arguments.PlayerArgument;
import com.muhammaddaffa.mdlib.utils.Common;
import com.muhammaddaffa.mdlib.utils.Config;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.gui.ShopInventory;
import org.bukkit.entity.Player;

import java.util.List;

public class ShopCommand {

    public static void register(GeneratorManager generatorManager) {
        // check if the command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("commands.shop.enabled")) {
            return;
        }
        ShopCommand command = new ShopCommand(generatorManager);
        // register the command
        command.register();
    }

    private final GeneratorManager generatorManager;
    private final CommandAPICommand command;
    public ShopCommand(GeneratorManager generatorManager) {
        this.generatorManager = generatorManager;
        this.command = new CommandAPICommand(Config.getFileConfiguration("config.yml").getString("commands.shop.command"))
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    Player actualTarget = null;
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.shop")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        if (!(sender instanceof Player player)) {
                            Common.sendMessage(sender, "&cUsage: /genshop <player>");
                            return;
                        }
                        actualTarget = player;

                    } else {
                        if (!sender.hasPermission("nextgens.shop.others")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        actualTarget = target;
                    }
                    // open the gui for the target
                    ShopInventory.openInventory(actualTarget, this.generatorManager);
                });
        List<String> aliases = Config.getFileConfiguration("config.yml").getStringList("commands.shop.aliases");
        this.command.setAliases(aliases.toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

}
