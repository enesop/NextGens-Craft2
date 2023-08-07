package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.gui.ShopInventory;
import com.muhammaddaffa.nextgens.utils.Common;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record ShopCommand(
        GeneratorManager generatorManager
) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Common.sendMessage(sender, "&cUsage: /genshop <player>");
                return true;
            }
            if (!sender.hasPermission("nextgens.shop")) {
                Common.config(sender, "messages.no-permission");
                return true;
            }
            // open the gui for player
            ShopInventory.openInventory(player, this.generatorManager);
        }

        if (args.length == 1) {
            if (!sender.hasPermission("nextgens.shop.others")) {
                Common.config(sender, "messages.no-permission");
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Common.config(sender, "messages.target-not-found");
                return true;
            }
            // open the gui for player
            ShopInventory.openInventory(player, this.generatorManager);
        }

        return true;
    }

}
