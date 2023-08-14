package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.gui.ShopInventory;
import com.muhammaddaffa.nextgens.utils.Common;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

public class ShopCommand {

    public static void register(GeneratorManager generatorManager) {
        ShopCommand command = new ShopCommand(generatorManager);
        // register the command
        command.register();
    }

    private final GeneratorManager generatorManager;
    private final CommandAPICommand command;
    public ShopCommand(GeneratorManager generatorManager) {
        this.generatorManager = generatorManager;
        this.command = new CommandAPICommand("genshop")
                .withAliases("gensshop")
                .withOptionalArguments(new PlayerArgument("target"))
                .executes((sender, args) -> {
                    Player target = (Player) args.get("target");
                    Player actualTarget = null;
                    if (target == null) {
                        if (!sender.hasPermission("nextgens.shop")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        if (!(sender instanceof Player player)) {
                            Common.sendMessage(sender, "&cUsage: /genshop <player>");
                            return;
                        }
                        actualTarget = player;

                    } else {
                        if (!sender.hasPermission("nextgens.shop.others")) {
                            Common.config(sender, "messages.no-permission");
                            return;
                        }
                        actualTarget = target;
                    }
                    // open the gui for the target
                    ShopInventory.openInventory(actualTarget, this.generatorManager);
                });
    }

    public void register() {
        this.command.register();
    }

}
