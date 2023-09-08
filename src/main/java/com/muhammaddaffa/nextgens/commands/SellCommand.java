package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.mdlib.commandapi.CommandAPIBukkit;
import com.muhammaddaffa.mdlib.commandapi.CommandAPICommand;
import com.muhammaddaffa.mdlib.commandapi.arguments.PlayerArgument;
import com.muhammaddaffa.mdlib.utils.*;
import com.muhammaddaffa.nextgens.users.managers.UserManager;
import org.bukkit.entity.Player;

import java.util.List;

public class SellCommand {

    public static void register(UserManager userManager) {
        // check if the command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("commands.sell.enabled")) {
            return;
        }
        SellCommand command = new SellCommand(userManager);
        // check if sell command is enabled
        if (!Config.getFileConfiguration("config.yml").getBoolean("sell-command")) {
            return;
        }
        Executor.sync(() -> {
            Logger.info("Sell command is enabled, overriding and registering sell command...");
            // unregister the command
            CommandAPIBukkit.unregister("sell", true, true);
            // register back the command
            command.register();
        });
    }

    private final UserManager userManager;
    private final CommandAPICommand command;
    public SellCommand(UserManager userManager) {
        this.userManager = userManager;
        this.command = new CommandAPICommand(Config.getFileConfiguration("config.yml").getString("commands.sell.command"))
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    Player finalTarget = null;
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.sell")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        if (!(sender instanceof Player player)) {
                            Common.sendMessage(sender, "&cUsage: /sell <player>");
                            return;
                        }
                        // sell the item to the player
                        finalTarget = player;
                    } else {
                        if (!sender.hasPermission("nextgens.sell.others")) {
                            Common.configMessage("config.yml", sender, "messages.no-permission");
                            return;
                        }
                        finalTarget = target;
                    }
                    // perform the sell
                    this.userManager.performSell(finalTarget, finalTarget.getInventory(), false);
                });
        List<String> aliases = Config.getFileConfiguration("config.yml").getStringList("commands.sell.aliases");
        this.command.setAliases(aliases.toArray(new String[0]));
    }

    public void register() {
        this.command.register();
    }

}
