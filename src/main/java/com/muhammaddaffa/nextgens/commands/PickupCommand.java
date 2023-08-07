package com.muhammaddaffa.nextgens.commands;

import com.muhammaddaffa.nextgens.generators.ActiveGenerator;
import com.muhammaddaffa.nextgens.generators.managers.GeneratorManager;
import com.muhammaddaffa.nextgens.utils.Common;
import com.muhammaddaffa.nextgens.utils.Config;
import com.muhammaddaffa.nextgens.utils.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record PickupCommand(
        GeneratorManager generatorManager
) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                Common.sendMessage(sender, "&cUsage: /pickupgens <player>");
                return true;
            }
            if (!sender.hasPermission("nextgens.pickup")) {
                Common.config(sender, "messages.no-permission");
                return true;
            }
            // get all generators that player have
            List<ActiveGenerator> generators = this.generatorManager.getActiveGenerator(player);
            int total = generators.size();
            // loop the generator
            for (ActiveGenerator active : generators) {
                // check if broken-pickup option is enabled
                if (Config.CONFIG.getBoolean("broken-pickup") && active.isCorrupted()) {
                    total--;
                    continue;
                }
                // unregister the generator
                this.generatorManager.unregisterGenerator(active.getLocation());
                // set the block to be air
                active.getLocation().getBlock().setType(Material.AIR);
                // give the item to the player
                Common.addInventoryItem(player, active.getGenerator().createItem(1));
            }
            // send message
            Common.config(player, "messages.pickup-gens", new Placeholder()
                    .add("{amount}", Common.digits(total)));
            // send sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }

        if (args.length == 1) {
            if (!sender.hasPermission("nextgens.pickup.others")) {
                Common.config(sender, "messages.no-permission");
                return true;
            }
            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                Common.config(sender, "messages.target-not-found");
                return true;
            }
            // get all generators that player have
            List<ActiveGenerator> generators = this.generatorManager.getActiveGenerator(player);
            int total = generators.size();
            // loop the generator
            for (ActiveGenerator active : generators) {
                // check if broken-pickup option is enabled
                if (Config.CONFIG.getBoolean("broken-pickup") && active.isCorrupted()) {
                    total--;
                    continue;
                }
                // unregister the generator
                this.generatorManager.unregisterGenerator(active.getLocation());
                // set the block to be air
                active.getLocation().getBlock().setType(Material.AIR);
                // give the item to the player
                Common.addInventoryItem(player, active.getGenerator().createItem(1));
            }
            // send message
            Common.config(sender, "messages.force-pickup", new Placeholder()
                    .add("{player}", player.getName())
                    .add("{amount}", Common.digits(total)));
            Common.config(player, "messages.player-pickup", new Placeholder()
                    .add("{amount}", Common.digits(total)));
            // send sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
        }

        return true;
    }

}
